package icu.etl.database.logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

import icu.etl.log.STD;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 数据库连接日志接口，使用代理方式打印数据库连接上的关键操作信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-09-23
 */
public class ConnectionLogger implements InvocationHandler {

    /**
     * 代理数据库连接对象的序号
     */
    private static int serialNo = 1;

    /**
     * 产生一个唯一序号（大于0）
     *
     * @return
     */
    private synchronized static int getSerialNo() {
        return serialNo++;
    }

    /** 被代理的 Connection 对象 */
    private Connection conn;

    /** 数据库连接编号 */
    private int number;

    /** 超时时间（单位：秒） */
    private int warnTimeout;

    /**
     * 初始化
     *
     * @param conn        数据库连接
     * @param warnTimeout 超时提醒时间，单位秒
     */
    public ConnectionLogger(Connection conn, int warnTimeout) {
        if (conn == null) {
            throw new NullPointerException();
        }
        if (warnTimeout < 0) {
            throw new IllegalArgumentException(String.valueOf(warnTimeout));
        }

        this.number = getSerialNo();
        this.conn = conn;
        this.warnTimeout = warnTimeout;
    }

    /**
     * 返回 Connection 对象的代理
     *
     * @return
     */
    public Connection getProxy() {
        Connection conn = (Connection) Proxy.newProxyInstance(this.conn.getClass().getClassLoader(), new Class[]{Connection.class}, this);
        if (STD.out.isInfoEnabled()) {
            STD.out.info(ResourcesUtils.getDatabaseMessage(34, this.getName(), this.conn.getClass().getName()));
        }
        return conn;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String id = this.getName();

        if (STD.out.isInfoEnabled()) {
            STD.out.info(ResourcesUtils.getDatabaseMessage(35, id, this.conn.getClass().getName(), StringUtils.toString(method), StringUtils.toString(args)));
        }

        TimeWatch watch = new TimeWatch();
        Object value = method.invoke(this.conn, args);
        if (value instanceof Statement) {
            Statement statement = (Statement) value;
            return new StatementLogger(this, statement).getProxy();
        } else {
            if (STD.out.isInfoEnabled()) {
                STD.out.info(ResourcesUtils.getDatabaseMessage(36, id, this.conn.getClass().getName(), StringUtils.toString(method), value, watch.useTime()));
            }
            return value;
        }
    }

    /**
     * 返回 true 表示存在超时警告秒数
     *
     * @return
     */
    public boolean haveOvertimeWarn() {
        return this.warnTimeout > 0;
    }

    /**
     * 返回超时警告秒数
     *
     * @return
     */
    public int getOvertimeWarn() {
        return this.warnTimeout;
    }

    /**
     * 返回数据库连接说明信息
     *
     * @return
     */
    public String getName() {
        return ResourcesUtils.getDatabaseMessage(37, this.number);
    }

}