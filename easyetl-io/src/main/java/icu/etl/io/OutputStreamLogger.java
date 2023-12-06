package icu.etl.io;

import java.io.IOException;
import java.io.OutputStream;

import icu.etl.collection.ByteBuffer;
import icu.etl.log.Log;
import icu.etl.util.CharsetName;
import icu.etl.util.StringUtils;

/**
 * {@linkplain OutputStream} 与 {@linkplain Log} 的适配器
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-05-03
 */
public class OutputStreamLogger extends OutputStream implements CharsetName {

    /** 输出接口 */
    protected Log log;

    /** 缓存 */
    protected ByteBuffer buf;

    /**
     * 初始化
     *
     * @param log         日志
     * @param charsetName 输出字符串的字符集编码
     */
    public OutputStreamLogger(Log log, String charsetName) {
        super();
        if (log == null) {
            throw new NullPointerException();
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        this.log = log;
        this.buf = new ByteBuffer(512, 50, charsetName);
    }

    public void write(int b) throws IOException {
        byte c = (byte) b;
        this.buf.append(c);

        if (c == '\r' || c == '\n') {
            this.flush();
        }
    }

    public void write(byte[] array, int off, int len) {
        for (int index = off, length = off + len; index < length; index++) {
            byte b = array[index];
            switch (b) {
                case '\n':
                    this.flush();
                    break;

                case '\r':
                    this.flush();
                    int next = index + 1;
                    if (next < length && array[next] == '\n') {
                        index = next;
                    }
                    break;

                default:
                    this.buf.append(b);
                    break;
            }
        }
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void flush() {
        if (this.buf.length() > 0) {
            this.log.info(this.buf.toString());
            this.buf.clear();
        }
    }

    public void close() {
        this.flush();
        this.buf.restore(10);
    }

    public String getCharsetName() {
        return this.buf.getCharsetName();
    }

    public void setCharsetName(String charsetName) {
        this.buf.setCharsetName(charsetName);
    }

}