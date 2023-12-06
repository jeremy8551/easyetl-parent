package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;

/**
 * %t：输出产生该日志事件的线程名。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class ThreadName extends AbstractField {

    public String format(LogEvent event) {
        return this.format(Thread.currentThread().getName());
    }
}
