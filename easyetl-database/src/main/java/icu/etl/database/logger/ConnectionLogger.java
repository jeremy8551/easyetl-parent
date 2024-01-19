package icu.etl.database.logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
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
    private final static Log log = LogFactory.getLog(ConnectionLogger.class);

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
     * @return 数据库连接
     */
    public Connection getProxy() {
        Connection conn = (Connection) Proxy.newProxyInstance(this.conn.getClass().getClassLoader(), new Class[]{Connection.class}, this);
        if (log.isInfoEnabled()) {
            log.info(ResourcesUtils.getMessage("database.standard.output.msg034", this.getName(), this.conn.getClass().getName()));
        }
        return conn;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String id = this.getName();
        if (log.isInfoEnabled()) {
            log.info(ResourcesUtils.getMessage("database.standard.output.msg035", id, this.conn.getClass().getName(), StringUtils.toString(method), StringUtils.toString(args)));
        }

        TimeWatch watch = new TimeWatch();
        Object value = method.invoke(this.conn, args);
        try {
            if (value instanceof Statement) {
                return new StatementLogger(id, value, method.getReturnType()).getProxy(); // 生成 Statement 代理
            } else {
                return value;
            }
        } finally {
            if (log.isInfoEnabled()) {
                log.info(ResourcesUtils.getMessage("database.standard.output.msg036", id, this.conn.getClass().getName(), StringUtils.toString(method), value, watch.useTime()));
            }
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
        return ResourcesUtils.getMessage("database.standard.output.msg037", this.number);
    }
}