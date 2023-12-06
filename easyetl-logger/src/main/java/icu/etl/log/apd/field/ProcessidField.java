package icu.etl.log.apd.field;

import java.lang.management.ManagementFactory;

import icu.etl.log.apd.LogEvent;

/**
 * %processId 是一种模式（pattern），用于在日志消息的布局模式中插入进程（Process）的唯一标识符
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class ProcessidField extends AbstractField {

    public String format(LogEvent event) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return this.format(name.split("@")[0]);
    }
}
