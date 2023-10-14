package icu.etl.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;

import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.internal.StandardRowSetInternal;
import icu.etl.ioc.BeanFactory;
import icu.etl.log.STD;
import icu.etl.os.OSConnectCommand;
import icu.etl.timer.Timer;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * JDBC Dao
 *
 * @author jeremy8551@qq.com
 */
public class JdbcDao implements OSConnectCommand {

    /** 数据库连接 */
    private Connection conn;

    /** true表示自动删除查询结果集中字符串右端的空白字符 */
    private boolean isRtrim;

    /** 数据库连接对应的数据库方言 */
    private DatabaseDialect dialect;

    /** true 时表示在执行 {@link #close()} 方法时自动关闭数据库连接 */
    private boolean autoClose;

    /** JDBC 处理接口 */
    private Statement statement;

    /** 数据库厂商提供的数据库连接定制信息（用于终止数据库连接） */
    private Properties attributes;

    /**
     * 初始化
     */
    public JdbcDao() {
        this.isRtrim = true;
        this.autoClose = true;
        this.attributes = new Properties();
    }

    /**
     * 初始化
     *
     * @param conn 数据库连接
     */
    public JdbcDao(Connection conn) {
        this();
        this.setConnection(conn, true);
    }

    /**
     * 初始化
     *
     * @param conn      数据库连接
     * @param autoClose true 表示执行 {@link #close()} 方法时关闭数据库连接
     */
    public JdbcDao(Connection conn, boolean autoClose) {
        this();
        this.setConnection(conn, autoClose);
    }

    /**
     * 设置true表示自动删除查询结果集中字符串右端的空白字符
     *
     * @param rtrim
     */
    public void setRtrimResultSetString(boolean rtrim) {
        this.isRtrim = rtrim;
    }

    public boolean isConnected() {
        return Jdbc.canUseQuietly(this.getConnection());
    }

    /**
     * 测试数据库连接是否有效
     *
     * @return
     */
    public boolean testConnection() {
        return Jdbc.testConnection(this.getConnection(), this.getDialect());
    }

    /**
     * 设置是否自动提交数据库事物
     *
     * @param b
     * @throws SQLException
     */
    public void setAutoCommit(boolean b) throws SQLException {
        this.getConnection().setAutoCommit(b);
    }

    /**
     * 返回 JDBC Statement
     *
     * @return
     */
    public Statement getStatement() {
        return this.statement;
    }

    /**
     * 关闭 Statement
     */
    public void closeStatement() {
        IO.close(this.statement);
        this.statement = null;
    }

    /**
     * 返回 JDBC 处理接口
     *
     * @return
     * @throws SQLException
     */
    protected Statement createStatement() throws SQLException {
        if (Jdbc.isClosed(this.statement)) {
            this.statement = this.getConnection().createStatement();
        }
        return this.statement;
    }

    /**
     * 生成当前数据库连接的数据库方言
     *
     * @return
     */
    public DatabaseDialect getDialect() {
        if (this.dialect == null && this.existsConnection()) {
            this.dialect = BeanFactory.get(DatabaseDialect.class, this.getConnection());
        }
        return this.dialect;
    }

    /**
     * 从数据库连接池中返回一个连接
     *
     * @param pool 数据库连接池
     * @throws SQLException
     */
    public void connect(DataSource pool) throws SQLException {
        Connection conn = Jdbc.getConnection(pool);
        this.setConnection(conn, true);
    }

    /**
     * 建立数据库连接
     *
     * @param url      JDBC url
     * @param port     端口号
     * @param username 用户名
     * @param password 密码
     */
    public boolean connect(String url, int port, String username, String password) {
        try {
            Connection conn = Jdbc.getConnection(url, username, password);
            this.setConnection(conn, true);
            return this.existsConnection();
        } catch (Throwable e) {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(url, e);
            }
            return false;
        }
    }

    /**
     * 终止数据库连接
     *
     * @return 返回 true 表示已关闭数据库连接，false表示失败
     * @throws SQLException
     */
    public boolean terminate() throws SQLException {
        Connection conn = this.getConnection();
        if (conn != null && this.getDialect().terminate(conn, this.attributes)) {
            this.closeStatement();
            IO.closeQuiet(conn);
            this.setConnection(null, true); // 删除数据库连接
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存数据库连接
     *
     * @param conn 数据库连接
     */
    public void setConnection(Connection conn) {
        this.setConnection(conn, true);
    }

    /**
     * 保存数据库连接
     *
     * @param conn      数据库连接
     * @param autoClose true 时表示在执行 {@link #close()} 方法时自动关闭数据库连接
     */
    public void setConnection(Connection conn, boolean autoClose) {
        this.conn = conn;
        this.dialect = null;
        this.autoClose = autoClose;
        this.attributes.clear();

        if (conn != null) {
            this.attributes.putAll(this.getDialect().getAttributes(conn));

            try {
                this.commit();
            } catch (Throwable e) {
                this.rollbackQuiet();
            }
        }
    }

    /**
     * 判断数据库连接是否不为null
     *
     * @return
     */
    public boolean existsConnection() {
        return this.getConnection() != null;
    }

    /**
     * 返回数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * 返回数据库中使用的默认的 catalog
     *
     * @return
     * @throws SQLException
     */
    public String getCatalog() throws SQLException {
        return this.getDialect().getCatalog(this.getConnection());
    }

    /**
     * 返回数据库连接默认的表模式名
     *
     * @return
     * @throws SQLException
     */
    public String getSchema() throws SQLException {
        return this.getDialect().getSchema(this.getConnection());
    }

    /**
     * 判断数据库中是否存在指定表名
     *
     * @param catalog   类别信息
     * @param schema    表模式名
     * @param tableName 表名
     * @return
     * @throws SQLException
     */
    public boolean containsTable(String catalog, String schema, String tableName) throws SQLException {
        DatabaseDialect dialect = this.getDialect();
        if (dialect == null) {
            try {
                this.queryFirstRowFirstColByJdbc("select 1 from " + SQL.toTableName(schema, tableName));
                return true;
            } catch (Throwable e) {
                return false;
            }
        } else {
            return dialect.containsTable(this.getConnection(), catalog, schema, tableName);
        }
    }

    /**
     * 返回指定数据库表信息
     *
     * @param catalog   类别信息
     * @param schema    表模式名
     * @param tableName 表名
     * @return
     * @throws SQLException
     */
    public DatabaseTable getTable(String catalog, String schema, String tableName) throws SQLException {
        List<DatabaseTable> list = this.getDialect().getTable(this.getConnection(), catalog, schema, tableName);
        if (list.size() == 1) {
            return list.get(0);
        } else {
            if (schema == null) {
                schema = this.getSchema();
            }

            for (DatabaseTable table : list) {
                if (table.getSchema().equals(schema)) {
                    return table;
                }
            }

            for (DatabaseTable table : list) {
                if (table.getSchema().equalsIgnoreCase(schema)) {
                    return table;
                }
            }
            return null;
        }
    }

    /**
     * 返回存储过程对象信息
     *
     * @param catalog       类别信息
     * @param schema        归属表模式
     * @param procedureName 存储过程名
     * @return
     * @throws SQLException
     */
    public DatabaseProcedure getProcedure(String catalog, String schema, String procedureName) throws SQLException {
        return this.getDialect().getProcedureForceOne(this.getConnection(), catalog, schema, procedureName);
    }

    /**
     * 提交事务
     */
    public void commit() {
        Jdbc.commit(this.getConnection());
    }

    /**
     * 提交事务, 如果发生错误不会抛出异常但会打印异常信息
     */
    public void commitQuiet() {
        Jdbc.commitQuiet(this.getConnection());
    }

    /**
     * 提交事务, 如果发生错误不会抛出异常且不会打印异常信息
     */
    public void commitQuietly() {
        Jdbc.commitQuietly(this.getConnection());
    }

    public void close() {
        this.closeStatement();
        if (this.autoClose) {
            IO.close(this.getConnection());
        }
        this.setConnection(null, true);
    }

    /**
     * 关闭数据库连接, 如果发生错误不会抛出异常但会打印异常信息
     */
    public void closeQuiet() {
        IO.closeQuiet(this);
    }

    /**
     * 关闭数据库连接, 如果发生错误不会抛出异常且不会打印异常信息
     */
    public void closeQuietly() {
        IO.closeQuietly(this);
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        Jdbc.rollback(this.getConnection());
    }

    /**
     * 回滚事务, 如果发生错误不会抛出异常但会打印异常信息
     */
    public void rollbackQuiet() {
        Jdbc.rollbackQuiet(this.getConnection());
    }

    /**
     * 回滚事务到指定时间点
     */
    public void rollback(Savepoint point) {
        Jdbc.rollback(this.conn, point);
    }

    /**
     * 回滚事务, 如果发生错误不会抛出异常且不会打印异常信息
     */
    public void rollbackQuietly() {
        Jdbc.rollbackQuietly(this.getConnection());
    }

    /**
     * 降低数据库连接上的事务隔离级别
     *
     * @return 降低后的事务隔离级别，返回 -1 表示降低事务隔离级别失败
     */
    public int reduceIsolation() {
        try {
            this.getConnection().setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return Connection.TRANSACTION_READ_UNCOMMITTED;
        } catch (Throwable e) {
        }

        try {
            this.getConnection().setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return Connection.TRANSACTION_READ_COMMITTED;
        } catch (Throwable e) {
        }

        try {
            this.getConnection().setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            return Connection.TRANSACTION_REPEATABLE_READ;
        } catch (Throwable e) {
        }

        try {
            this.getConnection().setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            return Connection.TRANSACTION_SERIALIZABLE;
        } catch (Throwable e) {
        }

        return -1;
    }

    /**
     * 快速清空数据库表中数据
     *
     * @param catalog   类别名称（非必填）
     * @param schema    表模式名
     * @param tableName 表名
     * @return 返回快速清空数据库表的语句
     */
    public String deleteTableQuickly(String catalog, String schema, String tableName) {
        // 快速清空表的语句
        String sql = this.getDialect().toDeleteQuicklySQL(this.getConnection(), catalog, schema, tableName);

        TimeWatch watch = new TimeWatch();
        boolean success = false;
        int count = 0;
        while (true) {
            if (++count > 10) {
                break;
            }

            try {
                this.executeByJdbc(sql);
                success = true;
                break;
            } catch (Throwable e) {
                watch.start();
                while (watch.useSeconds() < 3) {
                }
                continue;
            }
        }

        if (success) {
            return sql;
        } else {
            sql = "delete from " + this.getDialect().toTableName(catalog, schema, tableName);
            this.executeByJdbcQuietly(sql);
            return sql;
        }
    }

    /**
     * 执行查询
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public JdbcQueryStatement query(String sql) throws SQLException {
        return this.query(sql, -1, -1);
    }

    /**
     * 执行查询
     *
     * @param sql                  SQL语句
     * @param resultSetType        {@linkplain ResultSet#TYPE_FORWARD_ONLY} <br>
     *                             {@linkplain ResultSet#TYPE_SCROLL_INSENSITIVE} <br>
     *                             {@linkplain ResultSet#TYPE_SCROLL_SENSITIVE} <br>
     * @param resultSetConcurrency {@linkplain ResultSet#CONCUR_READ_ONLY} <br>
     *                             {@linkplain ResultSet#CONCUR_UPDATABLE} <br>
     * @param array                SQL语句的参数
     * @return
     * @throws SQLException
     */
    public JdbcQueryStatement query(String sql, int resultSetType, int resultSetConcurrency, Object... array) throws SQLException {
        // 设置默认值
        if (resultSetType <= 0) {
            resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        }

        // 设置默认值
        if (resultSetConcurrency <= 0) {
            resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        }

        JdbcQueryStatement dao = new JdbcQueryStatement(this.getConnection(), sql, resultSetType, resultSetConcurrency);
        for (Object value : array) {
            dao.setParameter(value);
        }

        // 需要设置参数且未设置参数时，不能执行自动查询
        if (array.length == 0 && SQL.indexOf(sql, "?", 0, false) == -1) {
            dao.query();
        }

        return dao;
    }

    /**
     * 执行查询并返回第一行第一列的字段值
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <E> E queryFirstRowFirstColByJdbc(String sql) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = this.createStatement().executeQuery(sql);
            return (E) (resultSet.next() ? Jdbc.getObject(resultSet, 1) : null);
        } finally {
            IO.close(resultSet);
        }
    }

    /**
     * 执行查询并返回第一列的字段值集合
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> queryFirstColumnByJdbc(String sql) throws SQLException {
        List<E> list = new ArrayList<E>();
        ResultSet resultSet = null;
        try {
            resultSet = this.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                list.add((E) Jdbc.getObject(resultSet, 1));
            }
            return list;
        } finally {
            IO.close(resultSet);
        }
    }

    /**
     * 执行统计查询 <br>
     * select count(*) from table
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public Integer queryCountByJdbc(String sql) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = this.createStatement().executeQuery(sql);
            return resultSet.next() ? Jdbc.getInt(resultSet, 1) : null;
        } finally {
            IO.close(resultSet);
        }
    }

    /**
     * 执行查询返回 Map 对象，查询结果集中第一个字段作为 Map 的 Key，第二个字段作为 Map 的 Value <br>
     * Map&lt;f0, f1&gt; = queryMapByJdbc("select f0, f1, f2 .. from table"); <br>
     * Map&lt;f1, f2&gt; = queryMapByJdbc("select f1, f2, f0 .. from table"); <br>
     * Map&lt;f2, f1&gt; = queryMapByJdbc("select f2, f1, f0 .. from table"); <br>
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public Map<String, String> queryMapByJdbc(String sql) throws SQLException {
        return this.queryMapByJdbc(sql, 1, 2);
    }

    /**
     * 执行查询返回 Map 对象，查询结果集中第 keyPosition 个字段作为 Map 的 Key，第 valuePosition 个字段作为 Map 的 Value <br>
     * Map&lt;f2, f1&gt; = queryMapByJdbc("select f2, f1, f3 .. from table", 1, 2); <br>
     * Map&lt;f1, f3&gt; = queryMapByJdbc("select f2, f1, f3 .. from table", 2, 3); <br>
     *
     * @param sql           SQL语句
     * @param keyPosition   Key 在查询结果集中的位置, 从1开始
     * @param valuePosition value 在查询结果集中的位置, 从1开始
     * @return
     * @throws SQLException
     */
    public Map<String, String> queryMapByJdbc(String sql, int keyPosition, int valuePosition) throws SQLException {
        if (keyPosition <= 0) {
            throw new IllegalArgumentException(String.valueOf(keyPosition));
        }
        if (valuePosition <= 0) {
            throw new IllegalArgumentException(String.valueOf(valuePosition));
        }

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getConnection().prepareStatement(sql);
            resultSet = statement.executeQuery();
            return Jdbc.resultToMap(resultSet, keyPosition, valuePosition);
        } finally {
            IO.close(resultSet, statement);
        }
    }

    /**
     * 执行查询返回 Map 对象，查询结果集中字段名是 keyName 的字段作为 Map 的 key ，字段名是 valueName 的字段作为 Map 的 Value <br>
     * Map&lt;f2, f3&gt; = queryMapByJdbc("select f1, f2, f3 from table", "f2", "f3"); <br>
     * Map&lt;f1, f4&gt; = queryMapByJdbc("select f1, f3, f4 from table", "f1", "f4"); <br>
     *
     * @param sql       SQL语句
     * @param keyName   key 在查询结果集中的字段名
     * @param valueName value 在查询结果集中的字段名
     * @return
     * @throws SQLException
     */
    public Map<String, String> queryMapByJdbc(String sql, String keyName, String valueName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getConnection().prepareStatement(sql);
            resultSet = statement.executeQuery();

            int keyIndex = 0;
            int valIndex = 0;

            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            for (int index = 1; index <= count; index++) {
                String name = metaData.getColumnName(index); // field Name
                if (keyIndex == 0 && name.equalsIgnoreCase(keyName)) {
                    keyIndex = index;
                }
                if (valIndex == 0 && name.equalsIgnoreCase(valueName)) {
                    valIndex = index;
                }
            }

            if (keyIndex == 0) {
                throw new IllegalArgumentException(keyName);
            }
            if (valIndex == 0) {
                throw new IllegalArgumentException(valueName);
            }
            return Jdbc.resultToMap(resultSet, keyIndex, valIndex);
        } finally {
            IO.close(resultSet, statement);
        }
    }

    /**
     * 执行批处理更新 <br>
     * 使用 {@link JdbcBatchStatement#setParameter(java.math.BigDecimal)} 保存字段值 <br>
     * 使用 {@link JdbcBatchStatement#addBatch()} 方法添加行 <br>
     * 使用 {@link JdbcBatchStatement#executeBatch()} 方法添加行 <br>
     * 使用 {@link JdbcBatchStatement#close()} 提交事务并释放数据库连接
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public JdbcBatchStatement update(String sql) throws SQLException {
        return this.update(sql, 0);
    }

    /**
     * 执行批处理更新 <br>
     * 使用 {@link JdbcBatchStatement#setParameter(java.math.BigDecimal)} 保存字段值 <br>
     * 使用 {@link JdbcBatchStatement#addBatch()} 方法添加行 <br>
     * 使用 {@link JdbcBatchStatement#executeBatch()} 方法添加行 <br>
     * 使用 {@link JdbcBatchStatement#close()} 提交事务并释放数据库连接
     *
     * @param sql    SQL语句
     * @param commit 设置批量提交的笔数, 小于等于零表示使用默认值
     * @return
     * @throws SQLException
     */
    public JdbcBatchStatement update(String sql, int commit) throws SQLException {
        if (commit > 0) {
            return new JdbcBatchStatement(this.getConnection(), sql, commit);
        } else {
            return new JdbcBatchStatement(this.getConnection(), sql);
        }
    }

    /**
     * 批量执行SQL语句
     *
     * @param array SQL语句数组
     * @return 返回SQL语句影响的记录数数组
     * @throws SQLException
     */
    public int[] executeUpdateByJdbc(String... array) throws SQLException {
        Statement statement = this.createStatement();
        for (String sql : array) {
            statement.addBatch(sql);
        }
        return statement.executeBatch();
    }

    /**
     * 批量执行SQL语句
     *
     * @param list SQL语句集合
     * @return 返回SQL语句影响的记录数数组
     * @throws SQLException
     */
    public int[] executeUpdateByJdbc(Iterable<String> list) throws SQLException {
        Statement statement = this.createStatement();
        for (String sql : list) {
            if (StringUtils.isNotBlank(sql)) {
                statement.addBatch(sql);
            }
        }
        return statement.executeBatch();
    }

    /**
     * 执行SQL语句 <br>
     * INSERT, UPDATE, DELETE
     *
     * @param sql SQL语句
     * @return 返回SQL语句影响的记录数
     * @throws SQLException
     */
    public int executeUpdateByJdbc(String sql) throws SQLException {
        return this.createStatement().executeUpdate(sql);
    }

    /**
     * 执行SQL语句，如果发生错误不会抛出异常, 但会打印异常信息 <br>
     * INSERT, UPDATE, DELETE
     *
     * @param sql SQL语句
     * @return 返回SQL语句影响的记录数
     */
    public Integer executeUpdateByJdbcQuiet(String sql) {
        try {
            return new Integer(this.executeUpdateByJdbc(sql));
        } catch (Throwable e) {
            if (STD.out.isErrorEnabled()) {
                STD.out.error(sql, e);
            }
            return null;
        }
    }

    /**
     * 执行SQL语句，如果发生错误不会抛出异常, 且不会打印异常信息 <br>
     * INSERT, UPDATE, DELETE
     *
     * @param sql SQL语句
     * @return 返回SQL语句影响的记录数
     */
    public Integer executeUpdateByJdbcQuietly(String sql) {
        try {
            return new Integer(this.executeUpdateByJdbc(sql));
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 执行SQL语句
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public boolean executeByJdbc(String sql) throws SQLException {
        return this.createStatement().execute(sql);
    }

    /**
     * 执行SQL语句
     *
     * @param c SQL语句集合
     * @throws SQLException
     */
    public void executeByJdbc(java.util.Collection<String> c) throws SQLException {
        for (String sql : c) {
            this.executeByJdbc(sql);
        }
    }

    /**
     * 执行SQL语句
     *
     * @param sql    SQL语句
     * @param reader JDBC 查询结果集的处理规则
     * @return 返回SQL语句影响的记录数
     * @throws SQLException
     */
    public int executeByJdbc(String sql, RowSetReader reader) throws SQLException {
        Statement statement = this.createStatement();
        if (statement.execute(sql)) {
            ResultSet resultSet = statement.getResultSet();
            try {
                if (reader != null) {
                    reader.readData(this.getRowSetInternal(statement, resultSet));
                }
                return 0;
            } finally {
                resultSet.close();
            }
        } else {
            return statement.getUpdateCount();
        }
    }

    protected RowSetInternal getRowSetInternal(Statement statement, ResultSet resultSet) {
        return new StandardRowSetInternal(statement, resultSet);
    }

    /**
     * 执行SQL语句, 如果发生错误不会抛出异常, 但会打印异常信息
     *
     * @param sql SQL语句
     * @return
     */
    public boolean executeByJdbcQuiet(String sql) {
        try {
            return this.executeByJdbc(sql);
        } catch (Throwable e) {
            STD.out.error(sql, e);
            return false;
        }
    }

    /**
     * 执行SQL语句, 如果发生错误不会抛出异常且不会打印异常信息
     *
     * @param sql SQL语句
     * @return
     */
    public boolean executeByJdbcQuietly(String sql) {
        try {
            return this.executeByJdbc(sql);
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 执行查询，并将查询结果集封装到 List 集合中
     *
     * @param sql SQL语句
     * @return
     * @throws SQLException
     */
    public List<Map<String, String>> queryListMapByJdbc(String sql) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = this.createStatement().executeQuery(sql);
            return Jdbc.resultToList(resultSet, this.isRtrim);
        } finally {
            IO.close(resultSet);
        }
    }

    /**
     * 执行数据库存储过程
     *
     * @param sql SQL语句, 如: call name(?)
     * @return 数据库存储过程信息
     * @throws SQLException
     */
    public DatabaseProcedure callProcedureByJdbc(String sql) throws SQLException {
        TimeWatch watch = new TimeWatch();
        DatabaseProcedure procedure = StandardDatabaseProcedure.toProcedure(this, sql);
        String call = procedure.toCallProcedureSql();

        CallableStatement statement = null;
        try {
            statement = this.getConnection().prepareCall(call);
            List<DatabaseProcedureParameter> parameters = procedure.getParameters();
            for (DatabaseProcedureParameter parameter : parameters) {
                if (parameter.getMode() == DatabaseProcedure.PARAM_INOUT_MODE) {
                    String expression = parameter.getExpression();
                    if (parameter.isExpression() || expression.equals("?")) { // mean output parameter
                        statement.registerOutParameter(parameter.getOutIndex(), parameter.getSqlType());
                    } else { // means input parameter
                        parameter.setStatement(statement);
                        continue;
                    }
                } else if (parameter.getMode() == DatabaseProcedure.PARAM_OUT_MODE) {
                    statement.registerOutParameter(parameter.getOutIndex(), parameter.getSqlType());
                }
            }
            statement.execute();

            for (DatabaseProcedureParameter parameter : parameters) {
                if (parameter.isOutMode()) {
                    Object value = statement.getObject(parameter.getOutIndex());
                    parameter.setValue(value);
                }
            }

            if (STD.out.isDebugEnabled()) {
                StringBuilder buf = new StringBuilder();
                for (DatabaseProcedureParameter param : parameters) {
                    if (param.isOutMode()) {
                        buf.append(" {" + param.getName() + " = " + StringUtils.toString(param.getValue()) + "}");
                    }
                }

                STD.out.debug(ResourcesUtils.getDatabaseMessage(16, call, watch.useTime(), buf.toString()));
            }

            return procedure;
        } finally {
            IO.close(statement);
        }
    }

    /**
     * 删除数据库表的索引信息
     *
     * @param table 数据库表信息
     * @return 执行删除数据库表时使用的SQL语句
     */
    public List<String> dropIndex(DatabaseTable table) {
        if (table == null) {
            throw new NullPointerException();
        }

        List<DatabaseIndex> indexs = table.getIndexs();
        List<String> list = new ArrayList<String>(indexs.size());
        for (DatabaseIndex index : indexs) {
            list.add(this.dropIndex(index));
        }
        return list;
    }

    /**
     * 删除数据库表上的主键
     *
     * @param index
     * @return
     * @throws SQLException
     */
    public String dropPrimaryKey(DatabaseIndex index) throws SQLException {
        if (index == null) {
            throw new NullPointerException();
        } else {
            return this.getDialect().dropPrimaryKey(this.getConnection(), index);
        }
    }

    /**
     * 删除数据库表索引
     *
     * @param index 数据库表索引信息
     * @return 执行删除数据库表索引时使用的SQL语句
     */
    public String dropIndex(DatabaseIndex index) {
        if (index == null) {
            throw new NullPointerException();
        }

        String sql = "drop index " + index.getFullName();
        int count = 0;
        while (true) {
            if (++count > 10) {
                break;
            }

            try {
                this.executeByJdbc(sql);
                break;
            } catch (Throwable e) {
                Timer.sleep(3000);
                continue;
            }
        }
        return sql;
    }

    /**
     * 删除数据库表
     *
     * @param table 数据库表信息
     * @return 执行删除数据库表时使用的SQL语句
     * @throws SQLException
     */
    public String dropTable(DatabaseTable table) throws SQLException {
        String sql = "drop table " + table.getFullName();
        this.executeByJdbc(sql);
        return sql;
    }

    /**
     * 创建数据库表，主键，索引
     *
     * @param ddl
     * @return 建表语句集合
     * @throws SQLException
     */
    public List<String> createTable(DatabaseTableDDL ddl) throws SQLException {
        this.executeByJdbc(ddl.getTable());
        this.executeByJdbc(ddl.getPrimaryKey());
        this.executeByJdbc(ddl.getIndex());
        this.executeByJdbc(ddl.getComment());

        List<String> list = new ArrayList<String>();
        list.add(ddl.getTable());
        list.addAll(ddl.getPrimaryKey());
        list.addAll(ddl.getIndex());
        list.addAll(ddl.getComment());
        return list;
    }

    /**
     * 执行SQL语句 <br>
     * INSERT, UPDATE, DELETE语句
     *
     * @param conn 数据库连接, 不会自动提交连接上的事务且不会自动关闭数据库连接
     * @param sql  SQL语句
     * @return SQL语句影响记录数
     * @throws SQLException
     */
    public static int executeUpdateByJdbc(Connection conn, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            return statement.executeUpdate(sql);
        } finally {
            IO.close(statement);
        }
    }

    /**
     * 在数据库连接参数 conn 上执行SQL语句
     *
     * @param conn 数据库连接, 不会自动提交连接上的事务且不会自动关闭数据库连接
     * @param sql  SQL语句
     * @return 如果结果是ResultSet对象，则为true；如果是更新计数或没有结果，则为false
     * @throws SQLException
     */
    public static boolean executeByJdbc(Connection conn, String sql) throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            return statement.execute(sql);
        } finally {
            IO.close(statement);
        }
    }

    /**
     * 在数据库连接参数 conn 上执行查询
     *
     * @param conn 数据库连接, 不会自动提交连接上的事务且不会自动关闭数据库连接
     * @param sql  SQL语句
     * @return
     * @throws SQLException
     */
    public static List<Map<String, String>> queryListMapsByJdbc(Connection conn, String sql) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();
            return Jdbc.resultToList(resultSet, true);
        } finally {
            IO.close(resultSet, statement);
        }
    }

    /**
     * 在数据库连接参数 conn 上执行统计查询
     *
     * @param conn 数据库连接, 不会自动提交连接上的事务且不会自动关闭数据库连接
     * @param sql  SQL语句
     * @return
     * @throws SQLException
     */
    public static Integer queryCountByJdbc(Connection conn, String sql) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet.next() ? Jdbc.getInt(resultSet, 1) : null;
        } finally {
            IO.close(resultSet, statement);
        }
    }

    /**
     * 从数据库连接池参数 pool 上返回一个连接并执行统计查询
     *
     * @param pool 数据库连接池
     * @param sql  SQL语句
     * @return
     * @throws SQLException
     */
    public static Integer queryCountByJdbc(DataSource pool, String sql) throws SQLException {
        Connection conn = Jdbc.getConnection(pool);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet.next() ? Jdbc.getInt(resultSet, 1) : null;
        } finally {
            IO.close(resultSet, statement, conn);
        }
    }

    /**
     * 在数据库连接参数 conn 上执行查询，并返回查询结果集上第一个行第一个列的数值
     *
     * @param conn 数据库连接, 不会自动提交连接上的事务且不会自动关闭数据库连接
     * @param sql  SQL语句
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static <E> E queryFirstRowFirstColByJdbc(Connection conn, String sql) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet.next() ? (E) Jdbc.getObject(resultSet, 1) : null;
        } finally {
            IO.close(resultSet, statement);
        }
    }

    /**
     * 将数据库表转为 DDL 语句
     *
     * @param table 数据库表信息
     * @return
     * @throws SQLException
     */
    public DatabaseTableDDL toDDL(DatabaseTable table) throws SQLException {
        return this.getDialect().toDDL(this.getConnection(), table);
    }

    /**
     * 从数据库中查询索引的 DDL 语句
     *
     * @param index   索引信息
     * @param primary true表示主键
     * @return
     * @throws SQLException
     */
    public DatabaseDDL toDDL(DatabaseIndex index, boolean primary) throws SQLException {
        return this.getDialect().toDDL(this.getConnection(), index, primary);
    }

    /**
     * 从数据库中查询存储过程的 DDL 语句
     *
     * @param procedure 存储过程
     * @return
     * @throws SQLException
     */
    public DatabaseDDL toDDL(DatabaseProcedure procedure) throws SQLException {
        return this.getDialect().toDDL(this.getConnection(), procedure);
    }

    /**
     * 打开数据库表的数据装载模式
     *
     * @param fullname 数据库表全名
     * @throws SQLException
     */
    public void openLoadMode(String fullname) throws SQLException {
        this.getDialect().openLoadMode(this, fullname);
    }

    /**
     * 提交数据库表上的数据装载模式
     *
     * @param fullname 数据库表全名
     * @throws SQLException
     */
    public void commitLoadMode(String fullname) throws SQLException {
        this.getDialect().commitLoadData(this, fullname);
    }

    /**
     * 关闭数据库表上的数据装载模式
     *
     * @param fullname 数据库表全名
     * @throws SQLException
     */
    public void closeLoadMode(String fullname) throws SQLException {
        this.getDialect().closeLoadMode(this, fullname);
    }

}
