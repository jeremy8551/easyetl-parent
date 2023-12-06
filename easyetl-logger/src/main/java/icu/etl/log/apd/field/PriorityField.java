package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %p：输出日志信息的优先级，即DEBUG，INFO，WARN，ERROR，FATAL。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class PriorityField extends AbstractField {

    public String format(LogEvent event) {
        return this.format(event.getLevel().name());
    }
}
