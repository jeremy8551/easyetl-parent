package icu.etl.log.cxt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import icu.etl.log.Appender;
import icu.etl.log.Log;
import icu.etl.log.LogBuilder;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.log.LogLevelAware;
import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.slf4j.Slf4jLogBuilder;
import icu.etl.util.Ensure;
import icu.etl.util.JUL;

/**
 * 日志模块的上下文信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/22
 */
public class LogContextImpl implements LogContext {

    /** 日志系统启动的时间 */
    private long startTimeMillis;

    /** 日志工厂 */
    private LogBuilder builder;

    /** 已注册的日志接口 */
    private LogPool alives;

    /** 日志级别 */
    private LogLevelManager levelManager;

    /** 日志记录器 */
    private List<Appender> appenders;

    /**
     * 日志模块的上下文信息
     */
    public LogContextImpl() {
        this.startTimeMillis = System.currentTimeMillis();
        this.alives = new LogPool();
        this.levelManager = new LogLevelManager();
        this.appenders = new ArrayList<Appender>();
        this.init();
    }

    /**
     * 初始化日志系统
     */
    public void init() {
        // 安装控制台日志记录器
        ConsoleAppender appender = new ConsoleAppender(LogFactory.getPattern(null, true));
        appender.setup(this);

        // 强制使用默认的日志系统输出日志
        if (DefaultLogBuilder.support()) {
            appender.pattern(LogFactory.getPattern(null, false));
            this.builder = new DefaultLogBuilder();
            return;
        }

        // 判断是否能使用 slf4j-api
        if (Slf4jLogBuilder.support()) {
            this.builder = new Slf4jLogBuilder();
            return;
        }

        // 默认的日志输出接口
        this.builder = new DefaultLogBuilder();
    }

    public boolean load(String str) {
        return new LogContextLoader(this).parse(str);
    }

    public synchronized void updateLevel(String name, LogLevel level) {
        this.levelManager.put(name, level); // 设置日志级别
        List<Log> logs = this.alives.get(name);
        for (Log log : logs) {
            if (log instanceof LogLevelAware) {
                LogLevel logLevel = this.levelManager.get(name);
                ((LogLevelAware) log).setLevel(logLevel);
            }
        }
    }

    public void setStartMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getStartMillis() {
        return startTimeMillis;
    }

    public synchronized void setBuilder(LogBuilder builder) {
        this.builder = Ensure.notNull(builder);
    }

    public LogBuilder getBuilder() {
        return builder;
    }

    @SuppressWarnings("unchecked")
    public <E> E findAppender(Class<E> type) {
        for (Appender next : this.appenders) {
            if (next.getClass().equals(type)) {
                return (E) next;
            }
        }
        return null;
    }

    public void addAppender(Appender appender) {
        this.appenders.add(Ensure.notNull(appender));
    }

    public List<Appender> removeAppender(Object where) {
        ArrayList<Appender> list = new ArrayList<Appender>();
        if (where instanceof Appender) {
            for (Iterator<Appender> it = this.appenders.iterator(); it.hasNext(); ) {
                Appender next = it.next();
                if (next.equals(where)) {
                    it.remove();
                    list.add(next);
                    try {
                        next.close();
                    } catch (Exception e) {
                        JUL.error(String.valueOf(where), e);
                    }
                }
            }
            return list;
        } else if (where instanceof String) {
            for (Iterator<Appender> it = this.appenders.iterator(); it.hasNext(); ) {
                Appender next = it.next();
                if (next.getName().equals(where)) {
                    it.remove();
                    list.add(next);
                }
            }
            return list;
        } else if (where instanceof Class) {
            for (Iterator<Appender> it = this.appenders.iterator(); it.hasNext(); ) {
                Appender next = it.next();
                if (next.getClass().equals(where)) {
                    it.remove();
                    list.add(next);
                }
            }
            return list;
        } else {
            throw new UnsupportedOperationException(where == null ? "" : where.getClass().getName());
        }
    }

    public List<Appender> getAppenders() {
        return Collections.unmodifiableList(this.appenders);
    }

    public LogLevel getLevel(Class<?> type) {
        return this.levelManager.get(type.getName());
    }

    public void addLog(Log log) {
        this.alives.add(log);
    }

}
