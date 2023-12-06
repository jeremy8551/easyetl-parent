package icu.etl.log.apd;

import icu.etl.log.Log;
import icu.etl.log.LogBuilder;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;

/**
 * 默认的日志工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public class DefaultLogBuilder implements LogBuilder {

    /**
     * 判断是否支持控制台输出
     *
     * @return 返回true表示支持使用控制台输出日志
     */
    public static boolean support() {
        return System.getProperty(LogFactory.PROPERTY_LOG_SOUT) != null;
    }

    public DefaultLogBuilder() {
    }

    public Log create(LogContext context, Class<?> type, String fqcn, boolean dynamicCategory) throws Exception {
        LogLevel level = context.getLevel(type);
        DefaultLog log = new DefaultLog(context, type, level, fqcn, dynamicCategory);
        context.addLog(log);
        return log;
    }
}
