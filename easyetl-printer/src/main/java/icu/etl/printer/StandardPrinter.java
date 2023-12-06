package icu.etl.printer;

import java.io.IOException;
import java.io.Writer;
import java.text.Format;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 信息输出接口的默认实现类
 *
 * @author jeremy8551@qq.com
 */
public class StandardPrinter implements Printer, java.io.Closeable {

    /** 日志接口 */
    protected Log log;

    /** 多任务程序使用的缓存 */
    protected LinkedHashMap<String, CharSequence> mulityTask;

    /** 缓存，每次输出信息之前需要清空缓存 */
    protected StringBuilder buffer;

    /** 信息输出流 */
    protected Writer writer;

    /** 类型转换器（负责将 Object 对象转为字符串） */
    protected Format converter;

    /**
     * 初始化
     */
    public StandardPrinter() {
        this.log = LogFactory.getLog(LogFactory.getContext(), this.getClass(), StandardPrinter.class.getName(), false);
        this.buffer = new StringBuilder(100);
        this.mulityTask = new LinkedHashMap<String, CharSequence>();
    }

    /**
     * 初始化
     *
     * @param writer    信息输出接口, 为 null 时默认使用 {@linkplain Log} 接口输出信息
     * @param conterter 类型转换器(用于将 Object 对象转为字符串, 为 null 时默认使用 {@linkplain Object#toString()})
     */
    public StandardPrinter(Writer writer, Format conterter) {
        this();
        this.setWriter(writer);
        this.setFormatter(conterter);
    }

    /**
     * 初始化
     *
     * @param writer 信息输出接口, 为 null 时默认使用 {@linkplain Log} 接口输出信息
     */
    public StandardPrinter(Writer writer) {
        this();
        this.setWriter(writer);
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void print(CharSequence cs) {
        synchronized (this.buffer) {
            this.buffer.append(cs);
        }
    }

    public void print(char c) {
        this.print(String.valueOf(c));
    }

    public void print(int i) {
        this.print(String.valueOf(i));
    }

    public void print(float f) {
        this.print(String.valueOf(f));
    }

    public void print(double d) {
        this.print(String.valueOf(d));
    }

    public void print(boolean b) {
        this.print(b ? "true" : "false");
    }

    public void print(long l) {
        this.print(String.valueOf(l));
    }

    public void print(char[] a) {
        this.print(new String(a));
    }

    public void print(Object object) {
        this.print(this.converter == null ? String.valueOf(object) : this.converter.format(object));
    }

    public void println(CharSequence msg) {
        synchronized (this.buffer) {
            this.buffer.append(msg);

            if (this.writer != null) {
                try {
                    this.buffer.append(FileUtils.lineSeparator);
                    IO.write(this.writer, this.buffer);
                    this.buffer.setLength(0);
                    this.writer.flush();
                    return;
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                if (log.isInfoEnabled()) {
                    log.info(this.buffer.toString());
                }
                this.buffer.setLength(0);
                return;
            }
        }
    }

    public void println(String id, CharSequence msg) {
        synchronized (this.mulityTask) {
            this.mulityTask.put(id, msg); // 保存某个任务信息
        }

        Set<String> keys = this.mulityTask.keySet();
        StringBuilder buf = new StringBuilder(keys.size() * 30);
        for (Iterator<String> it = keys.iterator(); it.hasNext(); ) { // 保存某个任务信息后，重新生成最新的任务信息
            String taskId = it.next();
            buf.append(StringUtils.escapeLineSeparator(this.mulityTask.get(taskId)));
            if (it.hasNext()) {
                buf.append(FileUtils.lineSeparator);
            }
        }
        this.println(buf);
    }

    public void println() {
        this.println("");
    }

    public void println(char c) {
        this.println(String.valueOf(c));
    }

    public void println(int i) {
        this.println(String.valueOf(i));
    }

    public void println(float f) {
        this.println(String.valueOf(f));
    }

    public void println(double d) {
        this.println(String.valueOf(d));
    }

    public void println(boolean b) {
        this.println(b ? "true" : "false");
    }

    public void println(long l) {
        this.println(String.valueOf(l));
    }

    public void println(char[] array) {
        this.println(new String(array));
    }

    public void println(Object object) {
        this.println(this.converter == null ? String.valueOf(object) : this.converter.format(object));
    }

    public void println(CharSequence msg, Throwable e) {
        StringBuilder buf = new StringBuilder(msg.length() + 100);
        buf.append(msg);
        buf.append(FileUtils.lineSeparator);
        buf.append(StringUtils.toString(e));

        if (this.writer != null) {
            try {
                buf.append(FileUtils.lineSeparator);
                IO.write(this.writer, buf);
                this.writer.flush();
            } catch (IOException e1) {
                if (log.isErrorEnabled()) {
                    log.error(e1.getLocalizedMessage(), e1);
                }
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error(buf.toString());
            }
        }
    }

    public void flush() {
        if (this.buffer.length() > 0) {
            try {
                IO.write(this.writer, this.buffer);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
            this.buffer.setLength(0);
        }
        IO.flushQuietly(this.writer);
    }

    public void close() {
        this.flush();
        IO.close(this.writer);
        this.mulityTask.clear();
        this.buffer = new StringBuilder(100);
    }

    public void setFormatter(Format converter) {
        this.converter = converter;
    }

    public Format getFormatter() {
        return this.converter;
    }

    public String toString() {
        return StandardPrinter.class.getSimpleName() + "[mulityTask=" + StringUtils.toString(this.mulityTask) + ", writer=" + writer + "]";
    }

}
