package icu.etl.log;

import icu.etl.log.cxt.LogContextImpl;
import icu.etl.util.Ensure;

/**
 * 日志模块
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public class LogFactory {

    /** JVM参数名，日志级别，详见 {@linkplain LogLevel} */
    public final static String PROPERTY_LOG = Log.class.getPackage().getName().split("\\.")[0] + "." + Log.class.getPackage().getName().split("\\.")[1] + ".logger";

    /** JVM参数名，强制使用控制台输出日志, 参数值就是日志格式（详见{@linkplain icu.etl.log.apd.LogPattern}） */
    public final static String PROPERTY_LOG_SOUT = Log.class.getPackage().getName().split("\\.")[0] + "." + Log.class.getPackage().getName().split("\\.")[1] + ".logger.sout";

    /** 默认的日志格式, 详见{@linkplain icu.etl.log.apd.LogPattern} */
    public static String DEFAULT_LOG_PATTERN = "%d|%-5.5p|%30.30c|%50.50l|%m%ex%n";

    /**
     * 初始化
     */
    private LogFactory() {
    }

    /** 单例模式 */
    private static LogContext CONTEXT;

    /** 锁 */
    private final static Object lock = new Object();

    /**
     * 返回日志模块上下文信息
     *
     * @return 上下文信息
     */
    public static LogContext getContext() {
        if (CONTEXT == null) {
            synchronized (lock) {
                if (CONTEXT == null) {
                    CONTEXT = new LogContextImpl();
                }
            }
        }
        return CONTEXT;
    }

    /**
     * 返回一个日志接口
     *
     * @param type 日志归属的类
     * @return 日志接口
     */
    public static Log getLog(Class<?> type) {
        return LogFactory.getLog(LogFactory.getContext(), type, null, false);
    }

    /**
     * 返回一个日志接口
     *
     * @param context 日志模块上下文信息
     * @param type    日志归属的类
     * @return 日志接口
     */
    public static Log getLog(LogContext context, Class<?> type) {
        return LogFactory.getLog(context, type, null, false);
    }

    /**
     * 返回一个日志接口
     *
     * @param context         日志模块上下文信息
     * @param type            日志归属的类
     * @param fqcn            用于定位输出日志的代码位置信息的标识符
     * @param dynamicCategory true表示动态生成日志接口归属的类名, false表示使用 {@code type} 作为日志接口归属的类名
     * @return 日志接口
     */
    public synchronized static Log getLog(LogContext context, Class<?> type, String fqcn, boolean dynamicCategory) {
        Ensure.notNull(context);
        Ensure.notNull(type);

        try {
            return context.getBuilder().create(context, type, fqcn, dynamicCategory);
        } catch (Throwable e) {
            throw new LogException(type.getName(), e);
        }
    }

    /**
     * 返回日志输出格式
     *
     * @param pattern 指定输出格式
     * @param force   true表示：如果未设置日志格式，则使用一个默认值的日志格式
     *                false表示：如果未设置日志格式，则不使用任何格式
     * @return 日志格式
     */
    public static String getPattern(String pattern, boolean force) {
        if (pattern == null || pattern.length() == 0) {
            pattern = System.getProperty(LogFactory.PROPERTY_LOG_SOUT);
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = force ? LogFactory.DEFAULT_LOG_PATTERN : "";
        }
        return pattern;
    }
}
