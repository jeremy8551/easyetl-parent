package icu.etl.io;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ArrayIteratorTest {

    @Test
    public void test() {
        String[] a = {"0", "1", "2", "3", "4", "5", null};
        ArrayIterator<String> it = new ArrayIterator<String>(a, 1, 4);
        if (it.hasNext()) {
            assertTrue(it.next().equals("1"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("2"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("3"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("4"));
        }
        assertTrue(!it.hasNext());
    }

    @Test
    public void test1() {
        String[] a = {"0", "1", "2", "3", "4", "5", null};
        ArrayIterator<String> it = new ArrayIterator<String>(a, 0, 7);
        if (it.hasNext()) {
            assertTrue(it.next().equals("0"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("1"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("2"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("3"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("4"));
        }
        if (it.hasNext()) {
            assertTrue(it.next().equals("5"));
        }
        if (it.hasNext()) {
            assertTrue(it.next() == null);
        }
        assertTrue(!it.hasNext());
    }
}
