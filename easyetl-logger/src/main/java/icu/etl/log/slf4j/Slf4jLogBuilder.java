package icu.etl.log.slf4j;

import icu.etl.log.Log;
import icu.etl.log.LogBuilder;
import icu.etl.log.LogContext;
import icu.etl.util.ClassUtils;

/**
 * 适配 Slf4j 日志
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-13
 */
public class Slf4jLogBuilder implements LogBuilder {

    /**
     * 根据参数，判断是否能使用 Slf4j 作为日志输出
     *
     * @return true表示使用Slf4j日志接口输出日志
     */
    public static boolean support() {
        return ClassUtils.forName("org.slf4j.Logger") != null && ClassUtils.forName("org.slf4j.LoggerFactory") != null;
    }

    public Log create(LogContext context, Class<?> type, String fqcn, boolean dynamicCategory) throws Exception {
        return new Slf4jLog(type, fqcn);
    }

}
