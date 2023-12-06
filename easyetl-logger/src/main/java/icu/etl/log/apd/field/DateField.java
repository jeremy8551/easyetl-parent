package icu.etl.log.apd.field;

import java.text.SimpleDateFormat;
import java.util.Date;

import icu.etl.log.apd.LogEvent;

/**
 * %d：输出日志时间点的日期或时间，也可以在其后指定格式，如：%d{yyyy/MM/dd HH:mm:ss,SSS}。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class DateField extends AbstractField {

    private String pattern;

    private SimpleDateFormat format;

    public DateField(String pattern) {
        this.pattern = pattern;
        this.format = new SimpleDateFormat(pattern);
    }

    public String format(LogEvent event) {
        return this.format(this.format.format(new Date()));
    }

    public String toString() {
        return "DatePattern[" + this.pattern + "]";
    }
}
