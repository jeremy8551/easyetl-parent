package icu.etl.log;

import icu.etl.util.StringUtils;

/**
 * 日志级别
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/21
 */
public enum LogLevel {

    TRACE(), //
    DEBUG(), //
    INFO(), //
    WARN(), //
    ERROR(), //
    FATAL(), //
    OFF() //
    ;

    LogLevel() {
    }

    /**
     * 将字符串转为日志级别
     *
     * @param level 字符串
     * @return 日志级别
     */
    public static LogLevel of(String level) {
        level = level.trim();
        if ("trace".equalsIgnoreCase(level)) {
            return TRACE;
        }

        if ("debug".equalsIgnoreCase(level)) {
            return DEBUG;
        }

        if ("info".equalsIgnoreCase(level)) {
            return INFO;
        }

        if ("warn".equalsIgnoreCase(level)) {
            return WARN;
        }

        if ("error".equalsIgnoreCase(level)) {
            return ERROR;
        }

        if ("fatal".equalsIgnoreCase(level)) {
            return FATAL;
        }

        if ("off".equalsIgnoreCase(level)) {
            return OFF;
        }

        throw new UnsupportedOperationException(level);
    }

    /**
     * 判断字符串是否是日志级别
     *
     * @param level 字符串
     * @return 返回true表示是日志级别，false不是日志级别
     */
    public static boolean is(String level) {
        if (level == null) {
            return false;
        }

        level = StringUtils.trimBlank(level);
        if ("trace".equalsIgnoreCase(level)) {
            return true;
        }

        if ("debug".equalsIgnoreCase(level)) {
            return true;
        }

        if ("info".equalsIgnoreCase(level)) {
            return true;
        }

        if ("warn".equalsIgnoreCase(level)) {
            return true;
        }

        if ("error".equalsIgnoreCase(level)) {
            return true;
        }

        if ("fatal".equalsIgnoreCase(level)) {
            return true;
        }

        return "off".equalsIgnoreCase(level);
    }

}
