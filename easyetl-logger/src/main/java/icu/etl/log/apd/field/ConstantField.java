package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * 字符常量信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class ConstantField extends AbstractField {

    private String str;

    public ConstantField(String str) {
        this.str = str;
    }

    public String format(LogEvent event) {
        return this.str;
    }

    public String toString() {
        return "String[" + this.str + "]";
    }
}
