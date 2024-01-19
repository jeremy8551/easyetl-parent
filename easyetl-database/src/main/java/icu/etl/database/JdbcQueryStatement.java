package icu.etl.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 查询操作
 *
 * @author jeremy8551@qq.com
 * @createtime 2013-09-06
 */
public class JdbcQueryStatement implements java.io.Closeable {

    /** 数据库查询语句 */
    private String sql;

    /** 数据库连接 */
    private Connection connection;

    /** sql预处理器 */
    private PreparedStatement statement;

    /** 查询结果 */
    private ResultSet resultSet;

    /** 参数序号计数器 */
    private int index;

    /** {@linkplain ResultSet#TYPE_FORWARD_ONLY} {@linkplain ResultSet#TYPE_SCROLL_INSENSITIVE} {@linkplain ResultSet#TYPE_SCROLL_SENSITIVE} */
    private int resultSetType;

    /** {@linkplain ResultSet#CONCUR_READ_ONLY} {@linkplain ResultSet#CONCUR_UPDATABLE} */
    private int resultSetConcurrency;

    /**
     * 初始化
     */
    private JdbcQueryStatement() {
        this.index = 0;
        this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        this.resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * 初始化
     *
     * @param connection           有效的数据库连接
     * @param sql                  SQL语句, 可以为空
     * @param resultSetType        {@linkplain ResultSet#TYPE_FORWARD_ONLY} <br>
     *                             {@linkplain ResultSet#TYPE_SCROLL_INSENSITIVE} <br>
     *                             {@linkplain ResultSet#TYPE_SCROLL_SENSITIVE} <br>
     * @param resultSetConcurrency {@linkplain ResultSet#CONCUR_READ_ONLY} <br>
     *                             {@linkplain ResultSet#CONCUR_UPDATABLE} <br>
     * @throws SQLException
     */
    public JdbcQueryStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        this();
        this.setConnection(connection);
        this.setSQL(sql);

        if (resultSetType == ResultSet.TYPE_FORWARD_ONLY || resultSetType == ResultSet.TYPE_SCROLL_INSENSITIVE || resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
            this.resultSetType = resultSetType;
        } else {
            throw new IllegalArgumentException(String.valueOf(resultSetType));
        }

        if (resultSetConcurrency == ResultSet.CONCUR_READ_ONLY || resultSetConcurrency == ResultSet.CONCUR_UPDATABLE) {
            this.resultSetConcurrency = resultSetConcurrency;
        } else {
            throw new IllegalArgumentException(String.valueOf(resultSetConcurrency));
        }

        this.statement = this.connection.prepareStatement(this.sql, this.resultSetType, this.resultSetConcurrency);
    }

    /**
     * 初始化
     *
     * @param connection 数据库连接
     * @param sql        查询语句
     * @throws SQLException
     */
    public JdbcQueryStatement(Connection connection, String sql) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * 初始化
     *
     * @param sql 数据库查询语句
     */
    public JdbcQueryStatement(String sql) {
        this();
        this.setSQL(sql);
    }

    /**
     * 设置数据库连接
     *
     * @param connection 有效的数据库连接
     * @throws SQLException
     */
    public void setConnection(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("database.standard.output.msg001"));
        } else {
            this.connection = connection;
        }
    }

    /**
     * 返回数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * 设置 SQL 语句
     *
     * @param sql
     */
    protected void setSQL(String sql) {
        if (StringUtils.isBlank(sql)) {
            throw new IllegalArgumentException(sql);
        } else {
            this.sql = sql;
        }
    }

    /**
     * 返回查询sql语句
     *
     * @return
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * 返回数据库处理程序
     *
     * @return
     */
    public PreparedStatement getStatement() {
        return this.statement;
    }

    /**
     * 执行查询语句
     *
     * @param parameters SQL语句中参数值数组
     * @return
     * @throws SQLException
     */
    public ResultSet query(Object... parameters) throws SQLException {
        this.index = 0;

        // 准备执行查询
        if (this.statement == null) {
            this.statement = this.connection.prepareStatement(this.sql, this.resultSetType, this.resultSetConcurrency);
        }

        // 设置查询参数值
        for (Object value : parameters) {
            this.setParameter(value);
        }

        // 执行查询
        IO.closeQuietly(this.resultSet);
        this.resultSet = this.statement.executeQuery();
        return this.resultSet;
    }

    /**
     * 返回结果集
     *
     * @return
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * 从结果集中读取一行数据
     *
     * @return
     * @throws SQLException
     */
    public boolean next() throws SQLException {
        return this.resultSet.next();
    }

    public void close() {
        IO.close(this.resultSet, this.statement);
        this.resultSet = null;
        this.statement = null;
        this.connection = null;
        this.index = 0;
        this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        this.resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        this.sql = null;
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param b     参数
     * @throws SQLException
     */
    public void setParameter(int index, byte b) throws SQLException {
        this.statement.setByte(index, b);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, String x) throws SQLException {
        this.statement.setString(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, int x) throws SQLException {
        this.statement.setInt(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, BigDecimal x) throws SQLException {
        this.statement.setBigDecimal(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameterString(int index, java.sql.Date x) throws SQLException {
        this.statement.setDate(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, java.util.Date x) throws SQLException {
        this.statement.setDate(index, x == null ? null : new java.sql.Date(x.getTime()));
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, Double x) throws SQLException {
        this.statement.setDouble(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, Long x) throws SQLException {
        this.statement.setLong(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, Object x) throws SQLException {
        this.statement.setObject(index, x);
    }

    /**
     * 设置参数值
     *
     * @param index 参数位置
     * @param x     参数
     * @throws SQLException
     */
    public void setParameter(int index, Object x, int sqltype) throws SQLException {
        this.statement.setObject(index, x, sqltype);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(byte x) throws SQLException {
        this.statement.setByte(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(String x) throws SQLException {
        this.statement.setString(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(int x) throws SQLException {
        this.statement.setInt(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(BigDecimal x) throws SQLException {
        this.statement.setBigDecimal(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameterString(java.sql.Date x) throws SQLException {
        this.statement.setDate(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(java.sql.Date x) throws SQLException {
        this.statement.setDate(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(java.util.Date x) throws SQLException {
        this.statement.setDate(++this.index, x == null ? null : new java.sql.Date(x.getTime()));
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(Double x) throws SQLException {
        this.statement.setDouble(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(Long x) throws SQLException {
        this.statement.setLong(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(Object x) throws SQLException {
        this.statement.setObject(++this.index, x);
    }

    /**
     * 设置参数值
     *
     * @param x 参数
     * @throws SQLException
     */
    public void setParameter(Object x, int sqlType) throws SQLException {
        this.statement.setObject(++this.index, x, sqlType);
    }

    /**
     * 返回字段值（自动删除右空格）
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public String getString(String name) throws SQLException {
        return StringUtils.rtrim(this.resultSet.getString(name));
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public byte[] getBytes(String name) throws SQLException {
        return this.resultSet.getBytes(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public java.sql.Date getDate(String name) throws SQLException {
        return this.resultSet.getDate(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public java.sql.Time getTime(String name) throws SQLException {
        return this.resultSet.getTime(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public java.sql.Timestamp getTimestamp(String name) throws SQLException {
        return this.resultSet.getTimestamp(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public boolean getBoolean(String name) throws SQLException {
        return this.resultSet.getBoolean(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public byte getByte(String name) throws SQLException {
        return this.resultSet.getByte(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public short getShort(String name) throws SQLException {
        return this.resultSet.getShort(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public int getInt(String name) throws SQLException {
        return this.resultSet.getInt(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public long getLong(String name) throws SQLException {
        return this.resultSet.getLong(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public float getFloat(String name) throws SQLException {
        return this.resultSet.getFloat(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public double getDouble(String name) throws SQLException {
        return this.resultSet.getDouble(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public BigDecimal getBigDecimal(String name) throws SQLException {
        return this.resultSet.getBigDecimal(name);
    }

    /**
     * 返回字段值
     *
     * @param name 列名
     * @return
     * @throws SQLException
     */
    public Object getObject(String name) throws SQLException {
        return this.resultSet.getObject(name);
    }

}
