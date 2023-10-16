package icu.etl.printer;

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
        this.log = LogFactory.getLog(StandardPrinter.class);
        this.initBuffer();
        this.mulityTask = new LinkedHashMap<String, CharSequence>();
    }

    /**
     * 初始化缓冲区
     */
    protected void initBuffer() {
        this.buffer = new StringBuilder(100);
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

    public void print(CharSequence msg) {
        synchronized (this.buffer) {
            this.buffer.setLength(0);
            this.buffer.append(msg);
            this.flush();
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

    public void println(String id, CharSequence msg) {
        synchronized (this.buffer) {
            this.buffer.setLength(0);
            this.mulityTask.put(id, msg); // 保存某个任务信息
            Set<String> keys = this.mulityTask.keySet();
            for (Iterator<String> it = keys.iterator(); it.hasNext(); ) { // 保存某个任务信息后，重新生成最新的任务信息
                String name = it.next();
                CharSequence cs = this.mulityTask.get(name);
                this.buffer.append(StringUtils.escapeLineSeparator(cs));
                this.buffer.append(FileUtils.lineSeparator);
            }
            this.flush();
        }
    }

    public void println() {
        synchronized (this.buffer) {
            this.buffer.setLength(0);
            this.buffer.append(FileUtils.lineSeparator);
            this.flush();
        }
    }

    public void println(CharSequence msg) {
        synchronized (this.buffer) {
            this.buffer.setLength(0);
            this.buffer.append(msg);
            this.buffer.append(FileUtils.lineSeparator);
            this.flush();
        }
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
        synchronized (this.buffer) {
            this.buffer.setLength(0);
            this.buffer.append(msg);
            this.buffer.append(FileUtils.lineSeparator);
            this.buffer.append(StringUtils.toString(e));
            this.buffer.append(FileUtils.lineSeparator);
            this.flushErr();
        }
    }

    protected void flushErr() {
        try {
            if (this.writer != null) {
                IO.write(this.writer, this.buffer);
                this.writer.flush();
            } else {
                this.log.write(this.buffer);
            }
        } catch (Exception e) {
            this.log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 提交缓存
     */
    protected void flush() {
        try {
            if (this.writer != null) {
                IO.write(this.writer, this.buffer);
                this.writer.flush();
            } else {
                this.log.write(this.buffer);
            }
        } catch (Exception e) {
            this.log.error(e.getLocalizedMessage(), e);
        }
    }

    public void close() {
        IO.flushQuietly(this.writer);
        IO.close(this.writer);
        this.mulityTask.clear();
        this.initBuffer();
    }

    /**
     * 返回信息输出接口的换行符
     *
     * @return
     */
    public String getLineSeperator() {
        return FileUtils.lineSeparator;
    }

    public void setFormatter(Format converter) {
        this.converter = converter;
    }

    public Format getFormatter() {
        return this.converter;
    }

    public String toString() {
        return StandardPrinter.class.getSimpleName() + "[log=" + log + ", mulityTask=" + StringUtils.toString(this.mulityTask) + ", writer=" + writer + "]";
    }

}
