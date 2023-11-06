package icu.etl.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import icu.etl.collection.CharBuffer;
import icu.etl.util.FileUtils;

/**
 * 带缓存的文件输出类
 *
 * @author jeremy8551@qq.com
 */
public class BufferedLineWriter implements java.io.Closeable, Flushable, LineSeparator, LineNumber {

    /** 文件字节输出流 */
    protected Writer out;

    /** 缓冲区 */
    protected CharBuffer buffer;

    /** 写入文件的字符集 */
    protected String charsetName;

    /** 缓冲行数 */
    protected int cacheRows;

    /** 计数器 */
    protected int count;

    /** 总计写入文件行数 */
    protected long total;

    /** 换行符 */
    protected String lineSeparator;

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @throws IOException 访问文件发生错误
     */
    public BufferedLineWriter(File file, String charsetName) throws IOException {
        this(file, charsetName, false, 20);
    }

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @param cache       缓冲行数
     * @throws IOException 访问文件发生错误
     */
    public BufferedLineWriter(File file, String charsetName, int cache) throws IOException {
        this(file, charsetName, false, cache);
    }

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @param append      true表示追加方式写入文件
     * @param cache       缓冲行数
     * @throws IOException 访问文件发生错误
     */
    public BufferedLineWriter(File file, String charsetName, boolean append, int cache) throws IOException {
        FileUtils.createFile(file);
        this.out = new OutputStreamWriter(new FileOutputStream(file, append), charsetName);
        this.buffer = new CharBuffer(8192, 512);
        this.cacheRows = cache <= 0 ? 20 : cache;
        this.count = 0;
        this.charsetName = charsetName;
        this.lineSeparator = FileUtils.lineSeparator;
    }

    /**
     * 初始化
     *
     * @param out   文件输出流
     * @param cache 缓冲行数
     * @throws IOException 访问文件发生错误
     */
    public BufferedLineWriter(Writer out, int cache) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        if (cache <= 0) {
            throw new IllegalArgumentException(String.valueOf(cache));
        }

        this.buffer = new CharBuffer(8192, 512);
        this.out = out;
        this.cacheRows = cache;
        this.count = 0;
        this.lineSeparator = FileUtils.lineSeparator;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * 返回字符集编码
     *
     * @return 字符集编码
     */
    public String getCharsetName() {
        return this.charsetName;
    }

    public long getLineNumber() {
        return this.total;
    }

    /**
     * 将字符串写入到缓存中
     *
     * @param str 字符串
     */
    public void write(String str) {
        if (str != null) {
            this.buffer.append(str);
        }
    }

    /**
     * 写入一行字符串
     *
     * @param line 字符串
     * @return 写入成功返回true 失败返回false
     * @throws IOException 访问文件发生错误
     */
    public boolean writeLine(String line) throws IOException {
        return this.writeLine(line, this.lineSeparator);
    }

    /**
     * 写入一行字符串
     *
     * @param line          字符串
     * @param lineSeperator 行间分隔符
     * @return 返回 true 表示已将缓存写入文件
     * @throws IOException 访问文件发生错误
     */
    public boolean writeLine(String line, String lineSeperator) throws IOException {
        this.buffer.append(line);
        this.buffer.append(lineSeperator);
        this.lineSeparator = lineSeperator;

        if (++this.count >= this.cacheRows) {
            this.flush();
            return true;
        } else {
            return false;
        }
    }

    public void flush() throws IOException {
        if (!this.buffer.isEmpty()) {
            this.out.write(this.buffer.value(), 0, this.buffer.length());
            this.out.flush();
            this.buffer.clear();
            this.total += this.count;
            this.count = 0;
        }
    }

    public void close() throws IOException {
        if (this.out != null) {
            this.flush();
            this.out.close();
            this.out = null;
        }
        this.buffer.restore();
        this.count = 0;
    }

}
