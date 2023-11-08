package icu.etl.ioc;

import org.junit.Assert;
import org.junit.Test;

public class EasyScanPatternTest {

    @Test
    public void test() {
        EasyScanPattern pattern = new EasyScanPattern("a");
        Assert.assertTrue(!pattern.isBlank());

        pattern = new EasyScanPattern("org.test");
        Assert.assertFalse(pattern.isBlank());
        Assert.assertEquals("org.test", pattern.getPrefix());
        Assert.assertEquals("org.test", pattern.getRule());
        Assert.assertFalse(pattern.isExclude());
        Assert.assertEquals("org.test", pattern.parse("org.test.*?.ab.cde"));
    }

    @Test
    public void test1() {
        EasyScanPattern pattern = new EasyScanPattern("!");
        Assert.assertTrue(pattern.isBlank());

        pattern = new EasyScanPattern("^^^");
        Assert.assertTrue(pattern.isBlank());

        pattern = new EasyScanPattern("!org.test");
        Assert.assertFalse(pattern.isBlank());
        Assert.assertEquals("org.test", pattern.getPrefix());
        Assert.assertEquals("!org.test", pattern.getRule());
        Assert.assertTrue(pattern.isExclude());

        Assert.assertEquals("org.test", pattern.parse("org.test.*?.ab.cde"));
        Assert.assertEquals("org.test", pattern.parse("org.test.*?.*.?"));
        Assert.assertEquals("org.test", pattern.parse("org.test."));
        Assert.assertEquals("org.test", pattern.parse("org.test.."));
    }

    @Test
    public void test2() {
        Assert.assertTrue(new EasyScanPattern("!a.b").contains(new EasyScanPattern("!a.b.c")));
        Assert.assertTrue(new EasyScanPattern("!a").contains(new EasyScanPattern("!a.b.c")));
        Assert.assertFalse(new EasyScanPattern("a").contains(new EasyScanPattern("!a.b.c")));
        Assert.assertTrue(new EasyScanPattern("a").contains(new EasyScanPattern("a.b.c")));
        Assert.assertTrue(new EasyScanPattern("a").contains(new EasyScanPattern("a.b")));
        Assert.assertTrue(new EasyScanPattern("a").contains(new EasyScanPattern("a.b.c.d.e")));
    }
}
