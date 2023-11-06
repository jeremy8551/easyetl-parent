package icu.etl.io;

import java.io.IOException;
import java.io.Writer;

import icu.etl.util.StringUtils;

public class OutputStreamWriter extends java.io.OutputStream {

    /** 字符输出流 */
    private Writer out;

    /** 字符集编码 */
    private String charsetName;

    public OutputStreamWriter(Writer out, String charsetName) {
        super();

        if (out == null) {
            throw new NullPointerException();
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        this.out = out;
        this.charsetName = charsetName;
    }

    public void write(int b) throws IOException {
        this.out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        String str = new String(b, off, len, this.charsetName);
        this.out.write(str);
    }

    public void write(byte[] b) throws IOException {
        String str = new String(b, 0, b.length, this.charsetName);
        this.out.write(str);
    }

    public void close() throws IOException {
        this.out.close();
    }

    public void flush() throws IOException {
        this.out.flush();
    }

}
