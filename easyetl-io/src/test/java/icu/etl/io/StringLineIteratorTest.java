package icu.etl.io;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringLineIteratorTest {

    @Test
    public void test() {
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
    public void test1() {
        BufferedLineReader in = new BufferedLineReader(" ");
        assertTrue(in.hasNext());
        assertTrue(" ".equals(in.next()));
        assertTrue(in.getLineNumber() == 1);
        assertEquals(in.getLineSeparator(), "");
    }

    @Test
    public void test2() {
        BufferedLineReader in = new BufferedLineReader("");

        assertTrue(!in.hasNext());
        assertTrue(in.next() == null);
        assertTrue(in.getLineNumber() == 0);
        assertEquals(in.getLineSeparator(), "");
    }
}
