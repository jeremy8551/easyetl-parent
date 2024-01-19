package icu.etl.database;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.sql.DataSource;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.internal.StandardDatabaseType;
import icu.etl.database.internal.StandardDatabaseTypes;
import icu.etl.database.logger.DataSourceLogger;
import icu.etl.database.logger.DataSourceLoggerProxy;
import icu.etl.database.pool.PoolConnection;
import icu.etl.database.pool.SimpleDatasource;
import icu.etl.ioc.EasyContext;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharTable;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.Property;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * JDBC 工具
 *
 * @author jeremy8551@qq.com
 * @createtime 2009-12-19 3:55:15
 */
public class Jdbc {
    private final static Log log = LogFactory.getLog(Jdbc.class);

    public final static String driver = "driver";
    public final static String driverClassName = "driverClassName";
    public final static String url = "url";
    public final static String schema = "schema";
    public final static String admin = "admin";
    public final static String adminPw = "adminPw";

    /** 是否打印内部使用的数据库连接池详细操作日志, true-表示使用数据库连接池代理打印详细信息 */
    public final static String PROPERTY_DBLOG = Jdbc.class.getPackage().getName().split("\\.")[0] + "." + Jdbc.class.getPackage().getName().split("\\.")[1] + ".dblog";

    public Jdbc() {
    }

    /**
     * 返回一个数据库连接 <br>
     * 事务策略：不自动提交事务 <br>
     *
     * @param url      JDBC URL
     * @param username 用户
     * @param password 密码
     * @return 数据库连接
     */
    public static Connection getConnection(String url, String username, String password) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(url);
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("database.standard.output.msg026", url, username, password));
        }

        try {
            if (username == null && password == null) {
                Connection conn = DriverManager.getConnection(url);
                conn.setAutoCommit(false);
                return conn;
            } else {
                Connection conn = DriverManager.getConnection(url, username, password);
                conn.setAutoCommit(false);
                return conn;
            }
        } catch (SQLException e) {
            throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg028", url, username, password), e);
        }
    }

    /**
     * 返回一个数据库连接池日志代理对象（用于打印访问数据库的关键操作信息）
     *
     * @param context    容器上下文信息
     * @param dataSource 数据库连接池
     * @return 数据库连接池
     */
    public static DataSource getDataSourceLogger(EasyContext context, SimpleDatasource dataSource) {
        if (dataSource != null && Boolean.parseBoolean(StringUtils.trimBlank(System.getProperty(PROPERTY_DBLOG))) && !(dataSource instanceof DataSourceLoggerProxy)) {
            return new DataSourceLogger(context, dataSource).getProxy();
        } else {
            return dataSource;
        }
    }

    /**
     * 判断表名是否相等
     *
     * @param schema1
     * @param tableName1
     * @param schema2
     * @param tableName2
     * @return
     */
    public static boolean equals(String schema1, String tableName1, String schema2, String tableName2) {
        schema1 = StringUtils.toCase(schema1, false, null);
        schema2 = StringUtils.toCase(schema2, false, null);

        if (StringUtils.isBlank(schema1)) {
            schema1 = null;
        }

        if (StringUtils.isBlank(schema2)) {
            schema2 = null;
        }

        if ((schema1 == null && schema2 == null) || (schema1 != null && schema1.equals(schema2))) {
            tableName1 = StringUtils.toCase(tableName1, false, null);
            tableName2 = StringUtils.toCase(tableName2, false, null);
            if (tableName1.equals(tableName2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 测试数据库连接是否可用 <br>
     * 测试内容包括创建表, 删除表, 新增记录, 修改记录, 删除记录 <br>
     *
     * @param conn    数据库连接
     * @param dialect 数据库方言, 可为null
     * @return true-数据库连接可用
     */
    public static boolean testConnection(Connection conn, DatabaseDialect dialect) {
        if (dialect == null) {
            throw new NullPointerException();
        }
        if (conn == null) {
            return false;
        }

        Statement statement = null;
        try {
            Connection connection = conn;
            if (conn instanceof PoolConnection) {
                connection = ((PoolConnection) conn).getConnection();
            }

            if (connection == null) {
                return false;
            }

            DatabaseMetaData metaData = connection.getMetaData();
            boolean supportSavepoint = metaData.supportsSavepoints();
            Savepoint savepoint = supportSavepoint ? connection.setSavepoint() : null;
            try {
                String catalog = dialect.getCatalog(conn);
                String schema = dialect.getSchema(conn);
                String tableName = getTableNameNoRepeat(connection, dialect, catalog, schema, "POOL_TEST_TABLE");
                statement = connection.createStatement();
                statement.executeUpdate("create table " + tableName + " ( id char(1) ) ");
                statement.executeUpdate("insert into " + tableName + " (id) values('1') ");
                IO.close(statement.executeQuery("select id from " + tableName));
                statement.executeUpdate("update " + tableName + " set id= '2' ");
                statement.executeUpdate("delete from " + tableName);
                statement.executeUpdate("drop table " + tableName);
                return true;
            } finally {
                try {
                    if (supportSavepoint && savepoint != null) {
                        connection.rollback(savepoint);
                    }
                } catch (Exception e) {
                }
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(StringUtils.toString(e));
            }
            return false;
        } finally {
            IO.closeQuiet(statement);
        }
    }

    /**
     * 返回 insert into 语句
     *
     * @param tableName
     * @param columns
     * @return
     */
    public static String toInsertStatement(String tableName, List<DatabaseTableColumn> columns) {
        String sql = "insert into " + tableName + " (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            sql += it.next().getName();
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ") values (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            it.next();
            sql += "?";
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ")";
        return sql;
    }

    /**
     * 将结果集中数据转为表格
     *
     * @param result
     * @return
     * @throws SQLException
     */
    public static String toString(ResultSet result) throws SQLException {
        String[] names = Jdbc.getColumnName(result);
        String[] typeNames = Jdbc.getColumnClassName(result);

        CharTable table = new CharTable();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            Class<Object> cls = ClassUtils.forName(typeNames[i]);
            if (cls != null && Number.class.isAssignableFrom(cls)) {
                table.addTitle(name, CharTable.ALIGN_RIGHT);
            } else {
                table.addTitle(name, CharTable.ALIGN_LEFT);
            }
        }

        while (result.next()) {
            for (int i = 0; i < names.length; i++) {
                Object value = result.getObject(names[i]);
                table.addCell(result.wasNull() ? "" : StringUtils.toString(value));
            }
        }

        return table.toString(CharTable.Style.db2);
    }

    /**
     * 判断数据库连接是否可用<br>
     * 如果数据库连接为null 返回 false <br>
     * 如果数据库连接已经关闭，返回 false <br>
     *
     * @param conn 数据库连接
     * @return
     */
    public static boolean canUse(Connection conn) {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            throw new DatabaseException("canUse(" + conn + ")", e);
        }
    }

    /**
     * 判断数据库连接是否可用<br>
     * 如果数据库连接为null 返回 false <br>
     * 如果数据库连接已经关闭，返回 false <br>
     *
     * @param conn 数据库连接
     * @return
     */
    public static boolean canUseQuietly(Connection conn) {
        try {
            return conn != null && !conn.isClosed();
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    /**
     * 判断数据库处理程序是否已关闭
     *
     * @param statement 处理器
     * @return 返回true表示已关闭 false表示未关闭
     */
    public static boolean isClosed(Statement statement) {
        if (statement == null) {
            return true;
        } else {
            try {
                return JavaDialectFactory.getDialect().isStatementClosed(statement);
            } catch (Throwable e) {
                return false;
            }
        }
    }

    /**
     * 如果数据库连接可用，则提交事务
     *
     * @param conn 数据库连接
     */
    public static void commit(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg030"), e);
            }
        }
    }

    /**
     * 提交事物，如果发生异常会打印错误信息但不会抛出异常信息
     *
     * @param conn 数据库连接
     */
    public static void commitQuiet(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getMessage("database.standard.output.msg030"), e);
                }
            }
        }
    }

    /**
     * 提交事务，如果发生异常不会打印错误信息也不会抛出异常信息
     *
     * @param conn 数据库连接
     */
    public static void commitQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * 回滚数据库事务
     *
     * @param conn 数据库连接
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg031"), e);
            }
        }
    }

    /**
     * 回滚数据库事务
     *
     * @param conn 数据库连接
     */
    public static void rollback(Connection conn, Savepoint savepoint) {
        if (conn != null && savepoint != null) {
            try {
                conn.rollback(savepoint);
            } catch (SQLException e) {
                throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg031"), e);
            }
        }
    }

    /**
     * 回滚数据库事务，如果发生异常会打印错误信息但不会抛出异常信息
     *
     * @param conn 数据库连接
     */
    public static void rollbackQuiet(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getMessage("database.standard.output.msg031"), e);
                }
            }
        }
    }

    /**
     * 回滚数据库事务，如果发生异常不会打印错误信息也不会抛出异常信息
     *
     * @param conn 数据库连接
     */
    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * 关闭数据库连接池数组，如果发生异常会打印错误信息但不会抛出异常信息
     *
     * @param array 数组
     */
    public static void closeDataSourceQuiet(DataSource... array) {
        if (ArrayUtils.isEmpty(array)) {
            return;
        }

        for (DataSource dataSource : array) {
            if (dataSource != null) {
                try {
                    Jdbc.closeDataSource(dataSource);
                } catch (Throwable e) {
                    if (log.isWarnEnabled()) {
                        log.warn(ResourcesUtils.getMessage("database.standard.output.msg027", dataSource), e);
                    }
                }
            }
        }
    }

    /**
     * 关闭数据库连接池
     *
     * @param array 数据库连接池
     */
    public static void closeDataSource(DataSource... array) {
        if (array == null || array.length == 0) {
            return;
        }

        List<Throwable> list = new ArrayList<Throwable>();
        for (DataSource dataSource : array) {
            try {
                Jdbc.closeDataSource(dataSource);
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(ResourcesUtils.getMessage("database.standard.output.msg027", dataSource), e);
                }

                list.add(e);
            }
        }

        if (list.size() > 0) {
            throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg027"));
        }
    }

    /**
     * 关闭数据库连接池
     *
     * @param dataSource 数据库连接池
     */
    protected static void closeDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }

        // 关闭数据库连接池
        if (SimpleDatasource.instanceOf(dataSource)) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg024", dataSource));
            }

            IO.close(dataSource);
        } else {
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("database.standard.output.msg027", dataSource));
        }
    }

    /**
     * 获取结果集的列名 <br>
     * {@linkplain ResultSetMetaData#getColumnName(int)} <br>
     *
     * @param result 结果集
     * @return 结果集列名
     * @throws SQLException 数据库错误
     */
    public static String[] getColumnName(ResultSet result) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int size = metaData.getColumnCount();
        String[] array = new String[size];
        for (int i = 0; i < size; ) {
            array[i] = metaData.getColumnName(++i);
        }
        return array;
    }

    /**
     * 返回结果集列的类型名 <br>
     * {@linkplain ResultSetMetaData#getColumnTypeName(int)} <br>
     * char, integer, decimal
     *
     * @param result 结果集
     * @return 结果集类型名
     * @throws SQLException 数据库错误
     */
    public static String[] getColumnTypeName(ResultSet result) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int size = metaData.getColumnCount();
        String[] array = new String[size];
        for (int i = 0; i < size; ) {
            array[i] = metaData.getColumnTypeName(++i);
        }
        return array;
    }

    /**
     * 返回结果中各个列的 {@linkplain java.sql.Types} 类型 <br>
     * {@linkplain ResultSetMetaData#getColumnType(int)}
     *
     * @param result 结果集
     * @return 结果集列类型
     * @throws SQLException 数据库错误
     */
    public static int[] getColumnType(ResultSet result) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int size = metaData.getColumnCount();
        int[] array = new int[size];
        for (int i = 0; i < size; ) {
            array[i] = metaData.getColumnType(++i);
        }
        return array;
    }

    /**
     * 返回结果集中各个字段对应的java类名(含包名) <br>
     * {@linkplain ResultSetMetaData#getColumnClassName(int)} <br>
     * 如: <br>
     * java.lang.String <br>
     * java.lang.Integer <br>
     *
     * @param result 结果集
     * @return 结果集列的类型名
     * @throws SQLException 数据库错误
     */
    public static String[] getColumnClassName(ResultSet result) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int size = metaData.getColumnCount();
        String[] array = new String[size];
        for (int i = 0; i < size; ) {
            array[i] = metaData.getColumnClassName(++i);
        }
        return array;
    }

    /**
     * 获取结果中列的个数
     *
     * @param result 结果集
     * @return 结果集列数
     * @throws SQLException 数据库错误
     */
    public static int getColumnCount(ResultSet result) throws SQLException {
        return Ensure.notNull(result).getMetaData().getColumnCount();
    }

    /**
     * 取得结果集中所有列: 标准宽度的和
     *
     * @param result 结果集
     * @return 结果集中所有列显示长度之和
     * @throws SQLException 数据库错误
     */
    public static int sumColumnDisplaySize(ResultSet result) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int size = metaData.getColumnCount();
        int value = 0;
        for (int i = 1; i <= size; i++) {
            value += metaData.getColumnDisplaySize(i);
        }
        return value;
    }

    /**
     * 把JDBC的ResultSet对象中的数据转换成 List <br>
     * <br>
     * 不适合处理大结果集
     *
     * @param result 数据库查询结果集
     * @param rtrim  true表示删除右侧空白字符
     * @return 结果集
     * @throws SQLException 数据库错误
     */
    public static List<Map<String, String>> resultToList(ResultSet result, boolean rtrim) throws SQLException {
        ResultSetMetaData metaData = Ensure.notNull(result).getMetaData();
        int column = metaData.getColumnCount();

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        while (result.next()) {
            Map<String, String> data = new CaseSensitivMap<String>();
            for (int i = 1; i <= column; i++) {
                String colName = metaData.getColumnName(i);
                String colValue = rtrim ? StringUtils.rtrim(result.getString(i)) : result.getString(i);
                data.put(colName, colValue);
            }
            list.add(data);
        }
        return list;
    }

    /**
     * 把JDBC的ResultSet对象中的数据转换成 List <br>
     * <br>
     * 不适合处理大结果集
     *
     * @param result 数据库结果集
     * @return 集合
     * @throws SQLException 数据库错误
     */
    public static List<Map<String, String>> resultToList(ResultSet result) throws SQLException {
        return resultToList(result, true);
    }

    /**
     * 把当前行的数据转为 Map 对象
     *
     * @param result 数据库查询结果集
     * @return 字段名与字段值映射的Map
     * @throws SQLException 数据库错误
     */
    public static Map<String, String> resultToMap(ResultSet result, boolean rtrimBlank) throws SQLException {
        if (result == null) {
            return null;
        }

        ResultSetMetaData metaData = result.getMetaData();
        int column = metaData.getColumnCount();
        Map<String, String> data = new CaseSensitivMap<String>();
        for (int i = 1; i <= column; i++) {
            String colName = metaData.getColumnName(i);
            String colValue = rtrimBlank ? StringUtils.rtrim(result.getString(i)) : result.getString(i);
            data.put(colName, colValue);
        }
        return data;
    }

    /**
     * 遍历 ResultSet，取keyPosition,valuePosition对应的数据列，组成Map
     *
     * @param result        JDBC ResultSet
     * @param keyPosition   从1开始
     * @param valuePosition 从1开始
     * @return 集合
     * @throws SQLException 数据库错误
     */
    public static Map<String, String> resultToMap(ResultSet result, int keyPosition, int valuePosition) throws SQLException {
        Map<String, String> map = new CaseSensitivMap<String>();
        while (result.next()) {
            String key = StringUtils.objToStr(result.getObject(keyPosition));
            String value = StringUtils.objToStr(result.getObject(valuePosition));
            map.put(key, value);
        }
        return map;
    }

    /**
     * 返回字段值，去掉右空格 <br>
     * 字段值为null时返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static String getString(ResultSet result, int index) throws SQLException {
        return StringUtils.rtrimBlank(result.getString(index));
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Integer getInt(ResultSet result, int index) throws SQLException {
        int value = result.getInt(index);
        return result.wasNull() ? null : value;
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Object getObject(ResultSet result, int index) throws SQLException {
        Object value = result.getObject(index);
        return result.wasNull() ? null : value;
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Double getDouble(ResultSet result, int index) throws SQLException {
        double value = result.getDouble(index); // 一定要先取数再判断
        return result.wasNull() ? null : new Double(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Float getFloat(ResultSet result, int index) throws SQLException {
        float value = result.getFloat(index);
        return result.wasNull() ? null : new Float(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Long getLong(ResultSet result, int index) throws SQLException {
        long value = result.getLong(index);
        return result.wasNull() ? null : new Long(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Short getShort(ResultSet result, int index) throws SQLException {
        short value = result.getShort(index);
        return result.wasNull() ? null : new Short(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Byte getByte(ResultSet result, int index) throws SQLException {
        byte val = result.getByte(index);
        return result.wasNull() ? null : new Byte(val);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param index  列位置（从1开始）
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Boolean getBoolean(ResultSet result, int index) throws SQLException {
        boolean value = result.getBoolean(index);
        return result.wasNull() ? null : new Boolean(value);
    }

    /**
     * 返回字段值，去掉右空格 <br>
     * 字段值为null时返回null
     *
     * @param result 结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static String getString(ResultSet result, String name) throws SQLException {
        return StringUtils.rtrimBlank(result.getString(name));
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Integer getInt(ResultSet result, String name) throws SQLException {
        int value = result.getInt(name);
        return result.wasNull() ? null : new Integer(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Double getDouble(ResultSet result, String name) throws SQLException {
        double value = result.getDouble(name);
        return result.wasNull() ? null : new Double(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Float getFloat(ResultSet result, String name) throws SQLException {
        float value = result.getFloat(name);
        return result.wasNull() ? null : new Float(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Long getLong(ResultSet result, String name) throws SQLException {
        long value = result.getLong(name);
        return result.wasNull() ? null : new Long(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Short getShort(ResultSet result, String name) throws SQLException {
        short value = result.getShort(name);
        return result.wasNull() ? null : new Short(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Byte getByte(ResultSet result, String name) throws SQLException {
        byte value = result.getByte(name);
        return result.wasNull() ? null : new Byte(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 数据库列的数值
     * @throws SQLException 数据库错误
     */
    public static Boolean getBoolean(ResultSet result, String name) throws SQLException {
        boolean value = result.getBoolean(name);
        return result.wasNull() ? null : new Boolean(value);
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param name   列名
     * @return 字符串
     * @throws SQLException 数据库错误
     */
    public static Object getObject(ResultSet result, String name) throws SQLException {
        Object value = result.getObject(name);
        return result.wasNull() ? null : value;
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result  数据库结果集
     * @param colname 列名
     * @return 字符串
     * @throws SQLException 数据库错误
     */
    public static String getClobAsString(ResultSet result, String colname) throws SQLException {
        Clob value = result.getClob(colname);
        return value == null ? null : value.getSubString((long) 1, (int) value.length());
    }

    /**
     * 从数据库结果集中读取指定列的数值，如果为空则返回null
     *
     * @param result 数据库结果集
     * @param column 列号
     * @return 数值
     * @throws SQLException 数据库错误
     */
    public static String getClobAsString(ResultSet result, int column) throws SQLException {
        Clob value = result.getClob(column);
        return value == null ? null : value.getSubString((long) 1, (int) value.length());
    }

    /**
     * 把 java对象使用驼峰命名法解析为数据库字段名
     *
     * @param javaFieldName java字段名
     * @return 字段名
     */
    public static String java2fieldName(String javaFieldName) {
        StringBuilder buf = new StringBuilder();
        char[] array = javaFieldName.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if (i == 0) {
                buf.append(c);
            } else if (Character.isUpperCase(c)) {
                buf.append('_');
                buf.append(c);
            } else {
                buf.append(c);
            }
        }
        return buf.toString().toUpperCase();
    }

    /**
     * 根据 sql类型返回 java 对象的类名
     *
     * @param sqlType 数据库中的字段的sql类型
     * @return 根据数据库字段类型返回对应的java对象的类名
     */
    public static String getJavaFieldClassName(int sqlType) {
        switch (sqlType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.CLOB:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                return String.class.getName();

            case java.sql.Types.DOUBLE:
                return Double.class.getName();

            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
                return Float.class.getName();

            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                return Integer.class.getName();

            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return BigDecimal.class.getName();

            case java.sql.Types.DATE:
                return Date.class.getName();

            case java.sql.Types.TIME:
                return Time.class.getName();

            case java.sql.Types.TIMESTAMP:
                return Timestamp.class.getName();

            case java.sql.Types.ARRAY:
                return "Object[]";

            case java.sql.Types.BIGINT:
                return Long.class.getName();

            case java.sql.Types.BIT:
                return Boolean.class.getName();

            case java.sql.Types.BLOB:
            case java.sql.Types.BINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.VARBINARY:
                return "byte[]";

            case java.sql.Types.BOOLEAN:
                return Boolean.class.getName();

            case java.sql.Types.OTHER:
                return Object.class.getName();

            case java.sql.Types.JAVA_OBJECT:
            case java.sql.Types.DISTINCT:
            case java.sql.Types.NULL:
            case java.sql.Types.REF:
            case java.sql.Types.STRUCT:
            case java.sql.Types.DATALINK:
                throw new UnsupportedOperationException();

            default:
                throw new UnsupportedOperationException(String.valueOf(sqlType));
        }
    }

    /**
     * 根据 sql类型返回 java 对象的类名
     *
     * @param sqlType 数据库中的字段的sql类型
     * @return 根据数据库字段类型返回对应的java对象的类名
     */
    public static String getJavaSqlResultSetGetMethod(int sqlType) {
        switch (sqlType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.CLOB:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                return "getString";

            case java.sql.Types.DOUBLE:
                return "getDouble";

            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
                return "getFloat";

            case java.sql.Types.INTEGER:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.TINYINT:
                return "getInt";

            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return "getBigDecimal";

            case java.sql.Types.DATE:
                return "getDate";

            case java.sql.Types.TIME:
                return "getTime";

            case java.sql.Types.TIMESTAMP:
                return "getTimestamp";

            case java.sql.Types.ARRAY:
                return "getBinaryStream";

            case java.sql.Types.BIGINT:
                return "getLong";

            case java.sql.Types.BIT:
                return "getBoolean";

            case java.sql.Types.BLOB:
            case java.sql.Types.BINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.VARBINARY:
                return "getBytes";

            case java.sql.Types.BOOLEAN:
                return "getBoolean";

            case java.sql.Types.OTHER:
                return "getObject";

            case java.sql.Types.JAVA_OBJECT:
            case java.sql.Types.DISTINCT:
            case java.sql.Types.NULL:
            case java.sql.Types.REF:
            case java.sql.Types.STRUCT:
            case java.sql.Types.DATALINK:
                throw new UnsupportedOperationException();

            default:
                throw new UnsupportedOperationException(String.valueOf(sqlType));
        }
    }

    /**
     * 删除属性名前面的 jdbc.
     *
     * @param p 属性
     * @return 一个新 Properties 副本
     */
    public static Properties removeJdbcFromKey(Properties p) {
        Properties obj = new Properties();
        Iterator<Entry<Object, Object>> it = p.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            String key = entry.getKey().toString();
            String val = entry.getValue().toString();

            if (key.startsWith("jdbc.")) {
                if (key.length() >= 5) {
                    obj.put(key.substring(5), val);
                } else {
                    obj.put("", val);
                }
            } else {
                obj.put(key, val);
            }
        }
        return obj;
    }

    /**
     * 检索此数据库用作目录和表名之间的分隔符的字符串
     *
     * @param conn 数据库连接
     * @return 分隔符
     */
    public static String getCatalogSeparator(Connection conn) {
        try {
            return conn.getMetaData().getCatalogSeparator();
        } catch (SQLException e) {
            throw new DatabaseException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 返回所有表模式名
     *
     * @param conn 数据库连接
     * @return 结果集是一个属性集合，属性名是 schema 属性值是 catalog
     */
    public static List<Property> getSchemas(Connection conn) {
        ResultSet resultSet = null;
        try {
            List<Property> list = new ArrayList<Property>();
            DatabaseMetaData metaData = conn.getMetaData();
            resultSet = metaData.getSchemas();
            while (resultSet.next()) {
                String schema = resultSet.getString("TABLE_SCHEM");
                String catalog = resultSet.getString("TABLE_CATALOG");
                list.add(new Property(schema, catalog));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e.getLocalizedMessage(), e);
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    /**
     * 截取数据库表名中的schema
     *
     * @param tableName 表名;不能为null
     * @return 表名中的schema; <br>
     * null表示不存在schema
     */
    public static String getSchema(String tableName) {
        String str = StringUtils.trimBlank(tableName);
        int index = str.indexOf('.');
        return (index == -1 || index == 0) ? null : str.substring(0, index);
    }

    /**
     * 删除表名中的 schema
     *
     * @param tableName 表名;不能为null
     * @return 表名（移除schema后的值）
     */
    public static String removeSchema(String tableName) {
        String str = StringUtils.trimBlank(tableName);
        int index = str.indexOf('.');
        return index == -1 ? str : ArrayUtils.lastElement(StringUtils.split(str, '.'));
    }

    /**
     * 返回一个数据库临时表名
     *
     * @param conn      数据库连接
     * @param dialect   数据库方言
     * @param catalog   编目名
     * @param schema    schema名
     * @param tableName 表名
     * @return 数据库表名
     * @throws SQLException 数据库错误
     */
    public static String getTableNameNoRepeat(Connection conn, DatabaseDialect dialect, String catalog, String schema, String tableName) throws SQLException {
        if (StringUtils.isBlank(schema)) {
            schema = dialect.getSchema(conn);
        } else {
            schema = StringUtils.trimBlank(schema).toUpperCase();
        }

        tableName = StringUtils.trimBlank(tableName).toUpperCase();
        List<DatabaseTable> tables = dialect.getTable(conn, catalog, schema, tableName);
        if (tables.isEmpty()) {
            return tableName;
        } else {
            return getTableNameNoRepeat(conn, dialect, catalog, schema, tableName + "_TMP");
        }
    }

    /**
     * 返回数据库中字段类型信息
     *
     * @param conn 数据库连接
     * @return 字段类型集合
     */
    public static DatabaseTypeSet getTypeInfo(Connection conn) {
        StandardDatabaseTypes map = new StandardDatabaseTypes();
        DatabaseMetaData metaData = null;
        ResultSet resultSet = null;
        try {
            metaData = conn.getMetaData();
            resultSet = metaData.getTypeInfo();
            while (resultSet.next()) {
                StandardDatabaseType type = new StandardDatabaseType(resultSet);
                map.put(type.getName(), type);
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException(e.getLocalizedMessage(), e);
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    /**
     * 查询数据库中关键字集合
     *
     * @param conn 数据库连接
     * @return 关键字集合
     */
    public static CaseSensitivSet getSQLKeywords(Connection conn) {
        try {
            CaseSensitivSet set = new CaseSensitivSet();
            String keywords = conn.getMetaData().getSQLKeywords();
            if (StringUtils.isNotBlank(keywords)) {
                StringUtils.split(keywords, ',', set);
            }
            return set;
        } catch (SQLException e) {
            throw new DatabaseException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 返回数据库连接当前用户下的所有表名 <br>
     * 如：<br>
     * 通过JDBC使用用户BIPS连接上ORACLE数据库：TESTDB <br>
     * 把得到的数据库连接对象 Connection 带入本函数，将返回BIPS用户下的所有数据库表名 <br>
     *
     * @param connection       数据库连接
     * @param catalog          类别名称，因为存储在此数据库中，所以它必须匹配类别名称。该参数为 "" 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
     * @param schemaPattern    模式名称，因为存储在此数据库中，所以它必须匹配模式名称。该参数为 "" 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
     * @param tableNamePattern 表名匹配（如：ECC_TABLE, ECC%），为null或空字符串表示表名
     * @return 数据库表集合
     * @throws SQLException 数据库错误
     */
    public static List<String> getTableNames(Connection connection, String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        List<String> list = new ArrayList<String>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(catalog, schemaPattern, tableNamePattern, new String[]{"TABLE"});
            while (resultSet.next()) {
                list.add(resultSet.getString("TABLE_NAME"));
            }
            return list;
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    /**
     * 移除与主键相同的索引信息
     *
     * @param indexs      索引信息
     * @param primaryKeys 主键信息
     */
    public static void removePrimaryKey(List<DatabaseIndex> indexs, List<DatabaseIndex> primaryKeys) {
        Iterator<DatabaseIndex> it = indexs.iterator();
        while (it.hasNext()) {
            DatabaseIndex index = it.next();
            List<String> idxNames = index.getColumnNames();
            List<Integer> idxSorts = index.getDirections();
            boolean alreadyExists = false;

            // 遍历主键信息
            for (DatabaseIndex pk : primaryKeys) {
                List<String> pkgNames = pk.getColumnNames();
                List<Integer> pkgSorts = pk.getDirections();

                if (index.getTableName().equals(pk.getTableName()) // 表名相同
                        && index.getTableSchema().equals(pk.getTableSchema()) // schema相同
                        && pkgNames.size() == idxNames.size() && pkgSorts.size() == idxSorts.size() // 字段个数相同
                ) {
                    boolean notequal = false; //
                    for (int i = 0; i < idxNames.size(); i++) {
                        Integer pkgS = pkgSorts.get(i);
                        if (!idxNames.get(i).equals(pkgNames.get(i)) // 索引字段相等
                                || (!pkgS.equals(DatabaseIndex.INDEX_UNKNOWN) && !idxSorts.get(i).equals(pkgS)) // 字段排序方式相同
                        ) {
                            notequal = true;
                            break;
                        }
                    }
                    if (notequal) {
                        continue;
                    }
                    alreadyExists = true;
                    break;
                }
            }

            if (alreadyExists) {
                it.remove();
            }
        }
    }

}
