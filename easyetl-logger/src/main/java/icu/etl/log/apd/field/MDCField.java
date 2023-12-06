package icu.etl.log.apd.field;

import icu.etl.log.MDC;
import icu.etl.log.apd.LogEvent;

/**
 * 用户自定义域中的数值
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class MDCField extends AbstractField {

    private String name;

    public MDCField(String name) {
        this.name = name;
    }

    public String format(LogEvent event) {
        String value = MDC.get(this.name);
        return this.format(value == null ? "" : value);
    }

    public String toString() {
        return "%X{" + this.name + "}";
    }
}
