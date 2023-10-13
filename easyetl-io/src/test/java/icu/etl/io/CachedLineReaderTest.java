package icu.etl.io;

import java.io.CharArrayReader;
import java.io.IOException;

import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CachedLineReaderTest {

    @Test
    public void test() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);

        assertTrue(r.cacheLine(1) == 1);
        assertEquals("", StringUtils.escapeLineSeparator(r.getLineSeparator()));
        assertTrue(r.getLineNumber() == 0);

        try {
            r.cacheLine(1);
            assertTrue("1".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue("\r".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue(r.getLineNumber() == 1);

            assertTrue("2".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 2);
            assertTrue("\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("3".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 3);
            assertTrue("\r\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("4".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 4);
            assertTrue("\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 5);
            assertTrue("\n".equals(r.getLineSeparator()));

            assertTrue(r.cacheLine(1) == 0);
            assertTrue(r.readLine() == null && "".equals(r.getLineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            r.close();
        }
    }

    @Test
    public void test1() {
        String str = "\r2\n3\r\n4\n\n";
//		System.out.print(str);
//		System.out.println("======");
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        try {
            assertTrue(r.cacheLine(1) == 1);
            assertTrue(r.getLineNumber() == 0);

            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue(r.cacheLine(1) == 1);
            System.out.println("this111");
//			System.out.println(StringUtils.escapeLineSeparator(r.getLineSeparator()));
            assertEquals(StringUtils.escapeLineSeparator("\r"), StringUtils.escapeLineSeparator(r.getLineSeparator()));

            assertTrue("2".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 2);
            r.cacheLine(1);
            assertTrue("\n".equals(r.getLineSeparator()));

            assertTrue("3".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 3);
            r.cacheLine(1);
            assertTrue("\r\n".equals(r.getLineSeparator()));

            assertTrue("4".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 4);
            r.cacheLine(1);
            assertTrue("\n".equals(r.getLineSeparator()));

            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 5);
            assertEquals("\\n", StringUtils.escapeLineSeparator(r.getLineSeparator()));
            r.cacheLine(1);

            assertTrue(r.readLine() == null);
            assertTrue("".equals(r.getLineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(r);
        }
    }

    @Test
    public void test2() throws IOException {
        String str = "";
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        r.cacheLine(1);
        assertTrue(r.getLineSeparator().equals(""));
        try {
            assertTrue(r.getLineNumber() == 0);
            assertTrue(r.readLine() == null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            r.close();
        }
    }

    @Test
    public void test3() throws IOException {
        String str = "1";
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);

        r.cacheLine(1);
        assertTrue(r.getLineSeparator().equals(""));

        try {
            assertTrue(r.getLineNumber() == 0);
            assertTrue("1".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue("".equals(r.getLineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            r.close();
        }
    }

    @Test
    public void test4() {
        String str = "\r\n\r\n";
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        try {
            r.cacheLine(1);
            assertTrue(r.getLineNumber() == 0);

            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue("\r\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 2);
            assertTrue("\r\n".equals(r.getLineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(r);
        }
    }

    @Test
    public void test5() {
        String str = "1234567890\r2234567890\n3234567890\r\n4";
        CacheLineReader r = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        try {
            assertTrue(r.getLineNumber() == 0);

            assertTrue("1234567890".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue("\r".equals(r.getLineSeparator()));

            assertTrue("2234567890".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 2);
            assertTrue("\n".equals(r.getLineSeparator()));

            assertTrue("3234567890".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 3);
            assertTrue("\r\n".equals(r.getLineSeparator()));

            assertTrue("4".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 4);
            assertTrue("".equals(r.getLineSeparator()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(r);
        }
    }

    @Test
    public void test6() throws IOException {
        String str = "1\r2\n3\r\n4\n\n";

//		System.out.print(str);
//		System.out.println("======");
        CacheLineReader r = new CacheLineReader(str);

        r.cacheLine(1);
        assertTrue(r.getLineSeparator().equals(""));

        try {

            assertTrue(r.getLineNumber() == 0);

//			System.out.println(r.readline());
//			System.out.println(r.getNumber());
            r.cacheLine(1);
            assertTrue("1".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 1);
            assertTrue("\r".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("2".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 2);
            assertTrue("\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("3".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 3);
            assertEquals("\r\n", r.getLineSeparator());

            r.cacheLine(1);
            assertTrue("4".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 4);
            assertTrue("\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue("".equals(r.readLine()));
            assertTrue(r.getLineNumber() == 5);
            assertTrue("\n".equals(r.getLineSeparator()));

            r.cacheLine(1);
            assertTrue(r.readLine() == null);
            assertTrue("".equals(r.getLineSeparator()));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            r.close();
        }
    }

    @Test
    public void test7() {
    }

    @Test
    public void test9() {
        String str = "1\r2\n3\r\n4\n\n";
        CacheLineReader in = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        try {
            assertTrue("1".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 1);
            assertTrue("\r".equals(in.getLineSeparator()));

            assertTrue(in.getLineNumber() == 1);

            assertTrue("2".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 2);
            assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue("3".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 3);
            assertTrue("\r\n".equals(in.getLineSeparator()));

            assertTrue("4".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 4);
            assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue("".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 5);
            assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue(in.readLine() == null);
            assertTrue(in.getLineNumber() == 5);
            assertTrue("".equals(in.getLineSeparator()));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(in);
        }
    }

    @Test
    public void test8() {
        String str = "1\r2\n3\r\n4\n\n";
        CacheLineReader in = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
        try {
            assertTrue("1".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 1);
            assertTrue("\r".equals(in.getLineSeparator()));

            assertTrue(in.getLineNumber() == 1);

            assertTrue("2".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 2);
            assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue("3".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 3);
            assertTrue("\r\n".equals(in.getLineSeparator()));

            assertTrue("4".equals(in.readLine()));
            assertTrue(in.getLineNumber() == 4);
            assertTrue("\n".equals(in.getLineSeparator()));

//			in.cacheLine("test");
//			assertTrue("test".equals(in.readLine()));
//			assertEquals(in.getLineNumber(), 5);
//			assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue("".equals(in.readLine()));
            assertEquals(in.getLineNumber(), 5);
            assertTrue("\n".equals(in.getLineSeparator()));

            assertTrue(in.readLine() == null);
            assertEquals(in.getLineNumber(), 5);
            assertTrue("".equals(in.getLineSeparator()));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(in);
        }
    }

//	@Test
//	public void test10() {
//		String str = "";
//		CacheLineReader in = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
//		try {
//			in.cacheLine("test");
//			assertTrue("test".equals(in.readLine()));
//			assertEquals(in.getLineNumber(), 1);
//			assertTrue(Files.lineSeparator.equals(in.getLineSeparator()));
//			
//			assertTrue(in.readLine() == null);
//			assertEquals(in.getLineNumber(), 1);
//			assertTrue("".equals(in.getLineSeparator()));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertTrue(false);
//		} finally {
//			in.close();
//		}
//	}
//	
//	@Test
//	public void test11() {
//		String str = "1";
//		CacheLineReader in = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
//		try {
//			in.cacheLine("test");
//			assertTrue("test".equals(in.readLine()));
//			assertEquals(in.getLineNumber(), 1);
//			assertTrue(Files.lineSeparator.equals(in.getLineSeparator()));
//			
//			assertEquals(in.readLine(), "1");
//			assertEquals(in.getLineNumber(), 2);
//			assertTrue("".equals(in.getLineSeparator()));
//			
//			assertTrue(in.readLine() == null);
//			assertEquals(in.getLineNumber(), 2);
//			assertTrue("".equals(in.getLineSeparator()));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertTrue(false);
//		} finally {
//			in.close();
//		}
//	}
//	
//	@Test
//	public void test12() {
//		String str = "1";
//		CacheLineReader in = new CacheLineReader(new CharArrayReader(str.toCharArray()), 1);
//		try {
//			assertEquals(in.readLine(), "1");
//			assertEquals(in.getLineNumber(), 1);
//			assertTrue("".equals(in.getLineSeparator()));
//			
//			in.cacheLine("test");
//			assertTrue("test".equals(in.readLine()));
//			assertEquals(in.getLineNumber(), 2);
//			assertTrue(Files.lineSeparator.equals(in.getLineSeparator()));
//			
//			assertTrue(in.readLine() == null);
//			assertEquals(in.getLineNumber(), 2);
//			assertTrue("".equals(in.getLineSeparator()));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			assertTrue(false);
//		} finally {
//			in.close();
//		}
//	}

}
