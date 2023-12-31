package icu.etl.database.pool;

import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Properties;

import icu.etl.database.DatabaseConfiguration;
import icu.etl.database.DatabaseConfigurationContainer;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseException;
import icu.etl.database.Jdbc;
import icu.etl.database.internal.StandardDatabaseConfiguration;
import icu.etl.io.OutputStreamLogger;
import icu.etl.ioc.EasyContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSAccount;
import icu.etl.util.ClassUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 数据库连接池
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-13
 */
public class Pool implements Closeable {
    private final static Log log = LogFactory.getLog(Pool.class);

    /** 活动数据库连接 */
    protected PoolConnectionList actives;

    /** 空闲数据库连接 */
    protected PoolConnectionList idles;

    /** jdbc配置信息 */
    protected DatabaseConfiguration jdbc;

    /** true-已关闭 */
    protected boolean isClose;

    /** 打印日志输出流 */
    protected PrintWriter out;

    /** 数据库方言 */
    protected DatabaseDialect dialect;

    /** 超时时间, 单位: 秒 */
    private int timeout;

    /** 计时器 */
    private TimeWatch watch;

    /** 容器上下文信息 */
    protected EasyContext context;

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     */
    public Pool(EasyContext context) {
        super();
        if (context == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.watch = new TimeWatch();
        this.out = new PrintWriter(new OutputStreamLogger(log, StringUtils.CHARSET));
        this.actives = new PoolConnectionList();
        this.idles = new PoolConnectionList();
        this.isClose = true;
        this.timeout = 0;
    }

    public EasyContext getContext() {
        return context;
    }

    /**
     * 启动数据库连接池
     */
    public synchronized void open() {
        if (this.isClose) {
            this.isClose = false;
        } else {
            this.out.println(ResourcesUtils.getDataSourceMessage(1));
        }
    }

    /**
     * 关闭数据库连接池
     */
    public synchronized void close() {
        if (this.isClose) {
            return;
        }

        this.out.println(ResourcesUtils.getDataSourceMessage(2, SimpleDatasource.class.getName(), this.idles.size(), this.actives.size()));
        this.idles.close();
        this.actives.close();
        this.dialect = null;
        this.isClose = true;
    }

    /**
     * 返回一个数据库连接
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public synchronized ConnectionProxy getConnection(String username, String password) {
        if (this.isClose) {
            throw new DatabaseException(ResourcesUtils.getDataSourceMessage(3));
        }

        if (this.idles.empty()) {
            String driver = this.jdbc.getDriverClass();
            String url = this.jdbc.getUrl();

            if (username == null) {
                OSAccount account = this.jdbc.getAccount();
                if (account != null) {
                    username = account.getUsername();
                    password = account.getPassword();
                }
            }

            Connection conn = this.create(driver, url, username, password);
            PoolConnection pc = new PoolConnection(conn, this);
            this.actives.push(pc);
            this.out.println(ResourcesUtils.getDataSourceMessage(4, pc.toString()));
            return pc.getProxy();
        } else {
            PoolConnection pc = this.idles.pop(); // 从空闲池中获取一个数据库连接
            if (pc == null || pc.getConnection() == null) {
                return this.getConnection(username, password);
            } else {
                Connection conn = pc.getConnection();
                try {
                    if (Jdbc.canUse(conn)) {
                        this.actives.push(pc);
                        this.out.println(ResourcesUtils.getDataSourceMessage(5, pc.toString()));
                        return pc.getProxy();
                    } else {
                        Jdbc.commitQuiet(conn);
                        IO.closeQuiet(conn);
                        pc.close();
                    }
                } catch (Throwable e) {
                    if (Jdbc.testConnection(conn, this.dialect)) {
                        this.actives.push(pc);
                        this.out.println(ResourcesUtils.getDataSourceMessage(5, pc.toString()));
                        return pc.getProxy();
                    } else {
                        Jdbc.commitQuiet(conn);
                        IO.closeQuiet(conn);
                        pc.close();
                    }
                }

                return this.getConnection(username, password);
            }
        }
    }

    /**
     * 建立数据库连接
     *
     * @param driver   JDBC驱动类名
     * @param url      JDBC数据库URL
     * @param username 用户名
     * @param password 密码
     * @return 数据库连接
     */
    protected Connection create(String driver, String url, String username, String password) {
        this.watch.start();
        if (this.timeout <= 0) {
            if (StringUtils.isBlank(driver)) {
                return Jdbc.getConnection(url, username, password);
            } else {
                ClassUtils.loadClass(driver);
                Connection conn = Jdbc.getConnection(url, username, password);
                DatabaseConfigurationContainer container = this.context.getBean(DatabaseConfigurationContainer.class);
                container.add(new StandardDatabaseConfiguration(this.context, null, driver, url, username, password, null, null, null, null, null));
                return conn;
            }
        } else {
            while (this.watch.useSeconds() <= this.timeout) {
                try {
                    ClassUtils.loadClass(driver);
                    Connection conn = Jdbc.getConnection(url, username, password);
                    DatabaseConfigurationContainer container = this.context.getBean(DatabaseConfigurationContainer.class);
                    container.add(new StandardDatabaseConfiguration(this.context, null, driver, url, username, password, null, null, null, null, null));
                    return conn;
                } catch (Throwable e) {
                    continue;
                }
            }
            throw new DatabaseException(ResourcesUtils.getDataSourceMessage(6));
        }
    }

    /**
     * 把数据库连接返回给连接池
     *
     * @param conn
     */
    public synchronized void returnPool(PoolConnection conn) {
        if (this.isClose) {
            throw new DatabaseException(ResourcesUtils.getDataSourceMessage(3));
        }

        // 遍历空闲的数据库连接，并关闭
        Iterator<PoolConnection> it = this.actives.iterator();
        while (it.hasNext()) {
            PoolConnection proxyConn = it.next();
            if (proxyConn != null && proxyConn.equals(conn)) {
                it.remove();
                break;
            }
        }

        this.out.println(ResourcesUtils.getDataSourceMessage(7, conn.toString()));
        PoolConnection copy = new PoolConnection(conn);
        conn.close();
        this.idles.push(copy);
    }

    /**
     * 从连接池中删除数据库连接
     *
     * @param conn
     */
    public synchronized void remove(PoolConnection conn) {
        if (this.isClose) {
            throw new DatabaseException(ResourcesUtils.getDataSourceMessage(3));
        }

        // 遍历空闲的数据库连接，并关闭
        Iterator<PoolConnection> it = this.actives.iterator();
        while (it.hasNext()) {
            PoolConnection proxy = it.next();
            if (proxy != null && proxy.equals(conn)) {
                it.remove();
                break;
            }
        }

        // 遍历空闲连接的集合
        it = this.idles.iterator();
        while (it.hasNext()) {
            PoolConnection proxyConn = it.next();
            if (proxyConn != null && proxyConn.equals(conn)) {
                it.remove();
                break;
            }
        }

        this.out.println(ResourcesUtils.getDataSourceMessage(8, conn.toString()));
    }

    /**
     * 设置日志输出流
     *
     * @param out
     */
    public void setLogWriter(PrintWriter out) {
        if (out == null) {
            throw new NullPointerException();
        } else {
            this.out = out;
        }
    }

    /**
     * 返回日志输出流
     *
     * @return
     */
    public PrintWriter getLogWriter() {
        return this.out;
    }

    /**
     * 数据库连接池是否关闭
     *
     * @return true关闭
     */
    public boolean isClose() {
        return this.isClose;
    }

    /**
     * 建立数据库连接的超时时间, 单位: 秒
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 建立数据库连接的超时时间, 单位: 秒
     *
     * @param timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 添加 jdbc 配置
     *
     * @param config 配置信息
     */
    public void setConfiguration(Properties config) {
        if (config == null) {
            throw new NullPointerException();
        }

        String url = config.getProperty(Jdbc.url);
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(url);
        }

        this.dialect = this.context.getBean(DatabaseDialect.class, url);
        this.jdbc = this.context.getBean(DatabaseConfigurationContainer.class).add(config).clone();
    }

}
