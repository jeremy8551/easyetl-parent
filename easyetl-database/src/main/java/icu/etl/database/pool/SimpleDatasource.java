package icu.etl.database.pool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import icu.etl.database.logger.DataSourceLogger;
import icu.etl.database.logger.DataSourceLoggerProxy;
import icu.etl.ioc.EasyContext;
import icu.etl.util.IO;

/**
 * 即时使用的数据库连接池
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-14
 */
public class SimpleDatasource implements DataSource, java.io.Closeable {

    /** 日志接口 */
    public static Logger logger = Logger.getLogger(SimpleDatasource.class.getName());

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

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     * @param p       配置信息
     */
    public SimpleDatasource(EasyContext context, Properties p) {
        super();
        this.pool = new Pool(context, p);
    }

    public EasyContext getContext() {
        return this.pool.getContext();
    }

    public void close() {
        IO.close(this.pool);
    }

    public Connection getConnection() throws SQLException {
        return this.pool.getConnection(null, null);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return this.pool.getConnection(username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.pool.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
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
    public <T> T unwrap(Class<T> cls) throws SQLException {
        if (cls != null && cls.isAssignableFrom(Connection.class)) {
            return (T) this.pool.getConnection(null, null).getOrignalConnection();
        } else {
            throw new SQLException(cls == null ? "" : cls.getName());
        }
    }

    public Logger getParentLogger() {
        return logger;
    }

    public String toString() {
        return SimpleDatasource.class.getName() + super.toString();
    }

}