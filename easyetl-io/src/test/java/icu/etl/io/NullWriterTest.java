package icu.etl.io;

import org.junit.Test;

public class NullWriterTest {

    @Test
    public void write() {
        NullWriter out = new NullWriter();
        out.write(1);
    }

    @Test
    public void flush() {
        NullWriter out = new NullWriter();
        out.flush();
    }

    @Test
    public void close() {
        NullWriter out = new NullWriter();
        out.close();
    }

    @Test
    public void testWrite() {
        NullWriter out = new NullWriter();
        out.write("test");
    }

    @Test
    public void testWrite1() {
        NullWriter out = new NullWriter();
        out.write("1234567", 0, 7);
    }

    @Test
    public void testWrite2() {
        NullWriter out = new NullWriter();
        out.write("1234".toCharArray(), 0, 4);
    }

    @Test
    public void testWrite3() {
        NullWriter out = new NullWriter();
        out.write("1234".toCharArray());
    }

    @Test
    public void append() {
        NullWriter out = new NullWriter();
        out.append('a');
    }

    @Test
    public void testAppend() {
        NullWriter out = new NullWriter();
        out.append("1234");
    }

    @Test
    public void testAppend1() {
        NullWriter out = new NullWriter();
        out.append("1234", 0, 4);
    }
}