package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %M：输出产生日志信息的方法名。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class MethodField extends AbstractField {

    public String format(LogEvent event) {
        return this.format(event.getStackTraceElement().getMethodName());
    }
}

