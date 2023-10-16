package icu.etl.printer;

import java.io.IOException;
import java.io.OutputStream;

import icu.etl.collection.ByteBuffer;
import icu.etl.util.Charset;
import icu.etl.util.StringUtils;

/**
 * {@linkplain OutputStream} 与 {@linkplain Printer} 的适配器
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-05-03
 */
public class OutputStreamPrinter extends OutputStream implements Charset {

    /** 输出接口 */
    protected Printer out;

    /** 缓存 */
    protected ByteBuffer buffer;

    /**
     * 初始化
     *
     * @param out         信息输出接口
     * @param charsetName 输出字符串的字符集编码，为空时默认取默认值
     */
    public OutputStreamPrinter(Printer out, String charsetName) {
        super();
        if (out == null) {
            throw new NullPointerException();
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        this.out = out;
        this.buffer = new ByteBuffer(512, 50, charsetName);
    }

    public void write(int b) throws IOException {
        byte c = (byte) b;
        this.buffer.append(c);

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
                    this.buffer.append(b);
                    break;
            }
        }
    }

    public void write(byte[] b) {
        this.write(b, 0, b.length);
    }

    public void flush() {
        if (this.buffer.length() > 0) {
            this.out.println(this.buffer.toString());
            this.buffer.clear();
        }
    }

    public void close() {
        this.flush();
        this.buffer.restore(10);
    }

    public String getCharsetName() {
        return this.buffer.getCharsetName();
    }

    public void setCharsetName(String charsetName) {
        this.buffer.setCharsetName(charsetName);
    }

}