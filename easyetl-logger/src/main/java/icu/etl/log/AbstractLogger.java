package icu.etl.log;

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
        if (context == null) {
            throw new NullPointerException();
        }
        if (type == null) {
            throw new NullPointerException();
        }
        if (level == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.type = type;
        this.name = type.getName();
        this.setLevel(level);
    }

    public String getName() {
        return name;
    }

    public void setLevel(LogLevel level) {
        if (level == null) {
            throw new NullPointerException();
        }

        switch (level) {
            case TRACE:
                this.trace = true;
                this.debug = true;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                return;

            case DEBUG:
                this.trace = false;
                this.debug = true;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                return;

            case INFO:
                this.trace = false;
                this.debug = false;
                this.info = true;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                return;

            case WARN:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = true;
                this.error = true;
                this.fatal = true;
                return;

            case ERROR:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = true;
                this.fatal = true;
                return;

            case FATAL:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = false;
                this.fatal = true;
                return;

            case OFF:
                this.trace = false;
                this.debug = false;
                this.info = false;
                this.warn = false;
                this.error = false;
                this.fatal = false;
                return;
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
