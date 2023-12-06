package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.util.MessageFormatter;

/**
 * %m: 输出代码中指定的日志信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class MessageField extends AbstractField {

    private MessageFormatter formater;

    public MessageField() {
        this.formater = new MessageFormatter();
    }

    public String format(LogEvent event) {
        String message = event.getMessage();
        if (message == null) {
            message = "";
        }

        Object[] args = event.getArgs();
        if (args != null && args.length > 0) {
            return this.format(this.formater.format(message, args));
        } else {
            return this.format(message);
        }
    }
}
