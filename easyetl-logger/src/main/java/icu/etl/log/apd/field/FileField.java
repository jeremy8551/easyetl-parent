package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %F 输出源文件名
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class FileField extends MethodField {

    public String format(LogEvent event) {
        return this.format(event.getStackTraceElement().getFileName());
    }
}
