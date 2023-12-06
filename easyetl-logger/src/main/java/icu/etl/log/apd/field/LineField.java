package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.util.FileUtils;

/**
 * %l 输出日志时间发生的位置，包括类名、线程、及在代码中的行数。如：Test.main(Test.java:10)
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class LineField extends MethodField {

    public String format(LogEvent event) {
        StackTraceElement trace = event.getStackTraceElement();
        StringBuffer buf = new StringBuffer();
        buf.append(FileUtils.getFilenameNoExt(trace.getFileName()));
        buf.append('.');
        buf.append(trace.getMethodName());
        buf.append('(');
        buf.append(trace.getFileName());
        buf.append(':');
        buf.append(trace.getLineNumber());
        buf.append(')');
        return this.format(buf);
    }
}
