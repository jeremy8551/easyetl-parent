package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %r：输出自应用程序启动到输出该log信息耗费的毫秒数。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class MillsField extends AbstractField {

    public String format(LogEvent event) {
        return this.format(String.valueOf(System.currentTimeMillis() - event.getContext().getStartMillis()));
    }
}
