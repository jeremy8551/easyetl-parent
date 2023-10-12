package icu.etl.log;

import java.io.PrintStream;
import java.util.Date;

import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 默认的日志接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public class DefaultLogger implements Log {

    private boolean trace;
    private boolean debug;
    private boolean infos;
    private boolean warns;
    private boolean error;
    private boolean fatal;
    private String name;
    private String level = StringUtils.left("", 5, ' ');
    private StringBuilder buf = new StringBuilder(100);
    private boolean newline = true;
    private int levelFlag;
    protected LogFactory factory;
    private PrintStream out;
    private PrintStream err;

    /**
     * 初始化
     *
     * @param factory 日志工厂类
     * @param clazz   产生log事件的类信息
     * @param level   日志级别
     */
    public DefaultLogger(LogFactory factory, Class<?> clazz, String level) {
        this(factory, clazz, null, null, level);
    }

    /**
     * 初始化
     *
     * @param factory 日志工厂
     * @param clazz   产生log事件的类信息
     * @param out     标准信息输出接口
     * @param err     错误信息输出接口
     * @param level   日志级别
     */
    public DefaultLogger(LogFactory factory, Class<?> clazz, PrintStream out, PrintStream err, String level) {
        this.setFactory(factory);
        this.setName(clazz);
        this.setLevel(level);
        this.out = (out == null) ? System.out : out;
        this.err = (err == null) ? System.err : err;
    }

    /**
     * 设置日志归属的工厂
     *
     * @param factory 日志工厂
     */
    protected void setFactory(LogFactory factory) {
        this.factory = factory;
    }

    /**
     * 设置日志归属类信息
     *
     * @param clazz 产生log事件的类信息
     */
    public void setName(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        } else {
            this.name = clazz.getName();
        }
    }

    /**
     * 设置日志级别
     *
     * @param level 日志级别, 详见 {@linkplain Log#LEVEL}
     */
    public void setLevel(String level) {
        level = StringUtils.trimBlank(level);
        this.level = level.toUpperCase();
        if ("trace".equalsIgnoreCase(level)) {
            this.trace = true;
            this.debug = true;
            this.infos = true;
            this.warns = true;
            this.error = true;
            this.fatal = true;
        } else if (StringUtils.isBlank(level) || "debug".equalsIgnoreCase(level)) {
            this.trace = false;
            this.debug = true;
            this.infos = true;
            this.warns = true;
            this.error = true;
            this.fatal = true;
        } else if ("info".equalsIgnoreCase(level)) {
            this.trace = false;
            this.debug = false;
            this.infos = true;
            this.warns = true;
            this.error = true;
            this.fatal = true;
        } else if ("warn".equalsIgnoreCase(level)) {
            this.trace = false;
            this.debug = false;
            this.infos = false;
            this.warns = true;
            this.error = true;
            this.fatal = true;
        } else if ("error".equalsIgnoreCase(level)) {
            this.trace = false;
            this.debug = false;
            this.infos = false;
            this.warns = false;
            this.error = true;
            this.fatal = true;
        } else if ("fatal".equalsIgnoreCase(level)) {
            this.trace = false;
            this.debug = false;
            this.infos = false;
            this.warns = false;
            this.error = false;
            this.fatal = true;
        } else {
            throw new UnsupportedOperationException(level);
        }
    }

    public boolean isTraceEnabled() {
        return trace;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public boolean isInfoEnabled() {
        return infos;
    }

    public boolean isWarnEnabled() {
        return warns;
    }

    public boolean isErrorEnabled() {
        return error;
    }

    public boolean isFatalEnabled() {
        return fatal;
    }

    public void trace(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (trace) {
            this.write("TRACE", msg);
        }
    }

    public void trace(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (trace) {
            this.write("TRACE", msg, e);
        }
    }

    public void debug(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (debug) {
            this.write("DEBUG", msg);
        }
    }

    public void debug(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (debug) {
            this.write("DEBUG", msg, e);
        }
    }

    public void info(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (infos) {
            this.write("INFO ", msg);
        }
    }

    public void info(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (infos) {
            this.write("INFO ", msg, e);
        }
    }

    public void warn(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (warns) {
            this.write("WARN ", msg);
        }
    }

    public void warn(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (warns) {
            this.write("WARN ", msg, e);
        }
    }

    public void error(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (error) {
            this.write("ERROR", msg);
        }
    }

    public void error(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (error) {
            this.write("ERROR", msg, e);
        }
    }

    public void fatal(String msg) {
        if (this.factory.isDisable()) {
            return;
        }

        if (fatal) {
            this.write("FATAL", msg);
        }
    }

    public void fatal(String msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        if (fatal) {
            this.write("FATAL", msg, e);
        }
    }

    public void write(CharSequence msg) {
        if (this.factory.isDisable()) {
            return;
        }

        this.prints(msg);
    }

    public void write(String level, CharSequence msg) {
        synchronized (buf) {
            buf.setLength(0);
            if (newline) {
                buf.append(Dates.format12(new Date()));
                buf.append(' ');
                buf.append(this.level);
                buf.append(' ');
            }
            buf.append(msg);
            out.println(buf.toString());
            buf.setLength(0);
            newline = true;
        }
    }

    public void write(String level, CharSequence msg, Throwable e) {
        synchronized (buf) {
            buf.setLength(0);
            if (newline) {
                buf.append(Dates.format12(new Date()));
                buf.append(' ');
                buf.append(this.level);
                buf.append(' ');
            }
            buf.append(msg);
            if (e != null) {
                buf.append(FileUtils.lineSeparator);
                buf.append(StringUtils.toString(e));
            }
            err.println(buf.toString());
            buf.setLength(0);
            newline = true;
        }
    }

    public Log writeline(CharSequence msg) {
        if (this.factory.isDisable()) {
            return this;
        }

        synchronized (buf) {
            buf.setLength(0);
            buf.append(msg);
            out.println(buf.toString());
            buf.setLength(0);
            newline = true;
        }
        return this;
    }

    public void print(CharSequence msg) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (buf) {
            buf.setLength(0);
            if (newline) {
                buf.append(Dates.format12(new Date()));
                buf.append(' ');
                buf.append(this.level);
                buf.append(' ');
            }
            buf.append(msg);
            out.print(buf.toString());
            buf.setLength(0);
            newline = false;
        }
    }

    public void println(CharSequence msg) {
        if (this.factory.isDisable()) {
            return;
        }

        this.write(this.level, msg);
    }

    public void println(CharSequence msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        this.write(this.level, msg, e);
    }

    public void println(Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        synchronized (buf) {
            buf.setLength(0);
            if (newline) {
                buf.append(Dates.format12(new Date()));
                buf.append(' ');
                buf.append(this.level);
                buf.append(' ');
            }
            buf.append(e);
            err.println(buf.toString());
            buf.setLength(0);
            newline = true;
        }
    }

    /**
     * 打印字符串，如果字符串中包含多行信息，则按行拆分字符串分别输出
     *
     * @param cs 字符串
     */
    public void prints(CharSequence cs) {
        if (this.factory.isDisable()) {
            return;
        }

        int length = 0;
        if (cs == null || (length = cs.length()) == 0) {
            return;
        }

        int next = 0;
        for (int i = 0; i < length; i++) {
            char c = cs.charAt(i);
            switch (c) {
                case '\n':
                    this.println(cs.subSequence(next, i));
                    next = i + 1;
                    break;

                case '\r':
                    this.println(cs.subSequence(next, i));
                    next = i + 1;
                    if (next < length && cs.charAt(next) == '\n') {
                        i++;
                        next = i + 1;
                    }
                    break;
            }
        }

        if (next < length) {
            this.print(cs.subSequence(next, length));
        }
    }

}