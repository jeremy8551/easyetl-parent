package icu.etl.log;

import java.io.PrintStream;

import icu.etl.util.StringUtils;

/**
 * 日志工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public class LogFactory {

    /**
     * 解析日志配置信息
     *
     * @param str 日志配置信息
     *            sout:info 表示使用控制台输出info级别的日志
     *            slf4j:debug 表示使用slf4j输出debug级别的日志
     *            :info 表示使用默认日志工厂流输出info级别的日志
     * @return 返回true表示解析成功 false表示失败
     */
    public static boolean parse(String str) {
        if (str == null || str.indexOf(':') == -1) {
            return false;
        }

        String[] array = StringUtils.split(str, ':');
        if (array.length > 2) {
            throw new IllegalArgumentException(str);
        }

        // 使用控制台输出
        if (StringUtils.inArrayIgnoreCase("sout", array)) {
            LogFactory.setbuilder(new ConsoleLoggerBuilder());
        }

        // 使用slf4j输出
        if (StringUtils.inArrayIgnoreCase("slf4j", array)) {
            LogFactory.setbuilder(new Slf4jLoggerBuilder());
        }

        // 使用标准输出
        if (StringUtils.inArrayIgnoreCase("", array)) {
            LogFactory.setbuilder(new DefaultLoggerBuilder());
        }

        // 如果是日志等级参数
        if (StringUtils.inArrayIgnoreCase(array[0], Log.LEVEL)) {
            System.setProperty(Log.PROPERTY_LOGGER, array[0]);
        }

        // 如果是日志等级参数
        if (StringUtils.inArrayIgnoreCase(array[1], Log.LEVEL)) {
            System.setProperty(Log.PROPERTY_LOGGER, array[1]);
        }

        return true;
    }

    /** 唯一实例 */
    protected final static LogFactory INSTANCE = new LogFactory();

    /** true表示关闭信息输出接口 */
    private volatile boolean disable;

    /** 日志工厂 */
    private LogBuilder builder;

    /**
     * 初始化
     */
    private LogFactory() {
        this.disable = false;

        // 使用控制台输出
        if (ConsoleLoggerBuilder.support()) {
            this.builder = new ConsoleLoggerBuilder();
            return;
        }

        // 判断是否能使用 slf4j
        if (Slf4jLoggerBuilder.support()) {
            this.builder = new Slf4jLoggerBuilder();
            return;
        }

        // 默认的日志输出接口
        this.builder = new DefaultLoggerBuilder();
    }

    /**
     * 返回 true 表示已关闭日志输出
     *
     * @return true表示可以输出日志
     */
    public boolean isDisable() {
        return this.disable;
    }

    /**
     * 打开或关闭日志输出
     *
     * @param status true 表示打开
     *               false 表示关闭
     */
    private void setEnable(boolean status) {
        this.disable = !status;
    }

    /**
     * 生成一个日志
     *
     * @param cls 返回的日志接口将以cls命名
     * @param out 标准信息输出接口
     * @param err 错误信息输出接口
     * @return 日志接口
     */
    protected synchronized Log create(Class<?> cls, PrintStream out, PrintStream err) {
        if (cls == null) {
            throw new NullPointerException();
        }

        try {
            return this.builder.create(LogFactory.INSTANCE, cls, out, err, LogFactory.getLevel());
        } catch (Throwable e) {
            throw new LogException(cls.getName(), e);
        }
    }

    /**
     * 设置日志工厂
     *
     * @param builder 日志工厂
     * @return 日志工厂
     */
    public static synchronized LogFactory setbuilder(LogBuilder builder) {
        if (builder == null) {
            throw new NullPointerException();
        }

        LogFactory.INSTANCE.builder = builder;
        return LogFactory.INSTANCE;
    }

    /**
     * 返回日志输出的级别
     *
     * @return 详见 {@linkplain Log#LEVEL}
     */
    public static String getLevel() {
        String level = System.getProperty(Log.PROPERTY_LOGGER);
        if (StringUtils.isBlank(level)) {
            return Log.DEFAULT_LEVEL;
        }

        if (StringUtils.inArrayIgnoreCase(level, Log.LEVEL)) {
            return level;
        }

        throw new IllegalArgumentException(level);
    }

    /**
     * 生成一个日志
     *
     * @param cls 产生log事件的类信息
     * @return 日志接口
     */
    public static Log getLog(Class<?> cls) {
        return LogFactory.INSTANCE.create(cls, null, null);
    }

    /**
     * 生成一个日志
     *
     * @param cls 产生log事件的类信息
     * @param out 标准信息输出接口
     * @param err 错误信息输出接口
     * @return 日志接口
     */
    public static Log getLog(Class<?> cls, PrintStream out, PrintStream err) {
        return LogFactory.INSTANCE.create(cls, out, err);
    }

    /**
     * 关闭所有日志输出
     */
    public static void turnOff() {
        LogFactory.INSTANCE.setEnable(false);
    }

    /**
     * 打开所有日志输出
     */
    public static void turnOn() {
        LogFactory.INSTANCE.setEnable(true);
    }

}
