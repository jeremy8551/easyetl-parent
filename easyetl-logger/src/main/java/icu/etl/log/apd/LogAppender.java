package icu.etl.log.apd;

import icu.etl.log.Appender;
import icu.etl.log.LogContext;

/**
 * 记录器，用于获取输出的日志内容
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/26
 */
public class LogAppender extends ConsoleAppender implements Appender {

    /** 日志内容 */
    private String value;

    public LogAppender(String pattern) {
        super(pattern);
    }

    public Appender setup(LogContext context) {
        context.removeAppender(LogAppender.class);
        context.addAppender(this);
        return this;
    }

    public String getName() {
        return LogAppender.class.getSimpleName();
    }

    public void append(LogEvent event) {
        this.value = this.layout.format(event);
    }

    public void close() {
        this.value = null;
    }

    /**
     * 返回日志内容
     *
     * @return 字符串
     */
    public String getValue() {
        return value;
    }
}
