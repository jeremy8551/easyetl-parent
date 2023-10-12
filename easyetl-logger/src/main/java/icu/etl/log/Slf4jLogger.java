package icu.etl.log;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用 Slf4j 输出日志信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-13
 */
public class Slf4jLogger implements Log {

    private List<CharSequence> list;

    private Logger log;

    private LogFactory factory;

    public Slf4jLogger(LogFactory factory, Class<?> cls) {
        this.list = new ArrayList<CharSequence>();
        this.factory = factory;
        this.log = LoggerFactory.getLogger(cls);
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isErrorEnabled();
    }

    public void trace(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.trace(this.list.get(i).toString());
            }
        }
    }

    public void trace(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.trace(message, e);
    }

    public void debug(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.debug(this.list.get(i).toString());
            }
        }
    }

    public void debug(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.debug(message, e);
    }

    public void info(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.info(this.list.get(i).toString());
            }
        }
    }

    public void info(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.info(message, e);
    }

    public void warn(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.warn(this.list.get(i).toString());
            }
        }
    }

    public void warn(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.warn(message, e);
    }

    public void error(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.error(this.list.get(i).toString());
            }
        }
    }

    public void error(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.error(message, e);
    }

    public void fatal(String message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.error(this.list.get(i).toString());
            }
        }
    }

    public void fatal(String message, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        log.error(message, e);
    }

    public void write(CharSequence message) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (this.list) {
            this.list.clear();
            StringUtils.splitLines(message, this.list);
            for (int i = 0, size = this.list.size(); i < size; i++) {
                log.info(this.list.get(i).toString());
            }
        }
    }

}
