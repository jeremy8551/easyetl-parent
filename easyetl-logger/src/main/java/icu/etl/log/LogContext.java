package icu.etl.log;

import java.util.List;

/**
 * 日志模块的上下文信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/22
 */
public interface LogContext {

    /**
     * 加载日志配置信息
     *
     * @param str 日志配置信息
     *            sout: 表示使用控制台输出info级别的日志 <br>
     *            slf4j: 表示使用slf4j输出debug级别的日志 <br>
     *            :info 设置默认的日志级别  <br>
     *            info: 设置默认的日志级别  <br>
     *            icu.etl.db:debug 设置包名下的日志级别 <br>
     *            info:icu.etl.io 设置包名下的日志级别 <br>
     * @return 返回true表示解析成功 false表示失败
     */
    boolean load(String str);

    /**
     * 设置日志输出级别
     *
     * @param name  包名或类名
     * @param level 日志级别
     */
    void updateLevel(String name, LogLevel level);

    /**
     * 返回类的日志对应的日志级别
     *
     * @param type 类信息
     * @return 日志级别
     */
    LogLevel getLevel(Class<?> type);

    /**
     * 返回应用的启动时间戳
     *
     * @param millis 时间戳
     */
    void setStartMillis(long millis);

    /**
     * 返回应用的启动时间戳
     *
     * @return 时间戳
     */
    long getStartMillis();

    /**
     * 添加日志
     *
     * @param log 日志接口
     */
    void addLog(Log log);

    /**
     * 设置日志工厂
     *
     * @param builder 日志工厂
     */
    void setBuilder(LogBuilder builder);

    /**
     * 返回日志工厂
     *
     * @return 日志工厂
     */
    LogBuilder getBuilder();

    /**
     * 查找指定类的日志记录器
     *
     * @param type 记录器的类信息
     * @return 返回 null 表示不存在
     */
    <E> E findAppender(Class<E> type);

    /**
     * 添加一个记录器
     *
     * @param appender 记录器
     */
    void addAppender(Appender appender);

    /**
     * 移除记录器
     *
     * @param where 移除条件，可以是：记录器对象、记录器名字、记录器的Class
     * @return 记录器集合
     */
    List<Appender> removeAppender(Object where);

    /**
     * 返回所有记录器集合
     *
     * @return 记录器集合
     */
    List<Appender> getAppenders();
}
