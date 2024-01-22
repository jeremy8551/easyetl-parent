package icu.etl.database.logger;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import icu.etl.database.pool.SimpleDatasource;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Ensure;

public class DataSourceLogger implements InvocationHandler {

    /** 被代理的 DataSource 对象 */
    private DataSource dataSource;

    /** 容器上下文信息 */
    private EasyContext context;

    /**
     * 初始化
     *
     * @param context    容器上下文信息
     * @param dataSource 数据源
     */
    public DataSourceLogger(EasyContext context, SimpleDatasource dataSource) {
        this.context = Ensure.notNull(context);
        this.dataSource = Ensure.notNull(dataSource);
    }

    /**
     * 返回 DataSource 对象的代理
     *
     * @return 代理接口
     */
    public DataSourceLoggerProxy getProxy() {
        if (this.dataSource instanceof DataSourceLoggerProxy) {
            return (DataSourceLoggerProxy) this.dataSource;
        } else {
            return (DataSourceLoggerProxy) Proxy.newProxyInstance(this.context.getClassLoader(), new Class[]{DataSourceLoggerProxy.class}, this);
        }
    }

    /**
     * 对 {@linkplain DataSource#getConnection()} 和 {@linkplain DataSource#getConnection(String, String)} 方法进行代理
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 返回被代理的数据库连接池
        if ("getOrignalDataSource".equals(method.getName()) && ArrayUtils.isEmpty(args)) { /** 对应 {@linkplain DataSourceLoggerProxy} 接口中的方法 */
            return this.dataSource;
        }

        // 从连接池中返回一个数据库连接，并对数据库连接进行代理
        Object value = method.invoke(this.dataSource, args); // 执行方法
        if ((value instanceof Connection) && "getConnection".equals(method.getName())) {
            Connection conn = (Connection) value;
            return new ConnectionLogger(conn, 0).getProxy();
        } else {
            return value;
        }
    }

}