package icu.etl.database.logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
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
    private final static Log log = LogFactory.getLog(StatementLogger.class);

    /** 被代理的 Statement 对象 */
    private Object statement;

    /** 计时器, 用于判断函数是否执行超时 */
    private TimeWatch watch;

    /** Statement接口或子接口的类信息 */
    private Class<?> returnType;

    /** 数据库连接信息 */
    private String id;

    /**
     * 初始化
     *
     * @param id         编号
     * @param statement  处理器
     * @param returnType 返回类型
     */
    public StatementLogger(String id, Object statement, Class<?> returnType) {
        this.id = id;
        this.watch = new TimeWatch();
        this.statement = Ensure.notNull(statement);
        this.returnType = returnType;
    }

    /**
     * 返回 Statement 对象的代理
     *
     * @return 代理对象
     */
    public Object getProxy() {
        return Proxy.newProxyInstance(this.statement.getClass().getClassLoader(), new Class[]{this.returnType}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (log.isInfoEnabled()) {
            log.info(ResourcesUtils.getMessage("database.standard.output.msg035", this.id, this.statement.getClass().getName(), StringUtils.toString(method), Arrays.toString(args)));
        }

        this.watch.start();
        Object value = method.invoke(this.statement, args);
        if (log.isInfoEnabled()) {
            log.info(ResourcesUtils.getMessage("database.standard.output.msg036", this.id, this.statement.getClass().getName(), StringUtils.toString(method), value, this.watch.useTime()));
        }
        return value;
    }

}
