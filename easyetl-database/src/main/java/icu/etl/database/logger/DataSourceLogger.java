package icu.etl.database.logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import javax.sql.DataSource;

import icu.etl.database.Jdbc;
import icu.etl.util.ArrayUtils;

public class DataSourceLogger implements InvocationHandler {

    /** 被代理的 DataSource 对象 */
    private DataSource dataSource;

    /**
     * 初始化
     *
     * @param dataSource
     */
    public DataSourceLogger(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException();
        } else {
            this.dataSource = dataSource;
        }
    }

    /**
     * 返回 DataSource 对象的代理
     *
     * @return
     */
    public DataSourceLoggerProxy getProxy() {
        return (DataSourceLoggerProxy) Proxy.newProxyInstance(this.dataSource.getClass().getClassLoader(), new Class[]{DataSourceLoggerProxy.class}, this);
    }

    /**
     * 对 {@linkplain DataSource#getConnection()} 和 {@linkplain DataSource#getConnection(String, String)} 方法进行代理
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 返回被代理的数据库连接池
        if ("getProxyDataSource".equals(method.getName()) && ArrayUtils.isEmpty(args)) {
            return this.dataSource;
        }

        Object value = method.invoke(this.dataSource, args); // 执行方法
        if ((value instanceof Connection) && "getConnection".equals(method.getName()) && args.length == 0) { // 从连接池中返回一个数据库连接，并对数据库连接进行代理
            return Jdbc.getConnection((Connection) value, 0);
        } else {
            return value;
        }
    }

}