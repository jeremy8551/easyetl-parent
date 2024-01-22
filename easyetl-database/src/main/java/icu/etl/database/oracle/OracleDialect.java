package icu.etl.database.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import icu.etl.annotation.EasyBean;
import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseException;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTypeSet;
import icu.etl.database.DatabaseURL;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.database.SQL;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.StandardDatabaseDDL;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.internal.StandardDatabaseProcedureParameter;
import icu.etl.database.internal.StandardDatabaseURL;
import icu.etl.io.ClobWriter;
import icu.etl.os.OSConnectCommand;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.Property;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 关于Oracle数据库的 icu.etl.db.dialect.DatabaseDialect 数据库方言接口实现类
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "oracle")
public class OracleDialect extends AbstractDialect {

    public OracleDialect() {
        super();

        this.keyword.add("ACCESS");
        this.keyword.add("ADD");
        this.keyword.add("ALL");
        this.keyword.add("ALTER");
        this.keyword.add("AND");
        this.keyword.add("ANY");
        this.keyword.add("AS");
        this.keyword.add("ASC");
        this.keyword.add("AUDIT");
        this.keyword.add("BETWEEN");
        this.keyword.add("BY");
        this.keyword.add("CHAR");
        this.keyword.add("CHECK");
        this.keyword.add("CLUSTER");
        this.keyword.add("COLUMN");
        this.keyword.add("COMMENT");
        this.keyword.add("COMPRESS");
        this.keyword.add("CONNECT");
        this.keyword.add("CREATE");
        this.keyword.add("CURRENT");
        this.keyword.add("DATE");
        this.keyword.add("DECIMAL");
        this.keyword.add("DEFAULT");
        this.keyword.add("DELETE");
        this.keyword.add("DESC");
        this.keyword.add("DISTINCT");
        this.keyword.add("DROP");
        this.keyword.add("ELSE");
        this.keyword.add("EXCLUSIVE");
        this.keyword.add("EXISTS");
        this.keyword.add("FILE");
        this.keyword.add("FLOAT");
        this.keyword.add("FOR");
        this.keyword.add("FROM");
        this.keyword.add("GRANT");
        this.keyword.add("GROUP");
        this.keyword.add("HAVING");
        this.keyword.add("IDENTIFIED");
        this.keyword.add("IMMEDIATE");
        this.keyword.add("IN");
        this.keyword.add("INCREMENT");
        this.keyword.add("INDEX");
        this.keyword.add("INITIAL");
        this.keyword.add("INSERT");
        this.keyword.add("INTEGER");
        this.keyword.add("INTERSECT");
        this.keyword.add("INTO");
        this.keyword.add("IS");
        this.keyword.add("LEVEL");
        this.keyword.add("LIKE");
        this.keyword.add("LOCK");
        this.keyword.add("LONG");
        this.keyword.add("MAXEXTENTS");
        this.keyword.add("MINUS");
        this.keyword.add("MLSLABEL");
        this.keyword.add("MODE");
        this.keyword.add("MODIFY");
        this.keyword.add("NOAUDIT");
        this.keyword.add("NOCOMPRESS");
        this.keyword.add("NOT");
        this.keyword.add("NOWAIT");
        this.keyword.add("NULL");
        this.keyword.add("NUMBER");
        this.keyword.add("OF");
        this.keyword.add("OFFLINE");
        this.keyword.add("ON");
        this.keyword.add("ONLINE");
        this.keyword.add("OPTION");
        this.keyword.add("OR");
        this.keyword.add("ORDER");
        this.keyword.add("P");
        this.keyword.add("CTFREE");
        this.keyword.add("PRIOR");
        this.keyword.add("PRIVILEGES");
        this.keyword.add("PUBLIC");
        this.keyword.add("RAW");
        this.keyword.add("RENAME");
        this.keyword.add("RESOURCE");
        this.keyword.add("REVOKE");
        this.keyword.add("ROW");
        this.keyword.add("ROWID");
        this.keyword.add("ROWNUM");
        this.keyword.add("ROWS");
        this.keyword.add("SELECT");
        this.keyword.add("SESSION");
        this.keyword.add("SET");
        this.keyword.add("SHARE");
        this.keyword.add("SIZE");
        this.keyword.add("SMALLINT");
        this.keyword.add("START");
        this.keyword.add("SUCCESSFUL");
        this.keyword.add("SYNONYM");
        this.keyword.add("SYSDATE");
        this.keyword.add("TABLE");
        this.keyword.add("THEN");
        this.keyword.add("TO");
        this.keyword.add("TRIGGER");
        this.keyword.add("UID");
        this.keyword.add("UNION");
        this.keyword.add("UNIQUE");
        this.keyword.add("UPDATE");
        this.keyword.add("USER");
        this.keyword.add("VALIDATE");
        this.keyword.add("VALUES");
        this.keyword.add("VARCHAR");
        this.keyword.add("VARCHAR2");
        this.keyword.add("VIEW");
        this.keyword.add("WHENEVER");
        this.keyword.add("WHERE");
        this.keyword.add("WITH");
    }

    public String getSchema(Connection conn) throws SQLException {
        return super.getSchema(conn); // TODO
    }

    public String getCatalog(Connection connection) throws SQLException {
        return null;
    }

    public void setSchema(Connection conn, String schema) throws SQLException {
        JdbcDao.execute(conn, "alter session set current_schema=" + schema);
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
            return "truncate table " + this.toTableName(catalog, schema, tableName);
        }
    }

    /**
     * 解析oracle 数据库jdbc url 字符串 <br>
     * 1.普通SID方式 <br>
     * jdbc:oracle:thin:username/password@x.x.x.1:1521:SID <br>
     * 2.普通ServerName方式 <br>
     * jdbc:Oracle:thin:username/password@//x.x.x.1:1522/ABCD <br>
     * 3.RAC方式 <br>
     * jdbc:oracle:thin:@(description=(address_list= (address=(host=rac1) (protocol=tcp1)(port=1521))(address=(host=rac2)(protocol=tcp2) (port=1522)) (load_balance=yes)(failover=yes))(connect_data=(SERVER=DEDICATED)(service_name= oratest)))
     */
    @SuppressWarnings("unchecked")
    public List<DatabaseURL> parseJdbcUrl(String url) {
        int index = (int) Ensure.isFromZero(url.indexOf("@"));
        List<DatabaseURL> list = new ArrayList<DatabaseURL>(1);
        StandardDatabaseURL obj = new StandardDatabaseURL(url);

        /**
         * 第一部分 <br>
         * jdbc:oracle:thin:[user/pass]@ip:port:sid <br>
         * jdbc:oracle:thin:[user/pass]@ip:port/servername <br>
         */
        String prefix = url.substring(0, index);
        String[] part1 = StringUtils.split(prefix, ":");
        Ensure.isTrue(part1.length == 4, url);
        obj.setDatabaseType(part1[1]);
        obj.setDriverType(part1[2]);

        if (!part1[3].equals("")) {
            String[] array = StringUtils.split(part1[3], "/");
            Ensure.isTrue(array.length == 2, url);
            obj.setUsername(array[0]);
            obj.setPassword(array[1]);
        }

        /**
         * 解析第二部分
         */
        String endfix = url.substring(index + 1);
        String[] part2 = StringUtils.split(endfix, ":");
        if (part2.length == 2) { // ip:sid
            String[] a = StringUtils.split(endfix, "/");
            if (a.length == 1) { // ip:port
                obj.setHostname(part2[0]);
                obj.setPort("1521");
                obj.setSID(part2[1]);
                obj.setServerName(part2[1]);
                obj.setDatabaseName(part2[1]);
            } else { // ip/name
                String[] b = StringUtils.split(a[0], ":");
                obj.setHostname(b[0]);
                obj.setPort(b[1]);
                obj.setSID(a[1]);
                obj.setServerName(a[1]);
                obj.setDatabaseName(a[1]);
            }
        } else if (part2.length == 3) { // ip:port:sid
            obj.setHostname(part2[0]);
            obj.setPort(part2[1]);
            obj.setSID(part2[2]);
            obj.setServerName(part2[2]);
            obj.setDatabaseName(part2[2]);
        } else {
            Ensure.isFromZero(endfix.toUpperCase().indexOf("DESCRIPTION"));

            /**
             * rac 集群配置 <br>
             * (description= <br>
             * (address_list= <br>
             * (address=(host=rac1) (protocol=tcp) (port=1521)) <br>
             * (address=(host=rac2) (protocol=tcp) (port=1521)) <br>
             * (load_balance=yes)(failover=yes) <br>
             * ) <br>
             * (connect_data=(SERVER=DEDICATED)(service_name=orctest) ) <br>
             * ) <br>
             */
            List<Property> attributes = this.oracleRacUrlResolve(url.substring(index + 1));
            Ensure.isTrue(attributes.size() == 1, url);

            list.clear();
            Property root = attributes.get(0);
            List<Property> tree2 = (List<Property>) root.getValue();
            setPropertiesValue(obj, tree2);
            List<Properties> l = getPropertiesList(tree2);
            for (Properties p : l) {
                StandardDatabaseURL copy = obj.clone();
                for (Iterator<Object> it = p.keySet().iterator(); it.hasNext(); ) {
                    String key = StringUtils.objToStr(it.next());
                    String value = StringUtils.objToStr(p.get(key));

                    if (OSConnectCommand.host.equalsIgnoreCase(key)) {
                        copy.setHostname(value);
                    } else if (OSConnectCommand.port.equalsIgnoreCase(key)) {
                        copy.setPort(value);
                    } else {
                        copy.setAttribute(key, value);
                    }
                }
                list.add(copy);
            }
            return list;
        }

        list.add(obj);
        return list;
    }

    @SuppressWarnings("unchecked")
    static List<Properties> getPropertiesList(List<Property> list) {
        List<Properties> result = new ArrayList<Properties>();
        for (Property attr : list) {
            if (attr.getKey().trim().equalsIgnoreCase("address_list")) {
                List<Property> tree3 = (List<Property>) attr.getValue();
                for (Property kv3 : tree3) {
                    if (kv3.getKey().trim().equalsIgnoreCase("address")) {
                        Properties cfg = new Properties();
                        List<Property> addresses = (List<Property>) kv3.getValue();
                        for (Property cattr : addresses) {
                            cfg.setProperty(cattr.getKey(), cattr.getString());
                        }
                        result.add(cfg);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static void setPropertiesValue(StandardDatabaseURL url, List<Property> list) {
        for (Property attr : list) {
            Object value = attr.getValue();
            if (value instanceof List && !((List<?>) value).isEmpty() && (((List<?>) value).get(0) instanceof Property)) {
                setPropertiesValue(url, (List<Property>) value);
            } else {
                url.setAttribute(attr.getKey(), attr.getString());
            }
        }
    }

    public List<DatabaseProcedure> getProcedure(Connection conn, String catalog, String schema, String procedureName) throws SQLException {
        schema = SQL.escapeQuote(schema);
        procedureName = SQL.escapeQuote(procedureName);
        List<DatabaseProcedure> list = new ArrayList<DatabaseProcedure>();

        String where = "";
        if (StringUtils.isNotBlank(schema)) {
            where += " and OWNER='" + SQL.toIdentifier(schema) + "'";
        }
        if (StringUtils.isNotBlank(procedureName)) {
            if (procedureName.indexOf('%') != -1) {
                where += " and OBJECT_NAME like '" + SQL.toIdentifier(procedureName) + "'";
            } else {
                where += " and OBJECT_NAME = '" + SQL.toIdentifier(procedureName) + "'";
            }
        }

        String sql = "select a.* from all_objects a where 1=1 " + where;
        JdbcQueryStatement query = new JdbcQueryStatement(conn, sql);
        ResultSet resultSet = query.query();
        while (resultSet.next()) {
            StandardDatabaseProcedure obj = new StandardDatabaseProcedure();
            obj.setId(StringUtils.rtrimBlank(resultSet.getString("OBJECT_ID")));
            obj.setCatalog(null);
            obj.setSchema(StringUtils.rtrimBlank(resultSet.getString("OWNER")));
            obj.setName(StringUtils.rtrimBlank(resultSet.getString("OBJECT_NAME")));
            obj.setFullName(this.toTableName(obj.getCatalog(), obj.getSchema(), obj.getName()));
            obj.setCreator(StringUtils.rtrimBlank(resultSet.getString("OWNER")));
            obj.setCreatTime(resultSet.getDate("CREATED"));
            obj.setLanguage("SQL");

            /**
             * 查询存储过程ddl语句
             */
            JdbcQueryStatement ddl = new JdbcQueryStatement(conn, "select dbms_metadata.get_ddl('PROCEDURE', '" + obj.getName() + "') as TEXT from dual ");
            ResultSet ddlRes = ddl.query();
            Ensure.isTrue(ddlRes.next());
            String text = new ClobWriter(ddlRes.getClob("TEXT")).toString();
            ddl.close();

            obj.setParameters(this.resolveOracleDatabaseProcedureParameter(conn, obj, text));
            list.add(obj);
        }
        query.close();

        return list;
    }

    public DatabaseProcedure getProcedureForceOne(Connection connection, String catalog, String schema, String procedureName) throws SQLException {
        return super.getProcedureForceOne(connection, catalog, schema, procedureName);
    }

//	public boolean supportAlterTableNotLog() {
//		return true;
//	}
//	
//	public String toAlterTableNotLogSQL(String catalog, String schema, String tableName) {
//		return "ALTER TABLE " + this.toTableName(catalog, schema, tableName) + " NOLOGGING";
//	}
//	
//	public String toAlterTableLogSQL(String catalog, String schema, String tableName) {
//		return "ALTER TABLE " + this.toTableName(catalog, schema, tableName) + " LOGGING";
//	}
//	
//	public boolean supportAlterTableLog() {
//		return true;
//	}

    /**
     * 解析oracle rac url 配置
     *
     * @param str url字符串 <br>
     *            如: <br>
     *            (description= <br>
     *            &nbsp;(address_list= <br>
     *            &nbsp;&nbsp;&nbsp;(address=(host=rac1) (protocol=tcp) (port=1521)) <br>
     *            &nbsp;&nbsp;&nbsp;(address=(host=rac2) (protocol=tcp) (port=1521)) <br>
     *            &nbsp;&nbsp;&nbsp;(load_balance=yes)(failover=yes) <br>
     *            ) <br>
     *            &nbsp;(connect_data= <br>
     *            &nbsp;&nbsp;&nbsp;(SERVER=DEDICATED) <br>
     *            &nbsp;&nbsp;&nbsp;(service_name=orctest) ) <br>
     *            ) <br>
     * @return 属性集合
     */
    public List<Property> oracleRacUrlResolve(String str) {
        str = str.trim();
        if (str.indexOf('(') == -1) {
            return null;
        } else {
            List<Property> list = new ArrayList<Property>();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '(') {
                    int ePos = getOracleRacUrlResolveEndKH(str, i + 1);
                    if (ePos == -1) {
                        throw new DatabaseException("oracleRacUrlResolve(" + str + ")");
                    }

                    String s = str.substring(i + 1, ePos);
                    int dhb = s.indexOf('=');
                    if (dhb == -1) {
                        throw new DatabaseException("oracleRacUrlResolve(" + str + ")");
                    }

                    Property obj = new Property();
                    String key = s.substring(0, dhb);
                    String val = s.substring(dhb + 1);
                    obj.setKey(key);
                    List<Property> v = oracleRacUrlResolve(val);
                    if (v == null) {
                        obj.setValue(val);
                    } else {
                        obj.setValue(v);
                    }
                    list.add(obj);

                    i = ePos;
                }
            }
            return list;
        }
    }

    /**
     * 返回括号结束位置
     *
     * @param str   字符串
     * @param begin 位置信息
     * @return 返回位置信息
     */
    int getOracleRacUrlResolveEndKH(String str, int begin) {
        for (int i = begin; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(') {
                int e = getOracleRacUrlResolveEndKH(str, i + 1);
                return getOracleRacUrlResolveEndKH(str, e + 1);
            } else if (c == ')') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析数据库存储过程的参数
     *
     * @param conn 数据库连接
     * @param proc 存储过程对象,可为null
     * @param ddl  存储过程ddl语句, 如: create procedure name(dt in varchar2)
     * @return 参数集合（按定义顺序从第一个开始排序） null表示ddl语句不合法不能解析
     */
    public List<DatabaseProcedureParameter> resolveOracleDatabaseProcedureParameter(Connection conn, DatabaseProcedure proc, String ddl) {
        if (StringUtils.isBlank(ddl)) {
            return null;
        }

        DatabaseTypeSet map = Jdbc.getTypeInfo(conn);
        String[][] pms = resolveDatabaseProcedureParam(ddl);
        if (pms == null) {
            return null;
        }

        List<DatabaseProcedureParameter> list = new ArrayList<DatabaseProcedureParameter>(pms.length);
        for (int i = 0, outcount = 0; i < pms.length; i++) {
            String[] a = pms[i];
            String name = a[0];
            String mode = a[1];
            String type = a[2];

            StandardDatabaseProcedureParameter dpp = new StandardDatabaseProcedureParameter();
            if (proc != null) {
                dpp.setProcedureSchema(proc.getSchema());
                dpp.setProcedureName(proc.getName());
            } else {
                String[] array = StandardDatabaseProcedure.resolveDatabaseProcedureDDLName(ddl);
                dpp.setProcedureSchema(SQL.toIdentifier(array[0]));
                dpp.setProcedureName(SQL.toIdentifier(array[1]));
            }

            dpp.setName(name);
            if ("IN".equalsIgnoreCase(mode)) {
                dpp.setMode(DatabaseProcedure.PARAM_IN_MODE);
            } else if ("OUT".equalsIgnoreCase(mode)) {
                dpp.setMode(DatabaseProcedure.PARAM_OUT_MODE);
            } else {
                dpp.setMode(DatabaseProcedure.PARAM_INOUT_MODE);
            }
            dpp.setFieldType(type);
            dpp.setPosition(i + 1);
            dpp.setSqlType(map.get(dpp.getFieldType()).getSqlType());
            dpp.setCanNull(true);
            dpp.setLength(-1);
            dpp.setScale(-1);

            if (dpp.getMode() == DatabaseProcedure.PARAM_OUT_MODE || dpp.getMode() == DatabaseProcedure.PARAM_INOUT_MODE) {
                dpp.setOutIndex(++outcount);
            } else {
                dpp.setOutIndex(0);
            }

            list.add(dpp);
        }

        return list;
    }

    /**
     * 解析数据库存储过程的参数
     *
     * @param ddl 存储过程参数, 如: procedure name(dt in varchar2)
     * @return 第一位表示参数名 第二位表示IN或OUT 第三位表示数据类型
     */
    public static String[][] resolveDatabaseProcedureParam(String ddl) {
        if (StringUtils.isBlank(ddl)) {
            return null;
        }

        int begin = ddl.indexOf('(');
        if (begin == -1) {
            return null;
        }

        int end = SQL.indexOfParenthes(ddl, begin);
        if (end == -1) {
            return null;
        }

        String str = ddl.substring(begin + 1, end);
        String[] split = SQL.split(str, ',');
        String[][] result = new String[split.length][3];
        for (int i = 0; i < split.length; i++) {
            String s = StringUtils.trimBlank(split[i]);
            String[] a = new String[3];
            String[] array = SQL.splitByBlank(s);
            if (array.length == 3) {
                a[0] = array[0].toUpperCase();
                a[1] = array[1].toUpperCase();
                a[2] = array[2].toUpperCase();
            } else if (array.length == 2) {
                a[0] = array[0].toUpperCase();
                a[1] = "IN";
                a[2] = array[1].toUpperCase();
            } else {
                throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg012", s));
            }

            result[i] = a;
        }
        return result;
    }

    public String getKeepAliveSQL() {
        return "select 1 from dual";
    }

    public JdbcConverterMapper getObjectConverters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean isOverLengthException(Throwable e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRebuildTableException(Throwable e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPrimaryRepeatException(Throwable e) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isIndexExistsException(Throwable e) {
        // TODO Auto-generated method stub
        return false;
    }

    public void reorgRunstatsIndexs(Connection conn, List<DatabaseIndex> indexs) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void openLoadMode(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void closeLoadMode(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void commitLoadData(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public JdbcConverterMapper getStringConverters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public DatabaseDDL toDDL(Connection connection, DatabaseProcedure procedure) throws SQLException {
        StandardDatabaseDDL ddl = new StandardDatabaseDDL();
        JdbcQueryStatement query = new JdbcQueryStatement(connection, "select dbms_metadata.get_ddl('PROCEDURE', '" + procedure.getName() + "') as TEXT from dual ");
        try {
            ResultSet resultSet = query.query();
            if (resultSet.next()) {
                String text = new ClobWriter(resultSet.getClob("TEXT")).toString();
                ddl.add(text);
            }
            return ddl;
        } finally {
            query.close();
        }
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

}
