package icu.etl.database.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Ensure;
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
    private final static Log log = LogFactory.getLog(PoolConnection.class);

    /**
     * 代理数据库连接的序号
     */
    private static int serialNo = 1;

    /**
     * 生成 PoolConnection 代理数据库连接的唯一三位字符串编号
     *
     * @return 序号
     */
    private synchronized static String getSerialNo() {
        int number = serialNo++;
        return PoolConnection.class.getName() + "@" + StringUtils.right(number, 3, '0');
    }

    /** 数据库连接编号（唯一的） */
    protected String id;

    /** 数据库连接池 */
    protected Pool pool;

    /** 数据库连接 */
    protected Connection connection;

    /** true表示数据库连接未关闭 */
    protected volatile boolean idle;

    /** 已生成的处理器 */
    protected List<Statement> statements;

    /** 数据库连接初始配置信息 */
    protected ConnectionAttributes attributes;

    /** 数据库连接代理 */
    protected ConnectionProxy proxy;

    /** 用户名 */
    protected String username;

    /** 密码 */
    protected String password;

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
     * @param pool     数据库连接池
     * @param conn     被代理的数据库连接
     * @param username 用户名
     * @param password 密码
     */
    public PoolConnection(Pool pool, Connection conn, String username, String password) {
        this();

        this.id = getSerialNo();
        this.connection = Ensure.notNull(conn);
        this.pool = Ensure.notNull(pool);
        this.username = username;
        this.password = password;
        this.attributes = new ConnectionAttributes(pool.getContext(), conn);
        this.proxy = this.createProxy(pool.getContext().getClassLoader(), conn, this.id);
        this.idle = true;
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

        this.id = conn.id;
        this.connection = conn.connection;
        this.username = conn.username;
        this.password = conn.password;
        this.pool = conn.pool;
        this.proxy = this.createProxy(conn.pool.getContext().getClassLoader(), conn.connection, this.id);
        this.idle = true;
    }

    /**
     * 创建一个数据库连接代理
     *
     * @param classLoader 类加载器
     * @param conn        数据库连接（非代理）
     * @param id          数据库连接编号
     * @return 数据库连接代理
     */
    private ConnectionProxy createProxy(ClassLoader classLoader, Connection conn, String id) {
        ConnectionProxy proxy = (ConnectionProxy) Proxy.newProxyInstance(classLoader, new Class[]{ConnectionProxy.class}, this);
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("database.standard.output.msg042", id, conn.getClass().getName()));
        }
        return proxy;
    }

    /**
     * 返回 Connection 对象的代理
     *
     * @return 代理
     */
    public ConnectionProxy getProxy() {
        return this.proxy;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("database.standard.output.msg035", this.id, this.connection.getClass().getName(), StringUtils.toString(method), StringUtils.toString(args)));
        }

        String methodName = method.getName();

        // 返回被代理数据库连接
        if ("getOrignalConnection".equals(methodName) && ArrayUtils.isEmpty(args)) {
            return this.connection;
        }

        // 数据库连接回池
        if ("close".equals(methodName) && ArrayUtils.isEmpty(args)) {
            this.returnPool();
            return null;
        }

        // 判断数据库连接是否可用
        if ("isClosed".equals(methodName) && ArrayUtils.isEmpty(args)) {
            if (this.idle) {
                return method.invoke(this.connection, args);
            } else {
                return true;
            }
        }

        // 执行被代理方法
        Object value = method.invoke(this.connection, args);
        if (value instanceof Statement) { // 将 Statement 保存到集合中
            this.statements.add((Statement) value);
        }
        return value;
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
     * 数据库连接回池
     */
    private synchronized void returnPool() throws SQLException {
        if (this.idle) {
            this.closeStatements();

            // 判断数据库连接池是否可用
            if (this.pool != null && !this.pool.isClose()) {
                // 返回数据库连接池
                if (this.attributes != null) {
                    this.attributes.reset(this.getConnection());
                }

                if (this.testConnection(this.connection)) {
                    this.pool.returnPool(this);
                } else {
                    this.pool.remove(this);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("dataSource.standard.output.msg011", this.toString()));
                }

                if (this.testConnection(this.connection)) {
                    IO.close(this.getConnection());
                } else {
                    IO.closeQuietly(this.getConnection());
                }
            }

            this.idle = false;
        }
    }

    /**
     * 测试数据库连接是否可以执行查询语句
     *
     * @param conn 数据库连接
     * @return 返回true表示数据库连接可用 false表示数据库连接不可用
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
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
            return false;
        } finally {
            IO.close(statement);
        }
    }

    /**
     * 关闭所有已生成的 Statement
     */
    protected void closeStatements() {
        for (Statement statement : this.statements) {
            IO.closeQuietly(statement);
        }
        this.statements.clear();
    }

    /**
     * 返回创建数据库连接时使用的用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * 返回创建数据库连接时使用的密码
     *
     * @return 密码
     */
    public String getPassword() {
        return this.password;
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
        this.connection = null;
        this.pool = null;
        this.idle = false;
        this.attributes = null;
        this.closeStatements();
    }

    protected void finalize() {
        this.close();
    }

}
