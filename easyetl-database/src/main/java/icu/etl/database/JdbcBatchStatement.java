package icu.etl.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库批处理程序
 *
 * @author jeremy8551@qq.com
 * @createtime 2013-09-06
 */
public class JdbcBatchStatement {

    /** 数据库连接 */
    private Connection connection;

    /** JDBC 处理接口 */
    private PreparedStatement statement;

    /** 缓冲记录数 */
    private int commit;

    /** 计数器 */
    private int count;

    /** 插入对象的位置 */
    private int index;

    /**
     * 初始化
     */
    protected JdbcBatchStatement() {
        this.commit = 5000;
        this.count = 0;
        this.index = 0;
    }

    /**
     * 初始化
     *
     * @param connection 数据库连接
     * @param sql        SQL语句
     * @throws SQLException 数据库错误
     */
    public JdbcBatchStatement(Connection connection, String sql) throws SQLException {
        this();
        this.init(connection, sql);
    }

    /**
     * 初始化
     *
     * @param connection 数据库连接
     * @param sql        SQL语句
     * @param commit     缓冲记录数
     * @throws SQLException 数据库错误
     */
    public JdbcBatchStatement(Connection connection, String sql, int commit) throws SQLException {
        this();
        this.init(connection, sql);
        this.setCommitNumber(commit);
    }

    /**
     * 初始化
     *
     * @param connection 数据库连接
     * @param sql        SQL 语句
     * @throws SQLException 数据库错误
     */
    protected void init(Connection connection, String sql) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("database.standard.output.msg001"));
        }
        if (StringUtils.isBlank(sql)) {
            throw new IllegalArgumentException(sql);
        }

        this.resetCounter();
        this.index = 0;
        this.connection = connection;
        this.statement = this.connection.prepareStatement(sql);
    }

    /**
     * 设置最大缓存记录数
     *
     * @param n 缓存记录数
     */
    public void setCommitNumber(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        } else {
            this.commit = n;
        }
    }

    /**
     * 返回缓冲记录数
     */
    public int getCommitNumber() {
        return this.commit;
    }

    /**
     * 返回 JDBC PreparedStatement
     *
     * @return PreparedStatement接口
     */
    public PreparedStatement getPreparedStatement() {
        return this.statement;
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(byte x) throws SQLException {
        this.statement.setByte(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(String x) throws SQLException {
        this.statement.setString(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(int x) throws SQLException {
        this.statement.setInt(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(BigDecimal x) throws SQLException {
        this.statement.setBigDecimal(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(java.util.Date x) throws SQLException {
        this.statement.setDate(++this.index, (x == null ? null : new java.sql.Date(x.getTime())));
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(Double x) throws SQLException {
        this.statement.setDouble(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(Long x) throws SQLException {
        this.statement.setLong(++this.index, x);
    }

    /**
     * 在当前行插入字段
     *
     * @param x 字段值
     * @throws SQLException 数据库错误
     */
    public void setParameter(Object x) throws SQLException {
        this.statement.setObject(++this.index, x);
    }

    /**
     * 执行 {@linkplain Statement#addBatch(String)}
     *
     * @throws SQLException 数据库错误
     */
    public void addBatch() throws SQLException {
        this.statement.addBatch();
    }

    /**
     * 提交事务
     *
     * @return 批处理语句对应的结果
     * @throws SQLException 数据库错误
     */
    public int[] executeBatch() throws SQLException {
        if (this.statement != null) {
            this.index = 0;
            this.count++;
            if (this.count >= this.commit) {
                int[] array = this.statement.executeBatch();
                this.statement.clearBatch();
                this.resetCounter();
                return array;
            }
        }
        return null;
    }

    /**
     * 强制提交所有缓冲和事务
     *
     * @return 批处理语句对应的执行结果
     * @throws SQLException 数据库错误
     */
    public int[] forceExecuteBatch() throws SQLException {
        int orignal = this.count;
        try {
            this.count = this.commit;
            return this.executeBatch();
        } finally {
            this.count = orignal;
        }
    }

    /**
     * 将缓存数据提交到数据库，但不会关闭数据库连接
     *
     * @throws SQLException 数据库错误
     */
    public void close() throws SQLException {
        try {
            this.forceExecuteBatch();
        } finally {
            IO.close(this.statement);
            this.statement = null;
            this.connection = null;
            this.resetCounter();
            this.index = 0;
        }
    }

    /**
     * 返回数据库连接
     *
     * @return 数据库连接
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * 缓冲记录数
     */
    protected void resetCounter() {
        this.count = 0;
    }

}