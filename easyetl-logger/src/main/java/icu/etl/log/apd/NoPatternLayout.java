package icu.etl.log.apd;

import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 不带日志格式
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/23
 */
public class NoPatternLayout implements Layout {

    public String format(LogEvent event) {
        String msg = event.getMessage();
        if (msg == null) {
            msg = "";
        }

        StringBuilder buf = new StringBuilder(msg.length() + 70);
        Throwable e = event.getThrowable();
        if (e == null) {
            buf.append(msg);
            buf.append(FileUtils.lineSeparator);
            return buf.toString();
        } else {
            buf.append(msg);
            buf.append(FileUtils.lineSeparator);
            buf.append(StringUtils.toString(e));
            buf.append(FileUtils.lineSeparator);
            return buf.toString();
        }
    }

    public String getPattern() {
        return "";
    }
}
