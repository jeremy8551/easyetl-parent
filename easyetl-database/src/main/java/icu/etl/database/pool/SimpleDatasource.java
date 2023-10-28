package icu.etl.database.pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.sql.DataSource;

import icu.etl.database.logger.DataSourceLogger;
import icu.etl.database.logger.DataSourceLoggerProxy;
import icu.etl.ioc.EasyContext;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.IO;

/**
 * 即时使用的数据库连接池
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-14
 */
public class SimpleDatasource implements DataSource, java.io.Closeable {

    /**
     * 判断参数 dataSource 是否是 {@linkplain SimpleDatasource} 类的实例对象
     *
     * @param dataSource 数据库连接池
     * @return
     */
    public static boolean instanceOf(DataSource dataSource) {
        if (dataSource instanceof SimpleDatasource) {
            return true;
        } else if (dataSource instanceof DataSourceLoggerProxy) {
            String str = dataSource.toString().substring(DataSourceLogger.class.getName().length());
            return str.startsWith(SimpleDatasource.class.getName());
        } else {
            return false;
        }
    }

    /** 连接池 */
    private Pool pool;

    /** 日志接口 */
    private Logger logger;

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     * @param p       配置信息
     */
    public SimpleDatasource(EasyContext context, Properties p) {
        super();
        this.pool = new Pool(context);
        this.pool.setJdbc(p);
        this.pool.start();
        this.init();
    }

    /**
     * 准备日志接口
     */
    private void init() {
        String name = ClassUtils.getPackageName(SimpleDatasource.class, 4);
        this.logger = Logger.getLogger(name);
        this.logger.addHandler(new Handler() {

            public void publish(LogRecord record) {
                STD.out.write(record.getMessage());
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

        });
    }

    public void close() {
        IO.close(this.pool);
    }

    public Connection getConnection() {
        return this.pool.getConnection(null, null);
    }

    public Connection getConnection(String username, String password) {
        return this.pool.getConnection(username, password);
    }

    public PrintWriter getLogWriter() {
        return this.pool.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) {
        this.pool.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return this.pool.getTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        this.pool.setTimeout(seconds);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(ConnectionProxy.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface != null && iface.isAssignableFrom(Connection.class)) {
            return (T) this.pool.getConnection(null, null).getOrignalConnection();
        } else {
            throw new SQLException(iface == null ? "" : iface.getName());
        }
    }

    public Logger getParentLogger() {
        return this.logger;
    }

    public String toString() {
        return SimpleDatasource.class.getName() + super.toString();
    }

}