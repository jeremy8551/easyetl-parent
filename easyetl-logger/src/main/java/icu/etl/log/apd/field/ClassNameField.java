package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.util.FileUtils;

/**
 * %C - java 类名，%C{1} 输出最后一个元素
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class ClassNameField extends MethodField {

    public String format(LogEvent event) {
        return this.format(FileUtils.getFilenameNoExt(event.getStackTraceElement().getFileName()));
    }
}
