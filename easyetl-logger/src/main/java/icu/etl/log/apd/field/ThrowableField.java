package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * %ex: 同 %throwable 都是输出异常信息。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class ThrowableField extends AbstractField {

    public String format(LogEvent event) {
        Throwable e = event.getThrowable();
        if (e == null) {
            return this.format("");
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(FileUtils.lineSeparator);
            buf.append(StringUtils.toString(e));
            return this.format(buf);
        }
    }
}
