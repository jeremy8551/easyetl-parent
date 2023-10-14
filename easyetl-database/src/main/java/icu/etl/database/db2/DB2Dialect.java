package icu.etl.database.db2;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.database.DatabaseConfiguration;
import icu.etl.database.DatabaseConfigurationContainer;
import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseException;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.DatabaseSpaceList;
import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;
import icu.etl.database.DatabaseTableDDL;
import icu.etl.database.DatabaseTypeSet;
import icu.etl.database.DatabaseURL;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.database.SQL;
import icu.etl.database.db2.expconv.BigDecimalConverter;
import icu.etl.database.db2.expconv.ByteArrayConverter;
import icu.etl.database.db2.expconv.DoubleConverter;
import icu.etl.database.db2.expconv.RealConverter;
import icu.etl.database.db2.expconv.StringConverter;
import icu.etl.database.db2.expconv.TimeConverter;
import icu.etl.database.db2.expconv.TimestampConverter;
import icu.etl.database.export.converter.AbstractConverter;
import icu.etl.database.export.converter.BlobConverter;
import icu.etl.database.export.converter.ClobConverter;
import icu.etl.database.export.converter.DateConverter;
import icu.etl.database.export.converter.IntegerConverter;
import icu.etl.database.export.converter.LongConverter;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.StandardDatabaseDDL;
import icu.etl.database.internal.StandardDatabaseIndex;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.internal.StandardDatabaseProcedureParameter;
import icu.etl.database.internal.StandardDatabaseSpace;
import icu.etl.database.internal.StandardDatabaseTable;
import icu.etl.database.internal.StandardDatabaseTableDDL;
import icu.etl.database.internal.StandardDatabaseURL;
import icu.etl.database.internal.StandardJdbcConverterMapper;
import icu.etl.database.pool.PoolConnection;
import icu.etl.io.BufferedLineReader;
import icu.etl.io.ClobWriter;
import icu.etl.ioc.BeanFactory;
import icu.etl.log.STD;
import icu.etl.os.OS;
import icu.etl.os.OSAccount;
import icu.etl.os.OSCommand;
import icu.etl.os.OSCommandException;
import icu.etl.timer.Timer;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

/**
 * 关于DB2数据库的 icu.etl.db.dialect.DatabaseDialect 数据库方言接口实现类
 *
 * @author jeremy8551@qq.com
 */
@EasyBeanClass(kind = "db2", mode = "", major = "", minor = "", description = "IBM DB2 数据库方言模版", type = DatabaseDialect.class)
public class DB2Dialect extends AbstractDialect implements DatabaseDialect {

    /** 进程编号名 */
    public final static String APPLICATION_ID = "applicationId";

    /** 应用程序句柄 */
    public final static String APPLICATION_HANDLE = "application_handle";

    /** 数据库中字段类型与卸载处理逻辑的映射关系 */
    protected StandardJdbcConverterMapper exp;

    protected StandardJdbcConverterMapper map;

    public DB2Dialect() {
        super();

        // 添加DB2数据库的关键字信息
        // this.getDB_KEYS().add("date");
        // this.getDB_KEYS().add("time");
        // this.getDB_KEYS().add("timestamp");
        this.keyword.add("first");
        this.keyword.add("only");
        this.keyword.add("with");
        this.keyword.add("logged");

        // 数据类型
        this.keyword.add("array");
        this.keyword.add("bigint");
        this.keyword.add("blob");
        this.keyword.add("boolean");
        this.keyword.add("char");
        this.keyword.add("varchar");
        this.keyword.add("char() for bit data");
        this.keyword.add("clob");
        this.keyword.add("date");
        this.keyword.add("dbclob");
        this.keyword.add("decfloat");
        this.keyword.add("decimal");
        this.keyword.add("double");
        this.keyword.add("graphic");
        this.keyword.add("integer");
        this.keyword.add("long varchar");
        this.keyword.add("long varchar for bit data");
        this.keyword.add("long graphic");
        this.keyword.add("real");
        this.keyword.add("row");
        this.keyword.add("smallint");
        this.keyword.add("time");
        this.keyword.add("timestamp");
        this.keyword.add("vargraphic");
        this.keyword.add("xml");
        this.keyword.add("varchar() for bit data");

        this.keyword.add("DETERMINISTIC");
        this.keyword.add("DISALLOW");
        this.keyword.add("DISCONNECT");
        this.keyword.add("DISTINCT");
        this.keyword.add("DO");
        this.keyword.add("DOUBLE");
        this.keyword.add("DROP");
        this.keyword.add("DSNHATTR");
        this.keyword.add("DSSIZE");
        this.keyword.add("DYNAMIC");
        this.keyword.add("EACH");
        this.keyword.add("EDITPROC");
        this.keyword.add("ELSE");
        this.keyword.add("ELSEIF");
        this.keyword.add("ENCODING");
        this.keyword.add("END");
        this.keyword.add("END-EXEC");
        this.keyword.add("END-EXEC1");
        this.keyword.add("ERASE");
        this.keyword.add("ESCAPE");
        this.keyword.add("EXCEPT");
        this.keyword.add("EXCEPTION");
        this.keyword.add("EXCLUDING");
        this.keyword.add("EXECUTE");
        this.keyword.add("EXISTS");
        this.keyword.add("EXIT");
        this.keyword.add("EXTERNAL");
        this.keyword.add("FENCED");
        this.keyword.add("FETCH");
        this.keyword.add("FIELDPROC");
        this.keyword.add("FILE");
        this.keyword.add("FINAL");
        this.keyword.add("FOR");
        this.keyword.add("FOREIGN");
        this.keyword.add("FREE");
        this.keyword.add("FROM");
        this.keyword.add("FULL");
        this.keyword.add("FUNCTION");
        this.keyword.add("GENERAL");
        this.keyword.add("GENERATED");
        this.keyword.add("GET");
        this.keyword.add("GLOBAL");
        this.keyword.add("GO");
        this.keyword.add("GOTO");
        this.keyword.add("GRANT");
        this.keyword.add("GRAPHIC");
        this.keyword.add("GROUP");
        this.keyword.add("HANDLER");
        this.keyword.add("HAVING");
        this.keyword.add("HOLD");
        this.keyword.add("HOUR");
        this.keyword.add("HOURS");
        this.keyword.add("IDENTITY");
        this.keyword.add("IF");
        this.keyword.add("IMMEDIATE");
        this.keyword.add("IN");
        this.keyword.add("INCLUDING");
        this.keyword.add("INCREMENT");
        this.keyword.add("INDEX");
        this.keyword.add("INDICATOR");
        this.keyword.add("INHERIT");
        this.keyword.add("INNER");
        this.keyword.add("INOUT");
        this.keyword.add("INSENSITIVE");
        this.keyword.add("INSERT");
        this.keyword.add("INTEGRITY");
        this.keyword.add("INTO");
        this.keyword.add("IS");
        this.keyword.add("ISOBID");
        this.keyword.add("ISOLATION");
        this.keyword.add("ITERATE");
        this.keyword.add("JAR");
        this.keyword.add("JAVA");
        this.keyword.add("JOIN");
        this.keyword.add("KEY");
        this.keyword.add("LABEL");
        this.keyword.add("LANGUAGE");
        this.keyword.add("LC_CTYPE");
        this.keyword.add("ADD");
        this.keyword.add("AFTER");
        this.keyword.add("ALIAS");
        this.keyword.add("ALL");
        this.keyword.add("ALLOCATE");
        this.keyword.add("ALLOW");
        this.keyword.add("ALTER");
        this.keyword.add("AND");
        this.keyword.add("ANY");
        this.keyword.add("APPLICATION");
        this.keyword.add("AS");
        this.keyword.add("ASSOCIATE");
        this.keyword.add("ASUTIME");
        this.keyword.add("AUDIT");
        this.keyword.add("AUTHORIZATION");
        this.keyword.add("AUX");
        this.keyword.add("AUXILIARY");
        this.keyword.add("BEFORE");
        this.keyword.add("BEGIN");
        this.keyword.add("BETWEEN");
        this.keyword.add("BINARY");
        this.keyword.add("BUFFERPOOL");
        this.keyword.add("BY");
        this.keyword.add("CACHE");
        this.keyword.add("CALL");
        this.keyword.add("CALLED");
        this.keyword.add("CAPTURE");
        this.keyword.add("CARDINALITY");
        this.keyword.add("CASCADED");
        this.keyword.add("CASE");
        this.keyword.add("CAST");
        this.keyword.add("CCSID");
        this.keyword.add("CHAR");
        this.keyword.add("CHARACTER");
        this.keyword.add("CHECK");
        this.keyword.add("CLOSE");
        this.keyword.add("CLUSTER");
        this.keyword.add("COLLECTION");
        this.keyword.add("COLLID");
        this.keyword.add("COLUMN");
        this.keyword.add("COMMENT");
        this.keyword.add("COMMIT");
        this.keyword.add("CONCAT");
        this.keyword.add("CONDITION");
        this.keyword.add("CONNECT");
        this.keyword.add("CONNECTION");
        this.keyword.add("CONSTRAINT");
        this.keyword.add("CONTAINS");
        this.keyword.add("CONTINUE");
        this.keyword.add("COUNT");
        this.keyword.add("COUNT_BIG");
        this.keyword.add("CREATE");
        this.keyword.add("CROSS");
        this.keyword.add("CURRENT");
        this.keyword.add("CURRENT_DATE");
        this.keyword.add("CURRENT_LC_CTYPE");
        this.keyword.add("CURRENT_PATH");
        this.keyword.add("CURRENT_SERVER");
        this.keyword.add("CURRENT_TIME");
        this.keyword.add("CURRENT_TIMESTAMP");
        this.keyword.add("CURRENT_TIMEZONE");
        this.keyword.add("CURRENT_USER");
        this.keyword.add("CURSOR");
        this.keyword.add("CYCLE");
        this.keyword.add("DATA");
        this.keyword.add("DATABASE");
        this.keyword.add("DAY");
        this.keyword.add("DAYS");
        this.keyword.add("DB2GENERAL");
        this.keyword.add("DB2GENRL");
        this.keyword.add("DB2SQL");
        this.keyword.add("DBINFO");
        this.keyword.add("DECLARE");
        this.keyword.add("DEFAULT");
        this.keyword.add("DEFAULTS");
        this.keyword.add("DEFINITION");
        this.keyword.add("DELETE");
        this.keyword.add("DESCRIPTOR");
        this.keyword.add("LEAVE");
        this.keyword.add("LEFT");
        this.keyword.add("LIKE");
        this.keyword.add("LINKTYPE");
        this.keyword.add("LOCAL");
        this.keyword.add("LOCALE");
        this.keyword.add("LOCATOR");
        this.keyword.add("LOCATORS");
        this.keyword.add("LOCK");
        this.keyword.add("LOCKMAX");
        this.keyword.add("LOCKSIZE");
        this.keyword.add("LONG");
        this.keyword.add("LOOP");
        this.keyword.add("MAXVALUE");
        this.keyword.add("MICROSECOND");
        this.keyword.add("MICROSECONDS");
        this.keyword.add("MINUTE");
        this.keyword.add("MINUTES");
        this.keyword.add("MINVALUE");
        this.keyword.add("MODE");
        this.keyword.add("MODIFIES");
        this.keyword.add("MONTH");
        this.keyword.add("MONTHS");
        this.keyword.add("NEW");
        this.keyword.add("NEW_TABLE");
        this.keyword.add("NO");
        this.keyword.add("NOCACHE");
        this.keyword.add("NOCYCLE");
        this.keyword.add("NODENAME");
        this.keyword.add("NODENUMBER");
        this.keyword.add("NOMAXVALUE");
        this.keyword.add("NOMINVALUE");
        this.keyword.add("NOORDER");
        this.keyword.add("NOT");
        this.keyword.add("NULL");
        this.keyword.add("NULLS");
        this.keyword.add("NUMPARTS");
        this.keyword.add("OBID");
        this.keyword.add("OF");
        this.keyword.add("OLD");
        this.keyword.add("OLD_TABLE");
        this.keyword.add("ON");
        this.keyword.add("OPEN");
        this.keyword.add("OPTIMIZATION");
        this.keyword.add("OPTIMIZE");
        this.keyword.add("OPTION");
        this.keyword.add("OR");
        this.keyword.add("ORDER");
        this.keyword.add("OUT");
        this.keyword.add("OUTER");
        this.keyword.add("OVERRIDING");
        this.keyword.add("PACKAGE");
        this.keyword.add("PARAMETER");
        this.keyword.add("PART");
        this.keyword.add("PARTITION");
        this.keyword.add("PATH");
        this.keyword.add("PIECESIZE");
        this.keyword.add("PLAN");
        this.keyword.add("POSITION");
        this.keyword.add("PRECISION");
        this.keyword.add("PREPARE");
        this.keyword.add("PRIMARY");
        this.keyword.add("PRIQTY");
        this.keyword.add("PRIVILEGES");
        this.keyword.add("PROCEDURE");
        this.keyword.add("PROGRAM");
        this.keyword.add("PSID");
        this.keyword.add("QUERYNO");
        this.keyword.add("READ");
        this.keyword.add("READS");
        this.keyword.add("RECOVERY");
        this.keyword.add("REFERENCES");
        this.keyword.add("REFERENCING");
        this.keyword.add("RELEASE");
        this.keyword.add("RENAME");
        this.keyword.add("REPEAT");
        this.keyword.add("RESET");
        this.keyword.add("RESIGNAL");
        this.keyword.add("RESTART");
        this.keyword.add("RESTRICT");
        this.keyword.add("RESULT");
        this.keyword.add("RESULT_SET_LOCATOR");
        this.keyword.add("RETURN");
        this.keyword.add("RETURNS");
        this.keyword.add("REVOKE");
        this.keyword.add("RIGHT");
        this.keyword.add("ROLLBACK");
        this.keyword.add("ROUTINE");
        this.keyword.add("ROW");
        this.keyword.add("ROWS");
        this.keyword.add("RRN");
        this.keyword.add("RUN");
        this.keyword.add("SAVEPOINT");
        this.keyword.add("SCHEMA");
        this.keyword.add("SCRATCHPAD");
        this.keyword.add("SECOND");
        this.keyword.add("SECONDS");
        this.keyword.add("SECQTY");
        this.keyword.add("SECURITY");
        this.keyword.add("SELECT");
        this.keyword.add("SENSITIVE");
        this.keyword.add("SET");
        this.keyword.add("SIGNAL");
        this.keyword.add("SIMPLE");
        this.keyword.add("SOME");
        this.keyword.add("SOURCE");
        this.keyword.add("SPECIFIC");
        this.keyword.add("SQL");
        this.keyword.add("SQLID");
        this.keyword.add("STANDARD");
        this.keyword.add("START");
        this.keyword.add("STATIC");
        this.keyword.add("STAY");
        this.keyword.add("STOGROUP");
        this.keyword.add("STORES");
        this.keyword.add("STYLE");
        this.keyword.add("SUBPAGES");
        this.keyword.add("SUBSTRING");
        this.keyword.add("SYNONYM");
        this.keyword.add("SYSFUN");
        this.keyword.add("SYSIBM");
        this.keyword.add("SYSPROC");
        this.keyword.add("SYSTEM");
        this.keyword.add("TABLE");
        this.keyword.add("TABLESPACE");
        this.keyword.add("THEN");
        this.keyword.add("TO");
        this.keyword.add("TRANSACTION");
        this.keyword.add("TRIGGER");
        this.keyword.add("TRIM");
        this.keyword.add("TYPE");
        this.keyword.add("UNDO");
        this.keyword.add("UNION");
        this.keyword.add("UNIQUE");
        this.keyword.add("UNTIL");
        this.keyword.add("UPDATE");
        this.keyword.add("USAGE");
        this.keyword.add("USER");
        this.keyword.add("USING");
        this.keyword.add("VALIDPROC");
        this.keyword.add("VALUES");
        this.keyword.add("VARIABLE");
        this.keyword.add("VARIANT");
        this.keyword.add("VCAT");
        this.keyword.add("VIEW");
        this.keyword.add("VOLUMES");
        this.keyword.add("WHEN");
        this.keyword.add("WHERE");
        this.keyword.add("WHILE");
        this.keyword.add("WITH");
        this.keyword.add("WLM");
        this.keyword.add("WRITE");
        this.keyword.add("YEAR");
        this.keyword.add("YEARS");
    }

    public String dropPrimaryKey(Connection connection, DatabaseIndex index) throws SQLException {
        if (index == null) {
            throw new NullPointerException();
        }

        String sql = "alter table " + index.getTableFullName() + " drop primary key "; // + index.getFullName();
        int count = 0;
        while (true) {
            if (++count > 10) {
                break;
            }

            try {
                JdbcDao.executeByJdbc(connection, sql);
                break;
            } catch (Throwable e) {
                Timer.sleep(2000);
                continue;
            }
        }
        return sql;
    }

    public DatabaseTableDDL toDDL(Connection connection, DatabaseTable table) throws SQLException {
        String tableDDL = this.extractTableDDL(connection, table);
        if (StringUtils.isBlank(tableDDL)) {
            tableDDL = super.toDDL(connection, table).getTable();
        }

        // DDL
        StandardDatabaseTableDDL ddl = new StandardDatabaseTableDDL();
        ddl.setTable(tableDDL);

        // 索引
        DatabaseIndexList indexs = table.getIndexs();
        for (DatabaseIndex index : indexs) {
            ddl.getIndex().addAll(this.toDDL(connection, index, false));
        }

        // 主键
        DatabaseIndexList pks = table.getPrimaryIndexs();
        for (DatabaseIndex index : pks) {
            ddl.getPrimaryKey().addAll(this.toDDL(connection, index, true));
        }

        // 表说明
        if (StringUtils.isNotBlank(table.getRemark())) {
            String remark = SQL.escapeQuote(table.getRemark());
            ddl.getComment().add("COMMENT ON TABLE " + table.getFullName() + " IS '" + remark + "'");
        }

        // 字段说明
        DatabaseTableColumnList list = table.getColumns();
        for (int i = 0; i < list.size(); i++) {
            DatabaseTableColumn column = list.get(i);
            String remark = SQL.escapeQuote(column.getRemark());
            if (StringUtils.isNotBlank(remark)) {
                String escapeStr = StringUtils.replaceAll(remark, "'", "''"); // 转义
                ddl.getComment().add("COMMENT ON COLUMN " + table.getFullName() + "." + column.getName() + " IS '" + escapeStr + "'");
            }
        }
        return ddl;
    }

    /**
     * 从数据库中抽取指定表的建表语句
     *
     * @param connection 数据库连接
     * @param table      数据库表信息
     * @return 建表语句
     * @throws SQLException
     */
    protected String extractTableDDL(Connection connection, DatabaseTable table) throws SQLException {
        if (table == null) {
            throw new NullPointerException();
        }

        DatabaseMetaData metaData = connection.getMetaData();
        int major = metaData.getDatabaseMajorVersion();
        int minor = metaData.getDatabaseMinorVersion();

        // 读取 JDBC 配置信息
        DatabaseConfigurationContainer container = BeanFactory.get(DatabaseConfigurationContainer.class);
        DatabaseConfiguration config = container.get(connection);
        List<OSAccount> accounts = config.getAccounts();
        Ensure.isTrue(!accounts.isEmpty(), table);

        DatabaseURL url = Jdbc.parseJdbcUrl(connection);
        int port = Integer.parseInt(url.getPort());
        String name = url.getDatabaseName();

        // 生成实例
        OS os = BeanFactory.get(OS.class);
        try {
            OSAccount acct;
            DB2Instance inst = DB2Instance.get(os, port, name);
            if (inst == null) {
                Iterator<OSAccount> it = accounts.iterator();
                while (it.hasNext()) {
                    IO.closeQuiet(os);
                    IO.closeQuietly(os);
                    acct = it.next();

                    // 如果登陆操作系统发生错误，继续尝试下一个用户与密码
                    try {
                        os = BeanFactory.get(OS.class, config);
                    } catch (Throwable e) {
                        continue;
                    }

                    // 执行 db2look 命令
                    Ensure.isTrue(os.enableOSCommand(), url);
                    OSCommand cmd = os.getOSCommand();

                    // 执行 db2look 命令
                    DB2Command builder = BeanFactory.get(DB2Command.class, "db2", os.getName(), major, minor);
                    if (builder == null) {
                        throw new NullPointerException();
                    }

                    String oscommand = builder.getTableCommand(name, table.getSchema(), table.getName(), acct.getUsername(), acct.getPassword());
                    if (cmd.execute(oscommand) == 0) {
                        String tableDDL = cmd.getStdout();
                        int start = (int) Ensure.isPosition(SQL.indexOf(tableDDL, "create", 0, true));
                        int end = (int) Ensure.isPosition(SQL.indexOf(tableDDL, ";", start, false));
                        return tableDDL.substring(start, end); // create table 语句
                    }
                }
            }

            return null;
        } finally {
            os.close();
        }
    }

    protected StringBuilder toDDL(DatabaseTable table) {
        StringBuilder ddl = super.toDDL(table);
        DatabaseSpaceList tsl = table.getTableSpaces();
        if (tsl.size() > 0) {
            ddl.append(" IN ").append(tsl.get(0).getName());
        }

        DatabaseSpaceList isl = table.getIndexSpaces();
        if (isl.size() > 0) {
            ddl.append(" INDEX IN ").append(isl.get(0).getName());
        }
        return ddl;
    }

    public DatabaseDDL toDDL(Connection connection, DatabaseProcedure procedure) throws SQLException {
        StandardDatabaseDDL ddl = new StandardDatabaseDDL();
        JdbcQueryStatement dao = null;
        try { // 从数据库系统表中查询储存过程的源代码信息
            dao = new JdbcQueryStatement(connection, "select text from syscat.procedures where procschema=? and procname=? with ur");
            dao.setParameter(procedure.getSchema());
            dao.setParameter(procedure.getName());

            ResultSet resultSet = dao.query();
            if (resultSet.next()) {
                Clob value = resultSet.getClob("text");
                ddl.add(new ClobWriter(value).toString());
            }
            return ddl;
        } finally {
            dao.close();
        }
    }

    public List<DatabaseIndex> getIndexs(Connection connection, String catalog, String schema, String table, String indexName) throws SQLException {
        List<DatabaseIndex> indexs = super.getIndexs(connection, catalog, schema, table, indexName);
        this.setIndexSchema(connection, indexs);
        return indexs;
    }

    public List<DatabaseIndex> getIndexs(Connection connection, String catalog, String schema, String table) throws SQLException {
        List<DatabaseIndex> indexs = super.getIndexs(connection, catalog, schema, table);
        this.setIndexSchema(connection, indexs);
        return indexs;
    }

    /**
     * 查询索引归属的 schema
     *
     * @param connection
     * @param indexs
     * @throws SQLException
     */
    public void setIndexSchema(Connection connection, List<DatabaseIndex> indexs) throws SQLException {
        JdbcQueryStatement dao = new JdbcQueryStatement(connection, "select indschema from syscat.INDEXES where tabschema=? and tabname=? and indname=? with ur");
        for (DatabaseIndex idx : indexs) {
            StandardDatabaseIndex index = (StandardDatabaseIndex) idx;
            dao.setParameter(idx.getTableSchema());
            dao.setParameter(idx.getTableName());
            dao.setParameter(idx.getName());
            ResultSet result = dao.query();
            if (result.next()) {
                String indexSchema = StringUtils.trimBlank(result.getString("indschema")); // 索引模式
                if (StringUtils.isNotBlank(indexSchema)) {
                    index.setSchema(indexSchema);
                }
            }

            index.setFullName(this.toIndexName(null, idx.getSchema(), idx.getName()));
        }
        dao.close();
    }

    public List<DatabaseIndex> getPrimaryIndex(Connection connection, String catalog, String schema, String table) {
        return super.getPrimaryIndex(connection, catalog, schema, table);
    }

    public List<DatabaseTable> getTable(Connection connection, String catalog, String schema, String tableName) throws SQLException {
        DatabaseTypeSet types = Jdbc.getTypeInfo(connection);
        List<DatabaseTable> list = new ArrayList<DatabaseTable>();
        ResultSet resultSet = connection.getMetaData().getTables(catalog, schema, tableName, null);
        try {
            while (resultSet.next()) {
                StandardDatabaseTable table = new StandardDatabaseTable();
                table.setName(StringUtils.trim(resultSet.getString("TABLE_NAME")));
                table.setCatalog(StringUtils.trim(resultSet.getString("TABLE_CAT")));
                table.setSchema(StringUtils.trim(resultSet.getString("TABLE_SCHEM")));
                table.setType(StringUtils.trim(resultSet.getString("TABLE_TYPE")));
                table.setRemark(StringUtils.trim(resultSet.getString("REMARKS")));
                table.setFullName(this.toTableName(table.getCatalog(), table.getSchema(), table.getName()));

                // 索引信息
                List<DatabaseIndex> indexs = this.getIndexs(connection, catalog, table.getSchema(), table.getName());

                // 主键信息
                List<DatabaseIndex> pks = this.getPrimaryIndex(connection, catalog, table.getSchema(), table.getName());

                // 保存索引与主键
                Jdbc.removePrimaryKey(indexs, pks);
                table.setIndexs(indexs);
                table.setPrimaryIndexs(pks);

                // 列信息
                List<DatabaseTableColumn> columns = this.getTableColumns(connection, types, catalog, table.getSchema(), table.getName());
                table.setColumns(columns);

                List<Map<String, String>> list0 = JdbcDao.queryListMapsByJdbc(connection, "select tabname, tbspace, index_tbspace from syscat.tables a where tabschema='" + table.getSchema() + "' and tabname='" + table.getName() + "'");
                if (list0.size() != 1) {
                    String fullTablename = SQL.toTableName(table.getSchema(), table.getName());
                    throw new DatabaseException(ResourcesUtils.getDatabaseMessage(58, list0.size(), fullTablename));
                }

                Map<String, String> map = list0.get(0);

                // 保存表空间
                StandardDatabaseSpace tbspace = new StandardDatabaseSpace(StringUtils.rtrim(map.get("tbspace")));
                table.addTableSpace(tbspace);

                // 保存索引
                StandardDatabaseSpace idxspace = new StandardDatabaseSpace(StringUtils.rtrim(map.get("index_tbspace")));
                table.addIndexSpace(idxspace);

                list.add(table);
            }

            return list;
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    public String getCatalog(Connection connection) throws SQLException {
        return null;
    }

    public String getSchema(Connection conn) throws SQLException {
        return StringUtils.trimBlank((String) JdbcDao.queryFirstRowFirstColByJdbc(conn, "SELECT current_schema FROM SYSIBM.SYSDUMMY1"));
    }

    public void setSchema(Connection conn, String schema) throws SQLException {
        JdbcDao.executeByJdbc(conn, "set current schema " + schema);
    }

    public boolean supportSchema() {
        return true;
    }

    public int getRowNumberStarter() {
        return 1;
    }

    public String toDeleteQuicklySQL(Connection connection, String catalog, String schema, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException(tableName);
        } else {
            return "ALTER TABLE " + this.toTableName(catalog, schema, tableName) + " ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE";
        }
    }

    public List<DatabaseURL> parseJdbcUrl(String url) {
        StandardDatabaseURL obj = new StandardDatabaseURL(url);

        if (url.indexOf("/") == -1) {
            String[] array = StringUtils.split(url, ":");
            Ensure.isTrue(array.length == 3, url);
            obj.setDatabaseName(array[2]);
            obj.setDatabaseType(array[1]);
        } else {
            String[] array = StringUtils.split(url, "/");
            Ensure.isTrue(array.length == 4, url);

            /**
             * 解析第一部分
             */
            String[] head = StringUtils.split(array[0], ":");
            Ensure.isTrue(head.length == 3, url);
            obj.setDatabaseType(head[1]);

            /**
             * 第二部分
             */
            Ensure.isTrue(array[1].equals(""), url);

            /**
             * 解析第三部分
             */
            String[] body = StringUtils.split(array[2], ":");
            if (body.length == 1) {
                obj.setHostname(array[2]);
                obj.setPort("50000");
            } else if (body.length == 2) {
                obj.setHostname(body[0]);
                obj.setPort(body[1]);
            } else {
                throw new IllegalArgumentException(url + " error!");
            }

            /**
             * 解析第四部分
             */
            String[] attrs = StringUtils.split(array[3], ":");
            if (attrs.length > 1) {
                String[] params = StringUtils.removeBlank(StringUtils.split(attrs[1], ";"));
                for (String pmStr : params) {
                    String[] del4 = StringUtils.split(pmStr, "=");
                    if (del4.length == 2) {
                        obj.setAttribute(StringUtils.trim(del4[0]), del4[1]);
                    }
                }
                obj.setDatabaseName(attrs[0]);
            } else {
                obj.setDatabaseName(array[3]);
            }

            String currentSchema = obj.getAttribute("currentSchema");
            if (StringUtils.isNotBlank(currentSchema) && StringUtils.isBlank(obj.getSchema())) {
                obj.setSchema(currentSchema);
            }
        }

        List<DatabaseURL> list = new ArrayList<DatabaseURL>(1);
        list.add(obj);
        return list;
    }

    public List<DatabaseProcedure> getProcedure(Connection connection, String catalog, String schema, String procedureName) throws SQLException {
        schema = SQL.escapeQuote(schema);
        procedureName = SQL.escapeQuote(procedureName);

        String where = "";
        if (StringUtils.isNotBlank(schema)) {
            where += " and procschema='" + SQL.toIdentifier(schema) + "'";
        }
        if (StringUtils.isNotBlank(procedureName)) {
            if (procedureName.indexOf('%') != -1) {
                where += " and procname like '" + SQL.toIdentifier(procedureName) + "'";
            } else {
                where += " and procname = '" + SQL.toIdentifier(procedureName) + "'";
            }
        }

        DatabaseTypeSet map = Jdbc.getTypeInfo(connection);
        List<DatabaseProcedure> list = new ArrayList<DatabaseProcedure>();
        JdbcQueryStatement dao = new JdbcQueryStatement(connection, "select * from syscat.procedures where 1=1 " + where + " with ur");
        ResultSet resultSet = dao.query();
        while (resultSet.next()) {
            StandardDatabaseProcedure obj = new StandardDatabaseProcedure();
            obj.setId(StringUtils.rtrimBlank(resultSet.getString("PROCEDURE_ID")));
            obj.setCatalog(null);
            obj.setSchema(StringUtils.rtrimBlank(resultSet.getString("PROCSCHEMA")));
            obj.setName(StringUtils.rtrimBlank(resultSet.getString("PROCNAME")));
            obj.setFullName(this.toTableName(obj.getCatalog(), obj.getSchema(), obj.getName()));
            obj.setCreator(StringUtils.rtrimBlank(resultSet.getString("DEFINER")));
            obj.setCreatTime(resultSet.getDate("CREATE_TIME"));
            obj.setLanguage(StringUtils.rtrimBlank(resultSet.getString("LANGUAGE")));

            int count = 0;
            List<DatabaseProcedureParameter> parameters = new ArrayList<DatabaseProcedureParameter>(5);
            JdbcQueryStatement queryManager = new JdbcQueryStatement(connection, "SELECT * FROM SYSCAT.PROCPARMS WHERE procschema='" + obj.getSchema() + "' and procname = '" + obj.getName() + "' order by ORDINAL asc with ur");
            try {
                ResultSet resultSet2 = queryManager.query();
                while (resultSet2.next()) {
                    StandardDatabaseProcedureParameter parameter = new StandardDatabaseProcedureParameter();
                    parameter.setName(StringUtils.rtrimBlank(resultSet2.getString("PARMNAME")));
                    parameter.setProcedureSchema(obj.getSchema());
                    parameter.setProcedureName(obj.getName());
                    parameter.setPosition(resultSet2.getInt("ORDINAL"));

                    String mode = StringUtils.trimBlank(resultSet2.getString("PARM_MODE"));
                    if (mode.equals("IN")) {
                        parameter.setMode(DatabaseProcedure.PARAM_IN_MODE);
                    } else if (mode.equals("INOUT")) {
                        parameter.setMode(DatabaseProcedure.PARAM_INOUT_MODE);
                    } else if (mode.equals("OUT")) {
                        parameter.setMode(DatabaseProcedure.PARAM_OUT_MODE);
                    } else {
                        throw new UnsupportedOperationException(mode);
                    }
                    parameter.setCanNull("Y".equalsIgnoreCase(StringUtils.trimBlank(resultSet2.getString("NULLS"))));
                    parameter.setLength(resultSet2.getInt("LENGTH"));
                    parameter.setScale(resultSet2.getInt("SCALE"));
                    parameter.setFieldType(StringUtils.rtrimBlank(resultSet2.getString("TYPENAME")));
                    parameter.setSqlType(map.get(parameter.getFieldType()).getSqlType());

                    if (parameter.getMode() == DatabaseProcedure.PARAM_OUT_MODE || parameter.getMode() == DatabaseProcedure.PARAM_INOUT_MODE) {
                        parameter.setOutIndex(++count);
                    } else {
                        parameter.setOutIndex(0);
                    }

                    parameters.add(parameter);
                }
            } finally {
                queryManager.close();
            }

            obj.setParameters(parameters);
            list.add(obj);
        }
        dao.close();

        return list;
    }

    public DatabaseProcedure getProcedureForceOne(Connection connection, String catalog, String schema, String procedureName) throws SQLException {
        return super.getProcedureForceOne(connection, catalog, schema, procedureName);
    }

    public boolean isOverLengthException(Throwable e) {
        if (e instanceof SQLException) {
            SQLException sqlExp = (SQLException) e;
            while (sqlExp != null) {
                if (sqlExp.getErrorCode() == -302) {
                    // 发现输入主变量的值对于其在 SELECT、VALUES 或预编译语句中的使用而言太大
                    // 。发生了下列情况之一：
                    // * SQL 语句中使用的相应主变量或参数标记被定义为字符串，但是输入主变量包
                    // 含的字符串太长。
                    // * SQL 语句中使用的相应主变量或参数标记被定义为数字，但是输入主变量包含
                    // 的数值超出了范围。
                    // * C 语言以 NUL 终止的字符串主变量中丢失终止字符 NUL。
                    // * 联合系统用户：在传递会话中，可能违反了特定于数据源的限制。
                    return true;
                }

                sqlExp = sqlExp.getNextException();
            }
        }

        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        } else {
            return this.isOverLengthException(cause);
        }
    }

    public boolean isRebuildTableException(Throwable e) {
        if (e instanceof SQLException) {
            SQLException sqlExp = (SQLException) e;
            while (sqlExp != null) {
                if (sqlExp.getErrorCode() == -1477) {
                    // SQL1477N 对于表 "<表名>"，不能访问表空间 "<表空间标识>" 中的对
                    // 象 "<对象标识>"。
                    //
                    // 说明:
                    //
                    // 试图访问一个表，而该表的其中一个对象是不可访问的。由于下列原因之一，该
                    // 表可能不可访问：
                    // * 当回滚工作单元时，该表激活了 NOT LOGGED INITIALLY。
                    // * 该表是分区的已声明临时表，因为声明了临时表（所有已声明临时表都具有模
                    // 式名 SESSION），导致一个或多个分区失败。
                    // * ROLLFORWARD 在此表上遇到了 NOT LOGGED INITIALLY 激活，或者遇到了
                    // NONRECOVERABLE 装入。
                    //
                    // 不允许访问此表，因为不能保证其完整性。
                    return true;
                }

                sqlExp = sqlExp.getNextException();
            }
        }

        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        } else {
            return this.isRebuildTableException(cause);
        }
    }

    public boolean isPrimaryRepeatException(Throwable e) {
        if (e instanceof SQLException) {
            SQLException sqlExp = (SQLException) e;
            while (sqlExp != null) {
                if (sqlExp.getErrorCode() == -803) {
                    // SQL0803N INSERT 语句、UPDATE 语句或由 DELETE 语句导致的外键更
                    // 新中的一个或多个值无效，因为由 "<索引标识>" 标识的主键、唯一约束
                    // 或者唯一索引将表 "<表名>" 的索引键限制为不能具有重复值。
                    //
                    // 说明:
                    //
                    // INSERT 或 UPDATE 对象表 "<表名>" 被一个或多个 UNIQUE 索引约束为在某些列
                    // 或列组中具有唯一值。另外，父表上的 DELETE 语句导致更新从属表 "<表名>"（
                    // 该从属表受一个或多个 UNIQUE 索引约束）中的外键。唯一索引可能支持在表上
                    // 定义的主键或唯一约束。不能处理语句，因为完成所请求的 INSERT、UPDATE 或
                    // DELETE 语句将导致重复的列值。如果索引在 XML 列上，那么可以从单个 XML 文
                    // 档中生成索引键的重复值。
                    //
                    // 另外，如果视图是 INSERT 或 UPDATE 语句的对象，那么在其上定义视图的表
                    // "<表名>" 会受到约束。
                    //
                    // 如果 "<索引标识>" 为整数值，那么可以通过发出下列查询来从 SYSCAT.INDEXES
                    // 中获取索引名：
                    //
                    // SELECT INDNAME, INDSCHEMA
                    // FROM SYSCAT.INDEXES
                    // WHERE IID = <index-id>
                    // AND TABSCHEMA = 'schema'
                    // AND TABNAME = 'table'
                    //
                    // 其中，schema 表示 "<表名>" 的模式部分，table 表示 "<表名>" 的表名部分。
                    //
                    // 不能处理该语句。未更改表。
                    return true;
                }

                sqlExp = sqlExp.getNextException();
            }
        }

        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        } else {
            return this.isPrimaryRepeatException(cause);
        }
    }

    public boolean isIndexExistsException(Throwable e) {
        if (e instanceof SQLException) {
            SQLException sqlExp = (SQLException) e;
            while (sqlExp != null) {
                if (sqlExp.getErrorCode() == -601) {
                    // SQL0601N 要创建的对象的名称与类型为 "<类型>" 的现有的名称 "<名
                    // 称>" 相同。
                    //
                    // 说明:
                    //
                    // CREATE 或 ALTER 语句尝试创建或添加对象 "<名称>"，但应用程序服务器上或同
                    // 一语句中已存在类型为 "<类型>" 的该名称的对象。
                    //
                    // 如果 "<类型>" 是 FOREIGN KEY、PRIMARY KEY、UNIQUE 或 CHECK CONSTRAINT，
                    // 那么 "<名称>" 是 ALTER NICKNAME、ALTER TABLE、CREATE NICKNAME 或 CREATE
                    // TABLE 语句中指定的或由系统生成的约束名。
                    //
                    // 如果 "<类型>" 为 ROLE，那么该名称是在 CREATE 或 ALTER ROLE 语句中指定的
                    // 角色名。
                    //
                    // 如果 "<类型>" 是 DATA PARTITION，那么 "<名称>" 是在 ALTER TABLE 或
                    // CREATE TABLE 语句中指定的数据分区名。
                    //
                    // 当使用 REGISTER 命令或者 XSR_REGISTER、XSR_DTD 或 XSR_EXTENTITY 这三个
                    // 过程之一来注册 XML 模式存储库对象时也可能会产生此错误。当 XSROBJECT 的
                    // 名称已存在时出错。
                    //
                    // 联合系统用户：某些数据源未向 "<名称>" 和 "<类型>" 消息标记提供适当的值
                    // 。在这些情况下，"<名称>" 和 "<类型>" 将具有以下格式："对象:<数据源>表/
                    // 视图"和"未知"，指示指定的数据源处的实际值未知。
                    //
                    // 不能处理该语句。未创建任何新对象，且未改变或修改现有的对象。
                    return true;
                }

                sqlExp = sqlExp.getNextException();
            }
        }

        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        } else {
            return this.isIndexExistsException(cause);
        }
    }

    public void reorgRunstatsIndexs(Connection conn, List<DatabaseIndex> indexs) throws SQLException {
        if (indexs == null || indexs.size() == 0) {
            return;
        }

        String fullTableName = null;
        HashSet<String> names = new HashSet<String>();
        for (DatabaseIndex index : indexs) {
            if (index != null) {
                fullTableName = index.getTableFullName();
                names.addAll(index.getColumnNames());
            }
        }

        if (StringUtils.isBlank(Jdbc.getSchema(fullTableName))) {
            throw new DatabaseException(ResourcesUtils.getDatabaseMessage(54, fullTableName));
        }

        JdbcDao dao = new JdbcDao(conn);
        if (StringUtils.isNotBlank(fullTableName) && names.size() > 0) {
            dao.callProcedureByJdbc("call SYSPROC.ADMIN_CMD('RUNSTATS ON TABLE " + fullTableName + " ON COLUMNS (" + StringUtils.join(names, ", ") + ") WITH DISTRIBUTION ON KEY COLUMNS')");
        }

        if (StringUtils.isNotBlank(fullTableName)) {
            dao.callProcedureByJdbc("call SYSPROC.ADMIN_CMD('reorg indexes all for table " + fullTableName + " ALLOW READ ACCESS')");
        }

        if (StringUtils.isNotBlank(fullTableName) && names.size() > 0) {
            dao.callProcedureByJdbc("call SYSPROC.ADMIN_CMD('RUNSTATS ON TABLE " + fullTableName + " ON COLUMNS (" + StringUtils.join(names, ", ") + ") WITH DISTRIBUTION ON KEY COLUMNS')");
        }
    }

    public boolean terminate(Connection conn, Properties config) throws SQLException {
        String applicationid = config.getProperty(DB2Dialect.APPLICATION_ID);
        if (StringUtils.isBlank(applicationid)) {
            return false;
        }

        String applicationHandle = config.getProperty(DB2Dialect.APPLICATION_HANDLE);
        DatabaseConfigurationContainer container = BeanFactory.get(DatabaseConfigurationContainer.class);
        DatabaseConfiguration jdbc = container.get(conn);
        if (jdbc == null) {
            throw new NullPointerException();
        }

        try {
            OSAccount sshacct = jdbc.getSSHAccount();
            if (sshacct != null) { // 优先使用 ssh 账户登录
                String username = sshacct.getUsername();
                String password = sshacct.getPassword();

                if (StringUtils.isBlank(applicationHandle)) {
                    applicationHandle = this.getApplicationHandle(conn, jdbc, applicationid);
                }

                if (StringUtils.isNotBlank(applicationHandle) && this.forceApplication(conn, username, password, applicationHandle)) {
                    STD.out.debug(ResourcesUtils.getDatabaseMessage(55, applicationHandle));
                    return true;
                }
            }

            for (Iterator<OSAccount> it = jdbc.getAccounts().iterator(); it.hasNext(); ) {
                OSAccount acct = it.next();
                String username = acct.getUsername();
                String password = acct.getPassword();

                if (StringUtils.isBlank(applicationHandle)) {
                    applicationHandle = this.getApplicationHandle(conn, jdbc, applicationid);
                }

                if (StringUtils.isNotBlank(applicationHandle) && this.forceApplication(conn, username, password, applicationHandle)) {
                    STD.out.debug(ResourcesUtils.getDatabaseMessage(55, applicationHandle));
                    return true;
                }
            }
            STD.out.warn(ResourcesUtils.getDatabaseMessage(56, applicationHandle));
            return false;
        } catch (SQLException e) {
            STD.out.error(ResourcesUtils.getDatabaseMessage(56, applicationHandle), e);
            return false;
        }
    }

    public Properties getAttributes(Connection conn) {
        Properties config = new Properties();
        try {
            List<Map<String, String>> list = JdbcDao.queryListMapsByJdbc(conn, "select mon_get_application_id() as application_id, mon_get_application_handle() as application_handle from sysibm.dual with ur");
            if (list.size() > 0) {
                Map<String, String> map = list.get(0);
                String applicationId = map.get("application_id");
                String applicationHandle = map.get("application_handle");
                config.put(DB2Dialect.APPLICATION_ID, applicationId);
                config.put(DB2Dialect.APPLICATION_HANDLE, applicationHandle);
            }
//			Jdbc.commit(conn);
        } catch (Throwable e) {
            Connection obj = conn;
            if (conn instanceof PoolConnection) {
                obj = ((PoolConnection) conn).getConnection();
            }

            Method method = ClassUtils.getMethod(obj, "getDB2Correlator");
            if (method != null) {
                String applicationId = StringUtils.toString(ClassUtils.executeMethod(obj, "getDB2Correlator"));
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug(ResourcesUtils.getDatabaseMessage(57, obj, applicationId));
                }
                config.put(DB2Dialect.APPLICATION_ID, applicationId);
            }
        }
        return config;
    }

    /**
     * 根据 db2 application id 查找对应的 application handle
     */
    public String getApplicationHandle(Connection conn, DatabaseConfiguration config, String applicationid) throws SQLException, OSCommandException {
        DatabaseMetaData metaData = conn.getMetaData();
        int major = metaData.getDatabaseMajorVersion();
        int minor = metaData.getDatabaseMinorVersion();

        OS os = BeanFactory.get(OS.class, config);
        try {
            DB2Command builder = BeanFactory.get(DB2Command.class, "db2", os.getName(), major, minor);
            if (builder == null) {
                throw new NullPointerException();
            }

            String command = builder.getApplicationDetail(applicationid);
            Ensure.isTrue(os.enableOSCommand(), os.getName(), major, minor);
            OSCommand cmd = os.getOSCommand();
            if (cmd.execute(command) != 0) {
                return null;
            }

            BufferedLineReader in = new BufferedLineReader(StringUtils.trimBlank(cmd.getStdout()));
            try {
                while (in.hasNext()) {
                    String line = in.next();

                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug(line);
                    }

                    String[] array = StringUtils.splitByBlank(line);
                    if (array.length >= 3 && array[3].equals(applicationid)) {
                        String number = array[2];
                        if (StringUtils.isInt(number)) {
                            return number;
                        }
                    }
                }
            } finally {
                IO.close(in);
            }

            STD.out.error(cmd.getStderr());
            return null;
        } finally {
            os.close();
        }
    }

    /**
     * 强制关闭db2进程
     */
    public boolean forceApplication(Connection conn, String username, String password, String applicationHandle) throws SQLException {
        JdbcDao dao = new JdbcDao();
        try {
            String url = conn.getMetaData().getURL();
            if (dao.connect(url, -1, username, password)) {
                dao.callProcedureByJdbc("call SYSPROC.ADMIN_CMD('force application (" + applicationHandle + ")')");
                dao.commit();
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            dao.rollbackQuiet();
            STD.out.error(applicationHandle, e);
            return false;
        } finally {
            dao.rollbackQuiet();
            dao.commitQuiet();
            dao.close();
        }
    }

    public String getKeepAliveSQL() {
        return "select 1 from sysibm.dual";
    }

    public void openLoadMode(JdbcDao dao, String fullTableName) throws SQLException {
        dao.setAutoCommit(false); // 关闭自动提交事物日志
        dao.reduceIsolation(); // 尽量降低事物隔离级别
        dao.executeByJdbc("ALTER TABLE " + fullTableName + " LOCKSIZE TABLE"); // 修改表的锁为表锁
        // LOCK TABLE [TableName] IN [SHARE | EXCLUSIVE] MODE

        dao.executeByJdbc("ALTER TABLE " + fullTableName + " APPEND ON"); // 新添加的数据将会插入到有空闲空间的页中，不再搜索空闲空间，但需要 reorg table
        dao.executeByJdbc("ALTER TABLE " + fullTableName + " ACTIVATE NOT LOGGED INITIALLY"); // 关闭表上的事物日志（操作只在事物中有效, 事务提交后自动失效）
    }

    public void commitLoadData(JdbcDao dao, String fullTableName) throws SQLException {
        dao.commit();
        dao.executeByJdbc("ALTER TABLE " + fullTableName + " ACTIVATE NOT LOGGED INITIALLY"); // 提交事务后需要重新打开
    }

    public void closeLoadMode(JdbcDao dao, String fullTableName) throws SQLException {
        dao.executeByJdbc("ALTER TABLE " + fullTableName + " LOCKSIZE ROW"); // 执行完批量插入后恢复表的锁级别
        dao.executeByJdbc("ALTER TABLE " + fullTableName + " APPEND OFF"); // 新增加的数据存放到最后一个页上，若该页存放满了，则数据将会存放到下一个页上。
        dao.commit();
    }

    public JdbcConverterMapper getObjectConverters() {
        if (this.exp == null) {
            this.exp = new StandardJdbcConverterMapper();
            this.exp.add("CHAR", StringConverter.class);
            this.exp.add("VARCHAR", StringConverter.class);
            this.exp.add("LONG VARCHAR", StringConverter.class);
            this.exp.add("GRAPHIC", StringConverter.class);
            this.exp.add("VARGRAPHIC", StringConverter.class);
            this.exp.add("LONG VARGRAPHIC", StringConverter.class);
            this.exp.add("VARCHAR FOR BIT DATA", ByteArrayConverter.class);
            this.exp.add("LONG VARCHAR FOR BIT DATA", ByteArrayConverter.class);
            this.exp.add("SMALLINT", IntegerConverter.class);
            this.exp.add("BIGINT", LongConverter.class);
            this.exp.add("INTEGER", IntegerConverter.class);
            this.exp.add("REAL", RealConverter.class);
            this.exp.add("DOUBLE", DoubleConverter.class);
            this.exp.add("DATE", DateConverter.class, AbstractConverter.PARAM_DATEFORMAT, "yyyyMMdd");
            this.exp.add("TIME", TimeConverter.class, AbstractConverter.PARAM_TIMEFORMAT, "hh.mm.ss");
            this.exp.add("TIMESTAMP", TimestampConverter.class, AbstractConverter.PARAM_TIMESTAMPFORMAT, "yyyy-MM-dd-HH.mm.ss.SSSSSS");
            this.exp.add("DECIMAL", BigDecimalConverter.class);
            this.exp.add("BLOB", BlobConverter.class);
            this.exp.add("CLOB", ClobConverter.class);
            this.exp.add("DBCLOB", ClobConverter.class);
        }
        return this.exp;
    }

    public JdbcConverterMapper getStringConverters() {
        if (this.map == null) {
            this.map = new StandardJdbcConverterMapper();
            this.map.add("CHAR", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("VARCHAR", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("LONG VARCHAR", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("GRAPHIC", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("VARGRAPHIC", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("LONG VARGRAPHIC", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("VARCHAR FOR BIT DATA", icu.etl.database.load.converter.BlobConverter.class);
            this.map.add("LONG VARCHAR FOR BIT DATA", icu.etl.database.load.converter.BlobConverter.class);
            this.map.add("SMALLINT", icu.etl.database.load.converter.IntegerConverter.class);
            this.map.add("BIGINT", icu.etl.database.load.converter.LongConverter.class);
            this.map.add("INTEGER", icu.etl.database.load.converter.IntegerConverter.class);
            this.map.add("REAL", icu.etl.database.load.converter.DoubleConverter.class);
            this.map.add("DOUBLE", icu.etl.database.load.converter.DoubleConverter.class);
            this.map.add("DATE", icu.etl.database.load.converter.DateConverter.class, AbstractConverter.PARAM_DATEFORMAT, "yyyyMMdd");
            this.map.add("TIME", icu.etl.database.load.converter.TimeConverter.class, AbstractConverter.PARAM_TIMEFORMAT, "hh.mm.ss");
            this.map.add("TIMESTAMP", icu.etl.database.load.converter.TimestampConverter.class, AbstractConverter.PARAM_TIMESTAMPFORMAT, "yyyy-MM-dd-HH.mm.ss.SSSSSS");
            this.map.add("DECIMAL", icu.etl.database.load.converter.BigDecimalConverter.class);
            this.map.add("BLOB", icu.etl.database.load.converter.BlobConverter.class);
            this.map.add("CLOB", icu.etl.database.db2.recconv.StringConverter.class);
            this.map.add("DBCLOB", icu.etl.database.db2.recconv.StringConverter.class);
        }
        return this.map;
    }

    public boolean supportedMergeStatement() {
        return true;
    }

    public String toMergeStatement(String tableName, List<DatabaseTableColumn> columns, List<String> mergeColumn) {
        String sql = "";

        /**
         * 按如下规则拼sql语句: <br>
         * MERGE INTO XCMDTRANSFERSTATE AS T <br>
         * USING TABLE (VALUES(?,?,?,?,?,?)) <br>
         * T1(DATASNO,EXTERIORSYSTEM,STATUS,DATA_TIME,MARKFORDELETE,DATATRANSFERNO) <br>
         * ON (T.DATATRANSFERNO = T1.DATATRANSFERNO) <br>
         * WHEN MATCHED THEN update set T.DATASNO = T1.DATASNO,T.EXTERIORSYSTEM = <br>
         * T1.EXTERIORSYSTEM,T.STATUS = T1.STATUS,T.LASTUP_TIME = T1.DATA_TIME,T.MARKFORDELETE = T1.MARKFORDELETE <br>
         * WHEN NOT MATCHED THEN INSERT <br>
         * (DATASNO,EXTERIORSYSTEM,STATUS,CREATE_TIME,MARKFORDELETE,DATATRANSFERNO) VALUES <br>
         * (T1.DATASNO,T1.EXTERIORSYSTEM,T1.STATUS,T1.DATA_TIME,T1.MARKFORDELETE,T1.DATATRANSFERNO)<br>
         */
        sql += "merge into " + tableName + " as T " + FileUtils.lineSeparator;
        sql += " using table (values(";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            it.next();
            sql += "?";
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ")) T1(";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            sql += it.next().getName();
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ") " + FileUtils.lineSeparator;

        // 唯一索引字段
        sql += " on (";
        for (Iterator<String> it = mergeColumn.iterator(); it.hasNext(); ) {
            String name = it.next();
            sql += "T." + name + " = T1." + name;
            if (it.hasNext()) {
                sql += " and ";
            }
        }
        sql += ")" + FileUtils.lineSeparator;

        // 唯一索引匹配时更新字段值
        sql += " when matched then update set " + FileUtils.lineSeparator;
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            String name = it.next().getName();
            sql += "T." + name + " = T1." + name + FileUtils.lineSeparator;
            if (it.hasNext()) {
                sql += ", ";
            }
        }

        // 唯一索引不匹配时，插入记录
        sql += " when not matched then insert (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            sql += it.next().getName();
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ") values (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            sql += "T1." + it.next().getName();
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ")";
        return sql;
    }

    public List<String> alterTableColumn(Connection connection, DatabaseTableColumn oc, DatabaseTableColumn nc) throws SQLException {
        if (oc == null && nc == null) {
            throw new NullPointerException();
        }

        List<String> list = new ArrayList<String>();
        JdbcDao dao = new JdbcDao(connection);
        try {
            // 添加数据库表字段
            if (oc == null) {
                String sql = "alter table " + nc.getTableFullName() + " add " + nc.getName() + " " + nc.getFieldName();

                // 添加默认值
                if (StringUtils.isNotBlank(nc.getDefault())) {
                    sql += " default " + nc.getDefaultValue();
                }

                list.add(sql.toString());

                // not null 语句
                if ("no".equalsIgnoreCase(nc.getNullAble())) {
                    list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " set not null");
                }

                dao.executeByJdbc(list);
                dao.callProcedureByJdbc("call sysproc.admin_cmd('reorg table " + nc.getTableFullName() + "');");
                return list;
            }

            // 删除字段
            if (nc == null) {
                list.add("alter table " + oc.getTableFullName() + " drop column " + oc.getName());
                dao.executeByJdbc(list);
                dao.callProcedureByJdbc("call sysproc.admin_cmd('reorg table " + oc.getTableFullName() + "');");
                return list;
            }

            // 修改字段
            if (StringComparator.compareIgnoreBlank(oc.getTableCatalog(), nc.getTableCatalog()) != 0 //
                    || StringComparator.compareIgnoreBlank(oc.getTableName(), nc.getTableName()) != 0 //
                    || StringComparator.compareIgnoreBlank(oc.getTableSchema(), nc.getTableSchema()) != 0 //
                    || StringComparator.compareIgnoreBlank(oc.getTableFullName(), nc.getTableFullName()) != 0 //
            ) {
                throw new SQLException(oc.getTableFullName() + " != " + nc.getTableFullName());
            }

            // 修改字段名
            if (!oc.getName().equalsIgnoreCase(nc.getName())) {
                list.addAll(this.alterTableColumn(connection, oc, null));
                list.addAll(this.alterTableColumn(connection, null, nc));
                dao.executeByJdbc(list);
                dao.callProcedureByJdbc("call sysproc.admin_cmd('reorg table " + nc.getTableFullName() + "');");
                return list;
            }

            // 字段类型发生变化
            if (StringComparator.compareIgnoreBlank(oc.getFieldType(), nc.getFieldType()) != 0) {
                list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " set data type " + nc.getFieldName());
            }

            // 修改字段类型
            else if (oc.length() != nc.length() || oc.getDigit() != nc.getDigit()) {
                list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " set data type " + nc.getFieldName());
            }

            // 修改字段自增信息
            if (StringComparator.compareIgnoreBlank(oc.getIncrement(), nc.getIncrement()) != 0) {
                throw new UnsupportedOperationException();
            }

            // 设置字段 not null 属性
            if (StringComparator.compareIgnoreBlank(nc.getNullAble(), oc.getNullAble()) != 0) {
                if ("no".equalsIgnoreCase(nc.getNullAble())) {
                    list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " set not null");
                } else if ("no".equalsIgnoreCase(oc.getNullAble())) {
                    list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " drop not null");
                }
            }

            // 设置字段默认值
            if (StringComparator.compareIgnoreBlank(oc.getDefault(), nc.getDefault()) != 0) {
                if (StringUtils.isBlank(nc.getDefault())) {
                    list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " drop default");
                } else {
                    list.add("alter table " + nc.getTableFullName() + " alter column " + nc.getName() + " set default " + nc.getDefaultValue());
                }
            }

            // 设置字段注释
            if (StringComparator.compareIgnoreBlank(oc.getRemark(), nc.getRemark()) != 0 && StringUtils.isNotBlank(nc.getRemark())) {
                list.add("comment on column " + nc.getTableFullName() + " is '" + nc.getRemark() + "'");
            }

            dao.executeByJdbc(list);
            dao.callProcedureByJdbc("call sysproc.admin_cmd('reorg table " + nc.getTableFullName() + "');");
            return list;
        } finally {
            dao.setConnection(null);
        }
    }

}