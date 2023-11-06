package icu.etl.database.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import icu.etl.database.DB;
import icu.etl.database.DatabaseException;
import icu.etl.util.ArrayUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库连接代理类
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-13
 */
public class PoolConnection implements InvocationHandler {

    /**
     * 代理数据库连接的序号
     */
    private static int serialNo = 1;

    /**
     * 生成 PoolConnection 代理数据库连接的唯一三位字符串编号
     *
     * @return
     */
    private synchronized static String getSerialNo() {
        int number = serialNo++;
        return PoolConnection.class.getName() + "@" + StringUtils.right(number, 3, '0');
    }

    /** 数据库连接编号（唯一的） */
    protected String id;

    /** 数据库连接所在的连接池 */
    protected Pool pool;

    /** 数据库连接 */
    protected Connection conn;

    /** true表示数据库连接未关闭 */
    protected boolean isIdle;

    /** 已生成的处理器 */
    protected List<Statement> statements;

    /** 数据库连接初始配置信息 */
    protected ConnectionAttributes attributes;

    /** 数据库连接代理 */
    protected ConnectionProxy proxy;

    /**
     * 初始化
     */
    private PoolConnection() {
        super();
        this.statements = new Vector<Statement>();
    }

    /**
     * 初始化
     *
     * @param conn 被代理的数据库连接
     * @param pool 连接池
     */
    public PoolConnection(Connection conn, Pool pool) {
        this();

        this.attributes = new ConnectionAttributes(pool.getContext(), conn);
        this.id = getSerialNo();
        this.conn = conn;
        this.pool = pool;
        this.isIdle = true;
        this.proxy = this.createProxy();
    }

    /**
     * 克隆一个未关闭连接的副本
     *
     * @param conn 数据库连接
     */
    public PoolConnection(PoolConnection conn) {
        this();

        if (conn.attributes != null) {
            this.attributes = conn.attributes.clone();
        }

        this.id = conn.getId();
        this.conn = conn.getConnection();
        this.pool = conn.getPool();
        this.isIdle = true;
        this.proxy = this.createProxy();
    }

    /**
     * 创建一个数据库连接代理
     *
     * @return
     */
    private ConnectionProxy createProxy() {
        ConnectionProxy proxy = (ConnectionProxy) Proxy.newProxyInstance(this.conn.getClass().getClassLoader(), new Class[]{ConnectionProxy.class}, this);
        if (DB.out.isDebugEnabled()) {
            DB.out.debug(ResourcesUtils.getDatabaseMessage(42, this.getId(), this.conn.getClass().getName()));
        }
        return proxy;
    }

    /**
     * 返回 Connection 对象的代理
     *
     * @return
     */
    public ConnectionProxy getProxy() {
        return this.proxy;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (DB.out.isDebugEnabled()) {
            DB.out.debug(ResourcesUtils.getDatabaseMessage(35, this.getId(), this.conn.getClass().getName(), StringUtils.toString(method), StringUtils.toString(args)));
        }

        String methodName = method.getName();

        // 返回被代理数据库连接
        if ("getProxyConnection".equals(methodName) && ArrayUtils.isEmpty(args)) {
            return this.conn;
        }

        // 数据库连接回池
        else if ("close".equals(methodName) && ArrayUtils.isEmpty(args)) {
            this.returnPool();
            return null;
        }

        // 判断数据库连接是否可用
        else if ("isClosed".equals(methodName) && ArrayUtils.isEmpty(args)) {
            if (!this.isIdle) {
                return true;
            } else {
                return method.invoke(this.conn, args);
            }
        }

        // 执行被代理方法
        else {
            Object value = method.invoke(this.conn, args);
            if (value instanceof Statement) { // 将 Statement 保存到集合中
                this.statements.add((Statement) value);
            }
            return value;
        }
    }

    /**
     * 任务编号
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 数据库连接池
     *
     * @return
     */
    public Pool getPool() {
        return pool;
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
     * 返回代理数据库连接
     *
     * @return
     */
    protected Connection getProxyConnection() {
        if (this.isIdle) {
            return this.conn;
        } else {
            throw new DatabaseException(ResourcesUtils.getDataSourceMessage(10));
        }
    }

    /**
     * 数据库连接回池
     */
    private synchronized void returnPool() {
        if (this.isIdle) {
            this.closeStatements();

            // 判断数据库连接池是否可用
            if (this.pool != null && !this.pool.isClose()) {
                // 返回数据库连接池
                if (this.attributes != null) {
                    this.attributes.reset(this.getProxyConnection());
                }

                if (this.testConnection(this.conn)) {
                    this.pool.returnPool(this);
                } else {
                    this.pool.remove(this);
                }
            } else {
                if (DB.out.isDebugEnabled()) {
                    DB.out.debug(ResourcesUtils.getDataSourceMessage(11, this.toString()));
                }

                if (this.testConnection(this.conn)) {
                    IO.close(this.getProxyConnection());
                } else {
                    IO.closeQuietly(this.getProxyConnection());
                }
            }

            this.isIdle = false;
        }
    }

    /**
     * 测试数据库连接是否可以执行查询语句
     *
     * @param conn
     * @return
     */
    private boolean testConnection(Connection conn) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            String sql = this.pool.dialect.getKeepAliveSQL();
            ResultSet resultSet = statement.executeQuery(sql);
            IO.close(resultSet);
            return true;
        } catch (Throwable e) {
            return false;
        } finally {
            IO.close(statement);
        }
    }

    /**
     * 关闭所有已生成的 Statement
     */
    protected void closeStatements() {
        for (Iterator<Statement> it = this.statements.iterator(); it.hasNext(); ) {
            Statement statement = it.next();
            IO.closeQuietly(statement);
        }
        this.statements.clear();
    }

    public String toString() {
        return this.id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PoolConnection) {
            PoolConnection conn = (PoolConnection) obj;
            return this.id.equals(conn.id);
        } else {
            return false;
        }
    }

    /**
     * 释放所有资源
     */
    public void close() {
        this.id = null;
        this.conn = null;
        this.pool = null;
        this.isIdle = false;
        this.attributes = null;
        this.closeStatements();
    }

    protected void finalize() throws Throwable {
        this.close();
    }

}
