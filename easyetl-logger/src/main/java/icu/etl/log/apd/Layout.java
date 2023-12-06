package icu.etl.log.apd;

/**
 * 日志格式
 */
public interface Layout {

    /**
     * 格式化后输出日志信息
     *
     * @param event 日志事件
     * @return 日志信息
     */
    String format(LogEvent event);

    /**
     * 返回日志格式
     *
     * @return 日志格式
     */
    String getPattern();
}
