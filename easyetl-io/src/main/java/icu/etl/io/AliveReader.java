package icu.etl.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * 不会执行 {@linkplain #close()} 方法
 *
 * @author jeremy8551@qq.com
 */
public class AliveReader extends Reader {

    private Reader proxy;

    public AliveReader(Reader in) {
        this.proxy = in;
    }

    public int read(CharBuffer target) throws IOException {
        return this.proxy.read(target);
    }

    public int read() throws IOException {
        return this.proxy.read();
    }

    public int read(char[] cbuf) throws IOException {
        return this.proxy.read(cbuf);
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return this.proxy.read(cbuf, off, len);
    }

    public long skip(long n) throws IOException {
        return this.proxy.skip(n);
    }

    public boolean ready() throws IOException {
        return this.proxy.ready();
    }

    public boolean markSupported() {
        return this.proxy.markSupported();
    }

    public void mark(int readAheadLimit) throws IOException {
        this.proxy.mark(readAheadLimit);
    }

    public void reset() throws IOException {
        this.proxy.reset();
    }

    public void close() throws IOException {
    }

    public int hashCode() {
        return this.proxy.hashCode();
    }

    public boolean equals(Object obj) {
        return this.proxy.equals(obj);
    }

    public String toString() {
        return this.proxy.toString();
    }

}
