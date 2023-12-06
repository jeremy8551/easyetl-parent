package icu.etl.log.apd;

/**
 * 日志格式中的字段
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public interface LogField {

    /**
     * 对齐方式
     *
     * @param align 对齐方式
     */
    void setAlign(LogFieldAlign align);

    /**
     * 格式化为字符串
     *
     * @param event 记录一个日志的事件
     * @return 字符串
     */
    String format(LogEvent event);
}
