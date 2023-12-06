package icu.etl.log;

public interface LogLevelAware {

    /**
     * 设置日志输出级别
     *
     * @param level 日志级别
     */
    void setLevel(LogLevel level);

}
