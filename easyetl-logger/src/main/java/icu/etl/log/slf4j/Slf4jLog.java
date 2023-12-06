package icu.etl.log.slf4j;

import java.util.ArrayList;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用 Slf4j 输出日志信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-13
 */
public class Slf4jLog implements Log {

    public static String FQCN = Slf4jLog.class.getName();

    /** 行的集合 */
    private List<CharSequence> list;

    /** 日志接口 */
    private final Logger log;

    public Slf4jLog(Class<?> type, String fqcn) {
        this.list = new ArrayList<CharSequence>();
        this.log = LoggerFactory.getLogger(type);

        // 如果对 Slf4j，log4j等日志进行封装，会导致日志输出代码行数、代码所在的类名、代码所在的方法，不准确，就需要用这个方法对日志中的 fqcn 字段进行调整
        ClassUtils.setField(this.log, "FQCN.*", String.class, fqcn == null ? Slf4jLog.FQCN : fqcn);
    }

    public String getName() {
        return this.log.getName();
    }

    public boolean isTraceEnabled() {
        return this.log.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return this.log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return this.log.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return this.log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return this.log.isErrorEnabled();
    }

    public void trace(String message, Object... args) {
        if (StringUtils.contains(message, '\r', '\n')) {
            synchronized (this.list) {
                StringUtils.splitLines(message, this.list);
                for (int i = 0, size = this.list.size(); i < size; i++) {
                    this.log.trace(this.list.get(i).toString(), args);
                }
            }
        } else {
            this.log.trace(message, args);
        }
    }

    public void trace(String message, Throwable e) {
        this.log.trace(message, e);
    }

    public void debug(String message, Object... args) {
        if (StringUtils.contains(message, '\r', '\n')) {
            synchronized (this.list) {
                StringUtils.splitLines(message, this.list);
                for (int i = 0, size = this.list.size(); i < size; i++) {
                    this.log.debug(this.list.get(i).toString(), args);
                }
            }
        } else {
            this.log.debug(message, args);
        }
    }

    public void debug(String message, Throwable e) {
        this.log.debug(message, e);
    }

    public void info(String message, Object... args) {
        if (StringUtils.contains(message, '\r', '\n')) {
            synchronized (this.list) {
                StringUtils.splitLines(message, this.list);
                for (int i = 0, size = this.list.size(); i < size; i++) {
                    this.log.info(this.list.get(i).toString(), args);
                }
            }
        } else {
            this.log.info(message, args);
        }
    }

    public void info(String message, Throwable e) {
        this.log.info(message, e);
    }

    public void warn(String message, Object... args) {
        if (StringUtils.contains(message, '\r', '\n')) {
            synchronized (this.list) {
                StringUtils.splitLines(message, this.list);
                for (int i = 0, size = this.list.size(); i < size; i++) {
                    this.log.warn(this.list.get(i).toString(), args);
                }
            }
        } else {
            this.log.warn(message, args);
        }
    }

    public void warn(String message, Throwable e) {
        this.log.warn(message, e);
    }

    public void error(String message, Object... args) {
        if (StringUtils.contains(message, '\r', '\n')) {
            synchronized (this.list) {
                StringUtils.splitLines(message, this.list);
                for (int i = 0, size = this.list.size(); i < size; i++) {
                    this.log.error(this.list.get(i).toString(), args);
                }
            }
        } else {
            this.log.error(message, args);
        }
    }

    public void error(String message, Throwable e) {
        this.log.error(message, e);
    }

    public void fatal(String message, Object... args) {
        this.error(message, args);
    }

    public void fatal(String message, Throwable e) {
        this.error(message, e);
    }

}
