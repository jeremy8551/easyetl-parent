package icu.etl.log.apd;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.StringUtils;

/**
 * 日志格式
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class PatternLayout implements Layout {

    private StringBuilder buf;

    /** 临时集合 */
    private List<CharSequence> list;

    /** 日志格式 */
    private LogPattern pattern;

    public PatternLayout(String pattern) {
        this.buf = new StringBuilder(100);
        this.list = new ArrayList<CharSequence>();
        this.pattern = new LogPattern(pattern);
    }

    /**
     * 初始化一个日志信息
     *
     * @param event 日志事件
     * @return 日志记录
     */
    public synchronized String format(LogEvent event) {
        this.buf.setLength(0);
        String message = event.getMessage();
        if (StringUtils.contains(message, '\r', '\n')) {
            StringUtils.splitLines(message, this.list);
            this.addRows(event);
        } else {
            this.addRow(event);
        }
        return this.buf.toString();
    }

    /**
     * 将日志信息按多行输出
     *
     * @param event 记录一个日志的事件
     */
    public void addRows(LogEvent event) {
        Object[] args = event.getArgs();
        Throwable exception = event.getThrowable();

        List<LogField> fields = this.pattern.getFields();
        for (int i = 0, size = this.list.size(); i < size; i++) {
            String line = this.list.get(i).toString();

            for (int j = 0; j < fields.size(); j++) {
                LogField field = fields.get(j);
                if (i + 1 == size) { // 最后一行才添加异常信息
                    this.buf.append(field.format(event.clone(line, args, exception)));
                } else {
                    this.buf.append(field.format(event.clone(line, args, null)));
                }
            }
        }
    }

    /**
     * 将日志信息按一行输出
     *
     * @param event 记录一个日志的事件
     */
    public void addRow(LogEvent event) {
        List<LogField> fields = this.pattern.getFields();
        for (int i = 0; i < fields.size(); i++) {
            this.buf.append(fields.get(i).format(event));
        }
    }

    public String getPattern() {
        return this.pattern.getName();
    }
}
