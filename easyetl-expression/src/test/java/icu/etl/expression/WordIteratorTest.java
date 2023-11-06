package icu.etl.expression;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WordIteratorTest {

    @Test
    public void test() throws IOException {
        WordIterator in = new WordIterator(new StandardAnalysis(), "this string conent is 'test is good' ");
        if (in.isNext("this")) {
            in.assertNext("this");
        }

        assertTrue(in.hasNext());
        assertEquals("string", in.next());
        in.mark();

        assertTrue(in.hasNext());
        assertEquals("conent", in.next());

        assertTrue(in.hasNext());
        in.assertNext(new String[]{"is", ""});

        assertTrue(in.hasNext());
        assertEquals("'test is good'", in.next());

        in.reset();

        assertTrue(in.hasNext());
        assertEquals("conent", in.next());

        assertTrue(in.hasNext());
        assertEquals("is", in.next());

        assertTrue(in.hasNext());
        assertEquals("'test is good'", in.next());
    }

    @Test
    public void test2() throws IOException {
        WordIterator in = new WordIterator(new StandardAnalysis(), "this string conent is 'test is good' ");
        if (in.isNext("this")) {
            in.assertNext("this");
        }
        assertTrue(in.hasNext());
        in.assertNext("string");
        assertEquals(" conent is 'test is good'", in.previewOther());
        assertEquals("conent", in.previewNext());
        in.assertLast("'test is good'");
        assertTrue(in.hasNext());
        assertEquals("conent is", in.readOther());
        in.assertOver();
    }

    @Test
    public void test1() throws IOException {
        WordIterator in = new WordIterator(new StandardAnalysis(), "this string conent is 'test is good' ");

        assertTrue(in.hasNext());
        assertEquals(in.next(), "this");

        assertEquals(in.previewOther(), " string conent is 'test is good'");
        assertEquals(in.last(), "'test is good'");

        assertEquals(in.previewOther(), " string conent is");
        assertEquals(in.last(), "is");
        assertEquals(in.last(), "conent");
        assertEquals(in.previewOther(), " string");
    }

    @Test
    public void testStartMatchBooleanStringStringArray() {
//		assertTrue(new WordReader(new ScriptAnalysis(), "ab cd ef g").startsWith(0, true, "ab", "cd"));
//		assertTrue(new WordReader(new ScriptAnalysis(), "ab cd ef g").startsWith(0, true, "ab"));
//		assertTrue(new WordReader(new ScriptAnalysis(), "Ab cd ef g").startsWith(0, true, "ab"));
    }

}
