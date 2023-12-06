package icu.etl.log.apd;

import icu.etl.log.Log;
import icu.etl.log.LogContext;
import icu.etl.log.LogLevel;

/**
 * 记录日志的事件
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/21
 */
public interface LogEvent {

    /**
     * 返回一个副本
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param args      参数数组
     * @param throwable 异常信息
     * @return 日志事件副本
     */
    LogEvent clone(LogLevel level, String message, Object[] args, Throwable throwable);

    /**
     * 返回一个副本
     *
     * @param message   日志信息
     * @param args      参数数组
     * @param throwable 异常信息
     * @return 日志事件副本
     */
    LogEvent clone(String message, Object[] args, Throwable throwable);

    /**
     * 用于定位输出日志的代码位置信息的标识符
     *
     * @param fqcn 字符串
     */
    void setFqcn(String fqcn);

    /**
     * 用于定位输出日志的代码位置信息的标识符
     *
     * @return 字符串
     */
    String getFqcn();

    /**
     * 返回 StackTraceElement 对象
     *
     * @return StackTraceElement 对象
     */
    StackTraceElement getStackTraceElement();

    /**
     * 设置 StackTraceElement 对象
     *
     * @param stackTraceElement StackTraceElement 对象
     */
    void setStackTraceElement(StackTraceElement stackTraceElement);

    /**
     * 返回日志接口归属的包名或类名
     *
     * @return 包名或类名
     */
    String getCategory();

    /**
     * 返回日志接口
     *
     * @return 日志接口
     */
    Log getLog();

    /**
     * 返回日志工厂
     *
     * @return 日志工厂
     */
    LogContext getContext();

    /**
     * 返回日志级别
     *
     * @return 日志级别
     */
    LogLevel getLevel();

    /**
     * 返回日志信息
     *
     * @return 日志信息
     */
    String getMessage();

    /**
     * 返回参数数组
     *
     * @return 数组
     */
    Object[] getArgs();

    /**
     * 返回异常信息
     *
     * @return 异常信息
     */
    Throwable getThrowable();

}
