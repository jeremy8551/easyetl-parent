package icu.etl.database.logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Statement;

import icu.etl.log.STD;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * Statement 代理
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-09-23
 */
public class StatementLogger implements InvocationHandler {

    /** 被代理数据库连接 */
    private ConnectionLogger connLogger;

    /** 被代理的 Statement 对象 */
    private Statement statement;

    /** 计时器, 用于判断函数是否执行超时 */
    private TimeWatch watch;

    /**
     * 初始化
     *
     * @param logger
     * @param statement
     */
    public StatementLogger(ConnectionLogger logger, Statement statement) {
        if (logger == null) {
            throw new NullPointerException();
        }
        if (statement == null) {
            throw new NullPointerException();
        }

        this.watch = new TimeWatch();
        this.connLogger = logger;
        this.statement = statement;
    }

    /**
     * 返回 Statement 对象的代理
     *
     * @return
     */
    public Statement getProxy() {
        return (Statement) Proxy.newProxyInstance(this.statement.getClass().getClassLoader(), new Class[]{Statement.class}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (STD.out.isInfoEnabled()) {
            STD.out.info(ResourcesUtils.getDatabaseMessage(35, this.connLogger.getName(), this.statement.getClass().getName(), StringUtils.toString(method), StringUtils.toString(args)));
        }

        this.watch.start();
        Object value = method.invoke(this.statement, args);
        if (STD.out.isInfoEnabled()) {
            STD.out.info(ResourcesUtils.getDatabaseMessage(36, this.connLogger.toString(), this.statement.getClass().getName(), StringUtils.toString(method), value, this.watch.useTime()));
        }
        return value;
    }

}
