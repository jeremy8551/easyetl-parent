package icu.etl.io;

import java.io.Writer;

public class NullWriter extends Writer {

    public NullWriter() {
        super();
    }

    public NullWriter(Object lock) {
        super(lock);
    }

    public void write(char[] cbuf, int off, int len) {
    }

    public void flush() {
    }

    public void close() {
    }

    public void write(int c) {
    }

    public void write(char[] cbuf) {
    }

    public void write(String str) {
    }

    public void write(String str, int off, int len) {
    }

    public Writer append(CharSequence csq) {
        return this;
    }

    public Writer append(CharSequence csq, int start, int end) {
        return this;
    }

    public Writer append(char c) {
        return this;
    }

    public String toString() {
        return "";
    }
}
