package icu.etl.io;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BufferedLineReaderTest {

    @Test
    public void test0() throws IOException {
        BufferedLineReader in = new BufferedLineReader(new StringReader("line1\rline2\r\nline3\nline4\rline5 "), 2, 10);
        String line;
        assertNotNull((line = in.readLine()));
        assertEquals(line, "line1");
        assertEquals(in.getLineSeparator(), "\r");

        assertNotNull((line = in.readLine()));
        assertEquals(line, "line2");
        assertEquals(in.getLineSeparator(), "\r\n");

        assertNotNull((line = in.readLine()));
        assertEquals(line, "line3");
        assertEquals(in.getLineSeparator(), "\n");

        assertNotNull((line = in.readLine()));
        assertEquals(line, "line4");
        assertEquals(in.getLineSeparator(), "\r");

        assertNotNull((line = in.readLine()));
        assertEquals(line, "line5 ");
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

    @Test
    public void test1() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str, 2, 0);

        String s = in.readLine();
        assertEquals(1, in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertEquals(2, in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(3, in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertEquals(4, in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertNull(s);
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

    @Test
    public void test2() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str, 1, 0);

        String s = in.readLine();
        assertEquals(1, in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertEquals(2, in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(3, in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertEquals(4, in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertNull(s);
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

    @Test
    public void test3() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str);

        String s = in.readLine();
        assertEquals(1, in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertEquals(2, in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(3, in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertEquals(4, in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertNull(s);
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

    @Test
    public void test4() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        BufferedLineReader in = new BufferedLineReader(str);

        assertTrue(in.hasNext());
        String s = in.next();
        assertEquals(1, in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");
        assertFalse(in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertEquals(2, in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");
        assertFalse(in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertEquals(3, in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");
        assertFalse(in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertEquals(4, in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");
        assertFalse(in.isClosed());

        assertTrue(in.hasNext());
        s = in.next();
        assertEquals(5, in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        assertFalse(in.hasNext());
        s = in.next();
        assertEquals(5, in.getLineNumber());
        assertNull(s);
        assertEquals(in.getLineSeparator(), "");

        assertTrue(in.isClosed());
        in.close();
    }

    @Test
    public void test5() throws IOException {
        StringBuilder str = new StringBuilder("1\r2\n3\r\n4\n\n");
        BufferedLineReader in = new BufferedLineReader(str);

        String s = in.readLine();
        assertEquals(1, in.getLineNumber());
        assertEquals(s, "1");
        assertEquals(in.getLineSeparator(), "\r");

        s = in.readLine();
        assertEquals(2, in.getLineNumber());
        assertEquals(s, "2");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(3, in.getLineNumber());
        assertEquals(s, "3");
        assertEquals(in.getLineSeparator(), "\r\n");

        s = in.readLine();
        assertEquals(4, in.getLineNumber());
        assertEquals(s, "4");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertEquals(s, "");
        assertEquals(in.getLineSeparator(), "\n");

        s = in.readLine();
        assertEquals(5, in.getLineNumber());
        assertNull(s);
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

        assertFalse(in.hasNext());
        in.close();
    }

    @Test
    public void test10() throws IOException {
        BufferedLineReader in = new BufferedLineReader("1\n2\n3");

        assertTrue(in.hasNext());
        assertEquals("1", in.next());
        assertEquals(1, in.getLineNumber());
        assertEquals("\n", in.getLineSeparator());

        assertTrue(in.hasNext());
        assertEquals("2", in.next());
        assertEquals(2, in.getLineNumber());
        assertEquals("\n", in.getLineSeparator());

        assertTrue(in.hasNext());
        assertEquals("3", in.next());
        assertEquals(3, in.getLineNumber());
        assertEquals(in.getLineSeparator(), "");

        assertFalse(in.hasNext());
        assertNull(in.next());
        assertEquals(3, in.getLineNumber());
        in.close();
    }

    @Test
    public void test11() throws IOException {
        BufferedLineReader in = new BufferedLineReader(" ");
        assertTrue(in.hasNext());
        assertEquals(" ", in.next());
        assertEquals(1, in.getLineNumber());
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

    @Test
    public void test12() throws IOException {
        BufferedLineReader in = new BufferedLineReader("");
        assertFalse(in.hasNext());
        assertNull(in.next());
        assertEquals(0, in.getLineNumber());
        assertEquals(in.getLineSeparator(), "");
        in.close();
    }

}
