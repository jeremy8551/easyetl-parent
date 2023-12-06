package icu.etl.log;

/**
 * 日志接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public interface Log {

    /**
     * 返回日志所属类的名字
     *
     * @return 字符串
     */
    String getName();

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
     * @param args    参数数组
     */
    void trace(String message, Object... args);

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
     * @param args    参数数组
     */
    void debug(String message, Object... args);

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
     * @param args    参数数组
     */
    void info(String message, Object... args);

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
     * @param args    参数数组
     */
    void warn(String message, Object... args);

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
     * @param args    参数数组
     */
    void error(String message, Object... args);

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
     * @param args    参数数组
     */
    void fatal(String message, Object... args);

    /**
     * 输出严重错误信息
     *
     * @param message 字符串
     * @param e       异常信息
     */
    void fatal(String message, Throwable e);

}