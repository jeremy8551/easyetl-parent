package icu.etl.database.load.serial;

import java.sql.SQLException;
import java.sql.Types;

import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumnList;
import icu.etl.database.DatabaseType;
import icu.etl.database.DatabaseTypeSet;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.internal.StandardDatabaseTable;
import icu.etl.database.internal.StandardDatabaseTableColumn;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadErrorTable;
import icu.etl.database.load.inernal.DataWriter;
import icu.etl.database.load.inernal.ErrorDataWriter;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.util.StringUtils;

/**
 * 将文件装载到数据库表中时，如果出现主键冲突错误。则自动将数据文件中的重复数据保存到错误信息表中
 *
 * @author jeremy8551@qq.com
 */
public class PrimaryRepeatExceptionProcessor {

    /** 上下文信息 */
    private LoadEngineContext context;

    /** 文件输入流 */
    private TextTableFileReader in;

    public PrimaryRepeatExceptionProcessor(LoadEngineContext context) {
        super();

        if (context == null) {
            throw new NullPointerException();
        } else {
            this.context = context;
        }
    }

    /**
     * 扫描数据文件并与目标表中字段类型进行比较，并自动扩容数据库表中字段长度
     *
     * @param dao    数据库接口
     * @param target 目标表
     * @param file   数据文件
     * @return 返回已修改字段个数
     * @throws Exception
     */
    public boolean execute(JdbcDao dao, LoadEngineContext context, DatabaseTable target, TextTableFile file, DataWriter out) throws Exception {
        String catalog = this.context.getTableCatalog();
        String schema = this.context.getErrorTableSchema();
        String tableName = this.context.getErrorTableName();

        DatabaseTable errtab = dao.getTable(catalog, schema, tableName);
        if (errtab == null) { // 创建一个错误信息，表结构与目标表一致，最右端多了一个错误时间字段与错误原因字段
            errtab = this.createErrorTable(dao, target, catalog, schema, tableName);
        }

        LoadErrorTable table = new LoadErrorTable(dao, errtab);
        return this.execute(dao, file, out, table);
    }

    /**
     * 创建一个错误信息表
     *
     * @param dao
     * @param target
     * @param catalog
     * @param schema
     * @param tableName
     * @return
     * @throws SQLException
     */
    private StandardDatabaseTable createErrorTable(JdbcDao dao, DatabaseTable target, String catalog, String schema, String tableName) throws SQLException {
        StandardDatabaseTable table = new StandardDatabaseTable(target);
        if (StringUtils.isNotBlank(catalog)) {
            table.setCatalog(catalog);
        }
        if (StringUtils.isNotBlank(schema)) {
            table.setSchema(schema);
        }
        table.setName(tableName);
        table.setFullName(dao.getDialect().toTableName(table.getCatalog(), table.getSchema(), table.getName()));

        DatabaseTableColumnList columns = table.getColumns();
        DatabaseTypeSet types = Jdbc.getTypeInfo(dao.getConnection());
        DatabaseType type1 = types.get(Types.TIMESTAMP);
        DatabaseType type2 = types.get(Types.CLOB);

        // 发生错误的时间
        StandardDatabaseTableColumn col1 = new StandardDatabaseTableColumn();
        col1.setName("ERR_TIMESTAMP");
        col1.setTableFullName(table.getFullName());
        col1.setTableCatalog(table.getCatalog());
        col1.setTableSchema(table.getSchema());
        col1.setTableName(table.getName());
        col1.setType(type1);
        col1.setFieldType(type1.getName());
        col1.setLength(type1.getPrecision()); // yyyy-MM-dd hh:mm:ss:SSS
        col1.setMaxLength(type1.getPrecision());
        col1.setSqlType(type1.getSqlType());
        col1.setDefault("");
        col1.setNullAble("YES");
        col1.setRemark("");
        col1.setPosition(columns.size() + 1);
        col1.setDigit(0);
        col1.setRadix(10);
        col1.setIncrement("");
        columns.add(col1);

        // 发生错误的原因
        StandardDatabaseTableColumn col2 = new StandardDatabaseTableColumn();
        col2.setName("ERR_REASON");
        col2.setTableFullName(table.getFullName());
        col2.setTableCatalog(table.getCatalog());
        col2.setTableSchema(table.getSchema());
        col2.setTableName(table.getName());
        col2.setType(type2);
        col2.setFieldType(type2.getName());
        col2.setLength(type2.getPrecision());
        col2.setMaxLength(type2.getPrecision());
        col2.setSqlType(type2.getSqlType());
        col2.setDefault("");
        col2.setNullAble("YES");
        col2.setRemark("");
        col2.setPosition(columns.size() + 1);
        col2.setDigit(0);
        col2.setRadix(10);
        col2.setIncrement("");
        columns.add(col2);

        // 删除表上的主键和索引
        table.getPrimaryIndexs().clear();
        table.getIndexs().clear();

        // 建表
        dao.createTable(dao.toDDL(table));
        return table;
    }

    /**
     * 将主键冲突的表保存到错误信息表中
     *
     * @param dao
     * @param file
     * @param out
     * @param target
     * @return
     * @throws Exception
     */
    private boolean execute(JdbcDao dao, TextTableFile file, DataWriter out, LoadErrorTable target) throws Exception {
        long commit = out.getCommitRecords(); // 未发生主键冲突错误的起始位置
        long count = this.context.getSavecount();

        // 将错误数据保存到错误信息表中
        ErrorDataWriter errout = new ErrorDataWriter(dao, target, count);
        try {
            errout.open();

            // 逐行提交事务，判断是哪一行发生主键冲突
            this.in = file.getReader(this.context.getReadBuffer());
            if (this.in.skip(0, commit)) {
                TextTableLine line = null;
                for (int i = 0; i < count && (line = this.in.readLine()) != null; i++) {
                    try {
                        out.write(line);
                        out.commit();
                    } catch (SQLException e) { // 如果发生主键冲突异常
                        if (dao.getDialect().isPrimaryRepeatException(e)) {
                            errout.write(line, e);
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }

            errout.commit();
            return true;
        } finally {
            errout.close();
        }
    }

    public TextTableFileReader getReader() {
        return in;
    }

}
