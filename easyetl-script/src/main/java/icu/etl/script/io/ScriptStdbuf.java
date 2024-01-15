package icu.etl.script.io;

import java.io.CharArrayReader;
import java.io.Reader;
import java.io.Writer;
import java.text.Format;

import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎信息输出接口的缓存实现
 *
 * @author jeremy8551@qq.com
 */
public class ScriptStdbuf implements UniversalScriptStdout {

    /** 代理的标准信息输出接口 */
    protected UniversalScriptStdout proxy;

    /** 缓冲 */
    protected StringBuilder buf;

    /**
     * 初始化
     *
     * @param stdout
     */
    public ScriptStdbuf(UniversalScriptStdout stdout) {
        this.proxy = stdout;
        this.close();
    }

    public Writer getWriter() {
        return this.proxy == null ? null : this.proxy.getWriter();
    }

    public void setWriter(Writer writer) {
        if (this.proxy != null) {
            this.proxy.setWriter(writer);
        }
    }

    public void print(CharSequence msg) {
        this.buf.append(msg);
    }

    public void print(Object obj) {
        this.buf.append(obj);
    }

    public void println(String id, CharSequence msg) {
        this.buf.append('[').append(id).append(']').append(msg).append(FileUtils.lineSeparator);
    }

    public void println() {
        this.buf.append(FileUtils.lineSeparator);
    }

    public void println(CharSequence msg) {
        this.buf.append(msg).append(FileUtils.lineSeparator);
    }

    public void println(Object object) {
        this.buf.append(object).append(FileUtils.lineSeparator);
    }

    public void println(CharSequence msg, Throwable e) {
        this.buf.append(msg).append(FileUtils.lineSeparator).append(StringUtils.toString(e)).append(FileUtils.lineSeparator);
    }

    public void close() {
        this.buf = new StringBuilder(512);
    }

    public void setFormatter(Format f) {
        if (this.proxy != null) {
            this.proxy.setFormatter(f);
        }
    }

    /**
     * 转为字符流
     *
     * @return
     */
    public Reader toReader() {
        char[] array = new char[this.buf.length()];
        this.buf.getChars(0, this.buf.length(), array, 0);
        return new CharArrayReader(array, 0, array.length);
    }

    public Format getFormatter() {
        return this.proxy == null ? null : this.proxy.getFormatter();
    }

    public void clear() {
        this.buf.setLength(0);
    }

    public String rtrimBlank() {
        return StringUtils.rtrimBlank(this.buf);
    }

    public String trimBlank() {
        return StringUtils.trimBlank(this.buf);
    }

    public String toString() {
        return this.buf.toString();
    }

}
