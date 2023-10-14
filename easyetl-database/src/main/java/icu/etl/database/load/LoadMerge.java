package icu.etl.database.load;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.internal.StandardDatabaseIndex;
import icu.etl.database.internal.StandardDatabaseTable;
import icu.etl.expression.ScriptAnalysis;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 当数据库不支持 merge into 语句时，通过 update 与 insert 语句方式实现 merge into 语句功能 <br>
 * 实现方式： <br>
 * 1. 新建一个临时表，表结构与目标表一致 <br>
 * 2. 将数据批量插入并保存到临时表中 <br>
 * 3. 在临时表上建立索引 <br>
 * 4. 将临时表中与目标表中索引相同的数据更新到目标表中 <br>
 * 5. 将临时表中的增量数据保存到目标表中 <br>
 * 6. 删除临时表及其数据与索引 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-09-02
 */
public class LoadMerge {

    /** 数据库操作接口 */
    private JdbcDao dao;

    /** 目标表 */
    private LoadTable target;

    /** 索引字段 */
    private List<String> indexColumn;

    /** 目标表字段集合 */
    private List<DatabaseTableColumn> columns;

    /** 更新语句 */
    private String updateSQL;

    /** 插入语句 */
    private String insertSQL;

    /** 临时表上的索引信息 */
    private StandardDatabaseIndex index;

    /** 数据库表信息 */
    private DatabaseTable table;

    /** 临时表信息 */
    private StandardDatabaseTable tempTable;

    /**
     * 初始化
     *
     * @param dao
     * @param target
     * @param indexColumn
     * @param columns
     * @throws SQLException
     */
    public LoadMerge(JdbcDao dao, LoadTable target, List<String> indexColumn, List<DatabaseTableColumn> columns) throws SQLException {
        this.dao = dao;
        this.target = target;
        this.indexColumn = indexColumn;
        this.columns = columns;

        this.table = target.getTable();
        this.tempTable = this.createTemp();
        this.index = this.toIndex();
        this.updateSQL = this.toUpdateSQL();
        this.insertSQL = this.toInsertSQL();
    }

    /**
     * 返回临时表信息
     *
     * @return
     */
    public DatabaseTable getTempTable() {
        return this.tempTable;
    }

    /**
     * 删除临时表
     *
     * @throws SQLException
     */
    public void removeTempTable() throws SQLException {
        DatabaseDialect dialect = this.dao.getDialect();
        DatabaseDDL ddl = dialect.toDDL(this.dao.getConnection(), this.index, false);
        this.dao.executeByJdbc(ddl);

        // 重组索引
        List<DatabaseIndex> list = new ArrayList<DatabaseIndex>();
        list.add(this.index);
        dialect.reorgRunstatsIndexs(this.dao.getConnection(), list);

        // 更新已有数据与保存新增数据
        this.dao.executeUpdateByJdbc(this.updateSQL);
        this.dao.executeUpdateByJdbc(this.insertSQL);

        // 删除数据库表信息
        this.dao.dropTable(this.tempTable);
    }

    /**
     * 创建临时表
     *
     * @return
     * @throws SQLException
     */
    private StandardDatabaseTable createTemp() throws SQLException {
        String fullname = this.table.getFullName();
        String schema = Jdbc.getSchema(fullname);
        String tableName = Jdbc.removeSchema(fullname);
        String newTableName = Jdbc.getTableNameNoRepeat(this.dao.getConnection(), this.dao.getDialect(), null, schema, tableName);

        StandardDatabaseTable newtable = new StandardDatabaseTable(this.target.getTable());
        newtable.setName(newTableName);

        String tableDDL = this.target.getTableDDL().getTable();
        ScriptAnalysis analysis = new ScriptAnalysis();
        int[] indexs = analysis.indexOf(tableDDL, new String[]{"create", "table"}, 0);
        if (indexs == null) {
            throw new SQLException(tableDDL);
        }

        int begin = analysis.indexOf(tableDDL, tableName, indexs[1], 0, 0);
        if (begin == -1) {
            throw new SQLException(tableDDL);
        }

        String newTableDDL = StringUtils.replace(tableDDL, begin, tableName.length(), newTableName);
        this.dao.executeByJdbc(newTableDDL); // 建立临时表
        return newtable;
    }

    /**
     * 生成临时表索引信息
     *
     * @return
     */
    private StandardDatabaseIndex toIndex() {
        DatabaseDialect dialect = this.dao.getDialect();

        // 索引字段的位置信息
        List<Integer> positions = new ArrayList<Integer>(this.indexColumn.size());
        for (int i = 1; i <= this.indexColumn.size(); i++) {
            positions.add(new Integer(i));
        }

        // 索引字段的排序规则
        ArrayList<Integer> sorts = ArrayUtils.asList(new Integer[this.indexColumn.size()]);
        Collections.fill(sorts, DatabaseIndex.INDEX_ASC);

        // 创建索引
        StandardDatabaseIndex index = new StandardDatabaseIndex();
        index.setName(this.tempTable.getName() + "IDX");
        index.setSchema(this.tempTable.getSchema());
        index.setTableCatalog(this.tempTable.getCatalog());
        index.setTableSchema(this.tempTable.getSchema());
        index.setTableName(this.tempTable.getName());
        index.setUnique(false);
        index.setTableFullName(dialect.toTableName(index.getTableCatalog(), index.getTableSchema(), index.getTableName()));
        index.setFullName(dialect.toIndexName(index.getTableCatalog(), index.getSchema(), index.getName()));
        index.setColumnNames(this.indexColumn);
        index.setPositions(positions);
        index.setSort(sorts);
        return index;
    }

    /**
     * 生成 update 更新语句
     *
     * @return
     */
    private String toUpdateSQL() {
        String tableName = this.table.getFullName();
        String sql = "update " + tableName + " a set (" + FileUtils.lineSeparator;
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            sql += "    " + col.getName() + (it.hasNext() ? "," : "") + FileUtils.lineSeparator;
        }

        sql += ") = ( select " + FileUtils.lineSeparator;
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            sql += "    " + col.getName() + (it.hasNext() ? "," : "") + FileUtils.lineSeparator;
        }

        String newtableName = this.tempTable.getFullName();
        sql += " from " + newtableName + " b where " + FileUtils.lineSeparator;
        boolean value = false;
        List<String> indexColumn = this.indexColumn;
        for (Iterator<String> it = indexColumn.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (value) {
                sql += " and ";
            }
            sql += "a." + name + " = b." + name + FileUtils.lineSeparator;
            value = true;
        }
        sql += ") where exists (" + FileUtils.lineSeparator;
        sql += "select 1 from " + newtableName + " b where " + FileUtils.lineSeparator;

        value = false;
        for (Iterator<String> it = indexColumn.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (value) {
                sql += " and ";
            }
            sql += "a." + name + " = b." + name + FileUtils.lineSeparator;
            value = true;
        }
        sql += ")";
        return sql;
    }

    /**
     * 生成 insert into 语句
     *
     * @return
     */
    private String toInsertSQL() {
        String tableName = this.table.getFullName();
        String sql = "insert into " + tableName + " a (" + FileUtils.lineSeparator;
        for (Iterator<DatabaseTableColumn> it = this.columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            sql += "    " + col.getName() + (it.hasNext() ? "," : "") + FileUtils.lineSeparator;
        }

        sql += ") select " + FileUtils.lineSeparator;
        for (Iterator<DatabaseTableColumn> it = this.columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            sql += "    " + col.getName() + (it.hasNext() ? "," : "") + FileUtils.lineSeparator;
        }

        String newTableName = this.tempTable.getFullName();
        sql += " from " + newTableName + " b where ";

        boolean value = false;
        List<String> indexColumn = this.indexColumn;
        for (Iterator<String> it = indexColumn.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (value) {
                sql += " and ";
            }
            sql += "a." + name + " = b." + name + FileUtils.lineSeparator;
            value = true;
        }
        return sql;
    }

}
