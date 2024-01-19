package icu.etl.database.pool;

import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
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
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;
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
    protected volatile boolean close;

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
     * @param config  JDBC配置
     */
    public Pool(EasyContext context, Properties config) {
        super();

        Ensure.notNull(context);
        Ensure.notNull(config);
        String url = Ensure.notBlank(config.getProperty(Jdbc.url));

        this.close = true;
        this.watch = new TimeWatch();
        this.context = context;
        this.out = new PrintWriter(new OutputStreamLogger(log, StringUtils.CHARSET));
        this.actives = new PoolConnectionList();
        this.idles = new PoolConnectionList();
        this.timeout = 0;
        this.dialect = this.context.getBean(DatabaseDialect.class, url);
        this.jdbc = this.context.getBean(DatabaseConfigurationContainer.class).add(config).clone();
        this.close = false;
    }

    public EasyContext getContext() {
        return context;
    }

    /**
     * 关闭数据库连接池
     */
    public synchronized void close() {
        if (this.close) {
            return;
        }

        this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg002", SimpleDatasource.class.getName(), this.idles.size(), this.actives.size()));
        this.idles.close();
        this.actives.close();
        this.dialect = null;
        this.close = true;
    }

    /**
     * 返回一个数据库连接
     *
     * @param username 用户名
     * @param password 密码
     * @return 数据库连接代理
     * @throws SQLException 数据库连接已关闭
     */
    public synchronized ConnectionProxy getConnection(String username, String password) throws SQLException {
        if (this.close) {
            throw new SQLException(ResourcesUtils.getMessage("dataSource.standard.output.msg003"));
        }

        if (username == null) {
            OSAccount account = this.jdbc.getAccount();
            if (account != null) {
                username = account.getUsername();
                password = account.getPassword();
            }
        }

        PoolConnection poolConn = this.findPoolConnection(username);
        if (poolConn == null) {
            String driver = this.jdbc.getDriverClass();
            String url = this.jdbc.getUrl();

            Connection conn = this.create(driver, url, username, password);
            poolConn = new PoolConnection(this, conn, username, password);
            this.actives.push(poolConn);
            this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg004", poolConn.toString()));
            return poolConn.getProxy();
        } else {
            Connection conn = poolConn.getConnection();
            try {
                if (Jdbc.canUse(conn)) {
                    this.actives.push(poolConn);
                    this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg005", poolConn.toString()));
                    return poolConn.getProxy();
                } else {
                    Jdbc.commitQuiet(conn);
                    IO.closeQuiet(conn);
                    poolConn.close();
                }
            } catch (Throwable e) {
                if (Jdbc.testConnection(conn, this.dialect)) {
                    this.actives.push(poolConn);
                    this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg005", poolConn.toString()));
                    return poolConn.getProxy();
                } else {
                    Jdbc.commitQuiet(conn);
                    IO.closeQuiet(conn);
                    poolConn.close();
                }
            }

            return this.getConnection(username, password);
        }
    }

    /**
     * 查找闲置的数据库连接
     *
     * @param username 用户名
     * @return 数据库连接
     */
    protected PoolConnection findPoolConnection(String username) {
        for (int i = 0; i < this.idles.size(); i++) {
            PoolConnection poolConn = this.idles.get(i);
            if (poolConn != null && StringComparator.compareTo(poolConn.getUsername(), username) == 0 && poolConn.getConnection() != null) {
                this.idles.remove(i);
                return poolConn;
            }
        }
        return null;
    }

    /**
     * 建立数据库连接
     *
     * @param driver   驱动类名
     * @param url      数据库URL
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
                    if (log.isDebugEnabled()) {
                        log.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
            throw new DatabaseException(ResourcesUtils.getMessage("dataSource.standard.output.msg006"));
        }
    }

    /**
     * 把数据库连接返回给连接池
     *
     * @param conn 数据库连接
     * @throws SQLException 数据库连接已关闭
     */
    public synchronized void returnPool(PoolConnection conn) throws SQLException {
        if (this.close) {
            throw new SQLException(ResourcesUtils.getMessage("dataSource.standard.output.msg003"));
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

        this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg007", conn.toString()));
        PoolConnection copy = new PoolConnection(conn);
        conn.close();
        this.idles.push(copy);
    }

    /**
     * 从连接池中删除数据库连接
     *
     * @param conn 数据库连接
     * @throws SQLException 数据库连接已关闭
     */
    public synchronized void remove(PoolConnection conn) throws SQLException {
        if (this.close) {
            throw new SQLException(ResourcesUtils.getMessage("dataSource.standard.output.msg003"));
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

        this.out.println(ResourcesUtils.getMessage("dataSource.standard.output.msg008", conn.toString()));
    }

    /**
     * 设置日志输出流
     *
     * @param out 日志输出流
     */
    public void setLogWriter(PrintWriter out) {
        this.out = Ensure.notNull(out);
    }

    /**
     * 返回日志输出流
     *
     * @return 日志输出流
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
        return this.close;
    }

    /**
     * 建立数据库连接的超时时间, 单位: 秒
     *
     * @return 超时时间
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 建立数据库连接的超时时间, 单位: 秒
     *
     * @param timeout 超时时间
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
