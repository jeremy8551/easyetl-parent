package icu.etl.log;

/**
 * 日志接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public interface Log {

    /** JVM参数名，用来设置日志级别，详见 {@linkplain #LEVEL} */
    public final static String PROPERTY_LOGGER = Log.class.getPackage().getName().split("\\.")[0] + "." + Log.class.getPackage().getName().split("\\.")[1] + ".logger";

    /** JVM参数名（没有参数值），如果设置参数默认使用控制台输出日志 */
    public final static String PROPERTY_LOGGERSOUT = Log.class.getPackage().getName().split("\\.")[0] + "." + Log.class.getPackage().getName().split("\\.")[1] + ".logger.sout";

    /** 默认的日志级别 */
    public final static String DEFAULT_LEVEL = System.getProperty(Log.PROPERTY_LOGGER, "info");

    /** 日志输出级别，从低到高 */
    public final static String[] LEVEL = {"trace", "debug", "info", "warn", "error", "fatal"};

    /**
     * 判断日志级别是否是跟踪模式
     *
     * @return 是Trace这个级别
     */
    boolean isTraceEnabled();

    /**
     * 判断日志级别是否是调试模式
     *
     * @return 是Debug这个级别
     */
    boolean isDebugEnabled();

    /**
     * 判断日志级别是否是正常模式
     *
     * @return 是Info这个级别
     */
    boolean isInfoEnabled();

    /**
     * 判断日志级别是否是警告模式
     *
     * @return 是Warn这个级别
     */
    boolean isWarnEnabled();

    /**
     * 判断日志级别是否是错误模式
     *
     * @return 是Error这个级别
     */
    boolean isErrorEnabled();

    /**
     * 判断日志级别是否是严重级别
     *
     * @return 是Fatal这个级别
     */
    boolean isFatalEnabled();

    /**
     * 输出跟踪信息
     *
     * @param message 字符串
     */
    void trace(String message);

    /**
     * 输出跟踪信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void trace(String message, Throwable e);

    /**
     * 输出调试信息
     *
     * @param message 字符串
     */
    void debug(String message);

    /**
     * 输出调试信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void debug(String message, Throwable e);

    /**
     * 输出一般信息
     *
     * @param message 字符串
     */
    void info(String message);

    /**
     * 输出一般信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void info(String message, Throwable e);

    /**
     * 输出警告信息
     *
     * @param message 字符串
     */
    void warn(String message);

    /**
     * 输出警告信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void warn(String message, Throwable e);

    /**
     * 输出错误信息
     *
     * @param message 字符串
     */
    void error(String message);

    /**
     * 输出错误信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void error(String message, Throwable e);

    /**
     * 输出严重错误信息
     *
     * @param message 字符串
     */
    void fatal(String message);

    /**
     * 输出严重错误信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void fatal(String message, Throwable e);

    /**
     * 不带日志级别的输出信息
     *
     * @param message 字符串
     */
    void write(CharSequence message);

}