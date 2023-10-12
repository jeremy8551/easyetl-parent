package icu.etl.log;

import java.io.PrintStream;

import icu.etl.util.StringUtils;

/**
 * 日志工厂的实现类，使用 System.out 与 System.err 作为默认输出接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-10-09
 */
public class ConsoleLoggerBuilder implements LogBuilder {

    /**
     * 判断字符串参数是否匹配 System.out 标志
     *
     * @return 返回true表示支持使用控制台输出日志
     */
    public static boolean support() {
        return StringUtils.isNotBlank(System.getProperty(Log.PROPERTY_LOGGERSOUT));
    }

    public ConsoleLoggerBuilder() {
    }

    public Log create(LogFactory factory, Class<?> cls, PrintStream out, PrintStream err, String level) throws Exception {
        return new ConsoleLogger(LogFactory.INSTANCE, level);
    }

}
