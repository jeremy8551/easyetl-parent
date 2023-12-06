package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %L 输出代码中的行号。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class LinenoField extends MethodField {

    public String format(LogEvent event) {
        return this.format(String.valueOf(event.getStackTraceElement().getLineNumber()));
    }
}
