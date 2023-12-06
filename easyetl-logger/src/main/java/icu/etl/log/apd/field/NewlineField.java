package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.util.FileUtils;

/**
 * %n：输出一个回车换行符，Windows平台为”rn”，Unix平台为”n”。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class NewlineField extends AbstractField {

    public String format(LogEvent event) {
        return this.format(FileUtils.lineSeparator);
    }
}
