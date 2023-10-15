package icu.etl.io;

import java.io.IOException;
import java.io.StringReader;

import icu.etl.util.IO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BufferedLineReaderTest {

    @Test
    public void test0() throws IOException {
        BufferedLineReader in = new BufferedLineReader(new StringReader("line1\rline2\r\nline3\nline4\rline5 "), 2, 10);
        String line = null;
        try {
            assertTrue((line = in.readLine()) != null);
            assertEquals(line, "line1");
            assertEquals(in.getLineSeparator(), "\r");

            assertTrue((line = in.readLine()) != null);
            assertEquals(line, "line2");
            assertEquals(in.getLineSeparator(), "\r\n");

            assertTrue((line = in.readLine()) != null);
            assertEquals(line, "line3");
            assertEquals(in.getLineSeparator(), "\n");

            assertTrue((line = in.readLine()) != null);
            assertEquals(line, "line4");
            assertEquals(in.getLineSeparator(), "\r");

            assertTrue((line = in.readLine()) != null);
            assertEquals(line, "line5 ");
            assertEquals(in.getLineSeparator(), "");
        } finally {
            IO.close(in);
        }
    }

    @Test
    public void test1() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str, 2, 0);

        String s = in.readLine();
        assertTrue(1 == in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertTrue(2 == in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(3 == in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertTrue(4 == in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, null);
        assertEquals(in.getLineSeparator(), "");

        in.close();
    }

    @Test
    public void test2() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str, 1, 0);

        String s = in.readLine();
        assertTrue(1 == in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertTrue(2 == in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(3 == in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertTrue(4 == in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, null);
        assertEquals(in.getLineSeparator(), "");

        in.close();
    }

    @Test
    public void test3() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str);

        String s = in.readLine();
        assertTrue(1 == in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertTrue(2 == in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(3 == in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertTrue(4 == in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, null);
        assertEquals(in.getLineSeparator(), "");

        in.close();
    }

    @Test
    public void test4() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str);

        assertTrue(in.hasNext());
        String s = in.next();
        assertTrue(1 == in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");
        assertTrue(!in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertTrue(2 == in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");
        assertTrue(!in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertTrue(3 == in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");
        assertTrue(!in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertTrue(4 == in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");
        assertTrue(!in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        assertTrue(!in.hasNext());
        s = in.next();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, null);
        assertEquals(in.getLineSeparator(), "");

        assertTrue(in.isClosed());
    }

    @Test
    public void test5() throws IOException {
        StringBuilder str = new StringBuilder("1\r2\n3\r\n4\n\n");
        BufferedLineReader in = new BufferedLineReader(str);

        String s = in.readLine();
        assertTrue(1 == in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertTrue(2 == in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(3 == in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertTrue(4 == in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertTrue(5 == in.getLineNumber());
        assertEquals(s, null);
        assertEquals(in.getLineSeparator(), "");

        in.close();
    }

    @Test
    public void test6() throws IOException {
        StringBuilder str = new StringBuilder("1");
        BufferedLineReader in = new BufferedLineReader(str);
        in.skip(str.length());
        assertEquals(in.getLineNumber(), 1);
        in.close();
    }

    @Test
    public void test7() throws IOException {
        StringBuilder str = new StringBuilder("1\r2\n3\r\n4");
        BufferedLineReader in = new BufferedLineReader(str);
        in.skip(str.length() + 1);
        assertEquals(in.getLineNumber(), 4);
        in.close();
    }

    @Test
    public void test8() throws IOException {
        StringBuilder str = new StringBuilder("1\r2\n3\r\n4");
        BufferedLineReader in = new BufferedLineReader(str);
        in.skip(str.length());
        assertEquals(in.getLineNumber(), 4);
        in.close();
    }

    @Test
    public void test9() throws IOException {
        StringBuilder str = new StringBuilder("1\r2\n3\r\n4");
        BufferedLineReader in = new BufferedLineReader(str);
        assertTrue(in.hasNext());
        assertEquals(in.next(), "1");
        assertEquals(in.getLineNumber(), 1);

        assertTrue(in.hasNext());
        assertEquals(in.next(), "2");
        assertEquals(in.getLineNumber(), 2);

        assertTrue(in.hasNext());
        assertEquals(in.next(), "3");
        assertEquals(in.getLineNumber(), 3);

        assertTrue(in.hasNext());
        assertEquals(in.next(), "4");
        assertEquals(in.getLineNumber(), 4);

        assertTrue(!in.hasNext());

        in.close();
    }

    @Test
    public void test10() {
        BufferedLineReader in = new BufferedLineReader("1\n2\n3");

        assertTrue(in.hasNext());
        assertTrue("1".equals(in.next()));
        assertTrue(in.getLineNumber() == 1);
        assertTrue("\n".equals(in.getLineSeparator()));

        assertTrue(in.hasNext());
        assertTrue("2".equals(in.next()));
        assertTrue(in.getLineNumber() == 2);
        assertTrue("\n".equals(in.getLineSeparator()));

        assertTrue(in.hasNext());
        assertTrue("3".equals(in.next()));
        assertTrue(in.getLineNumber() == 3);
        assertEquals(in.getLineSeparator(), "");

        assertTrue(!in.hasNext());
        assertTrue(in.next() == null);
        assertTrue(in.getLineNumber() == 3);
    }

    @Test
    public void test11() {
        BufferedLineReader in = new BufferedLineReader(" ");
        assertTrue(in.hasNext());
        assertTrue(" ".equals(in.next()));
        assertTrue(in.getLineNumber() == 1);
        assertEquals(in.getLineSeparator(), "");
    }

    @Test
    public void test12() {
        BufferedLineReader in = new BufferedLineReader("");

        assertTrue(!in.hasNext());
        assertTrue(in.next() == null);
        assertTrue(in.getLineNumber() == 0);
        assertEquals(in.getLineSeparator(), "");
    }

}
