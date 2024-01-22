package icu.etl.io;

import java.io.IOException;
import java.io.Writer;

import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

public class OutputStreamWriter extends java.io.OutputStream {

    /** 字符输出流 */
    private Writer out;

    /** 字符集编码 */
    private String charsetName;

    public OutputStreamWriter(Writer out, String charsetName) {
        super();
        this.out = Ensure.notNull(out);
        this.charsetName = StringUtils.charset(charsetName);
    }

    public void write(int b) throws IOException {
        this.out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        String str = new String(b, off, len, this.charsetName);
        this.out.write(str);
    }

    public void write(byte[] b) throws IOException {
        String str = new String(b, this.charsetName);
        this.out.write(str);
    }

    public void close() throws IOException {
        this.out.close();
    }

    public void flush() throws IOException {
        this.out.flush();
    }

}
