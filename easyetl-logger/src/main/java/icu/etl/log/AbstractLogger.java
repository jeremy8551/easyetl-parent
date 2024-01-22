package icu.etl.log;

import icu.etl.util.Ensure;

/**
 * 抽象日志
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/22
 */
public abstract class AbstractLogger implements Log, LogLevelAware {

    /** 日志模块的上下文信息 */
    protected LogContext context;

    /** 日志所属类 */
    protected Class<?> type;

    /** 日志所属类的名字 */
    protected String name;

    /** 日志级别 */
    protected volatile boolean trace;
    protected volatile boolean debug;
    protected volatile boolean info;
    protected volatile boolean warn;
    protected volatile boolean error;
    protected volatile boolean fatal;

    /**
     * 抽象日志
     *
     * @param context 日志工厂的上下文信息
     * @param type    日志关联类
     * @param level   日志级别
     */
    public AbstractLogger(LogContext context, Class<?> type, LogLevel level) {
        this.setLevel(level);
        this.context = Ensure.notNull(context);
        this.type = Ensure.notNull(type);
        this.name = type.getName();
    }

    public String getName() {
        return name;
    }

    public void setLevel(LogLevel level) {
        switch (level) {
            case TRACE:
                this.trace = true;
                this.debug = true;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                break;

            case DEBUG:
                this.trace = false;
                this.debug = true;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                break;

            case INFO:
                this.trace = false;
                this.debug = false;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                break;

            case WARN:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                break;

            case ERROR:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = true;
                this.fatal = true;
                break;

            case FATAL:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = false;
                this.fatal = true;
                break;

            case OFF:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = false;
                this.fatal = false;
                break;

            default:
                throw new UnsupportedOperationException(String.valueOf(level));
        }
    }

    public boolean isTraceEnabled() {
        return trace;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public boolean isInfoEnabled() {
        return info;
    }

    public boolean isWarnEnabled() {
        return warn;
    }

    public boolean isErrorEnabled() {
        return error;
    }

    public boolean isFatalEnabled() {
        return fatal;
    }

}
