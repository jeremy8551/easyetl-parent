package icu.etl.log;

import java.io.PrintStream;

/**
 * 适配 Slf4j 日志
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-13
 */
public class Slf4jLoggerBuilder implements LogBuilder {

    /**
     * 根据参数，判断是否能使用 Slf4j 作为日志输出
     *
     * @return true表示使用Slf4j日志接口输出日志
     */
    public static boolean support() {
        return forName("org.slf4j.Logger") != null && forName("org.slf4j.LoggerFactory") != null;
    }

    /**
     * 判断字符串参数className对应的Java类是否存在
     *
     * @param className java类全名
     * @return 返回类信息
     */
    @SuppressWarnings("unchecked")
    private static <E> Class<E> forName(String className) {
        try {
            return (Class<E>) Class.forName(className);
        } catch (Throwable e) {
            return null;
        }
    }

    public Log create(LogFactory factory, Class<?> cls, PrintStream out, PrintStream err, String level) throws Exception {
        return new Slf4jLogger(factory, cls);
    }

}
