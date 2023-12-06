package icu.etl.log.apd;

import org.junit.Assert;
import org.junit.Test;

public class LogFieldAlignTest {

    @Test
    public void test() {
        Assert.assertEquals("", new LogFieldAlign(0, 0, true).format(null));
        Assert.assertEquals("", new LogFieldAlign(0, 0, true).format("1234"));
        Assert.assertEquals("4", new LogFieldAlign(1, 1, true).format("1234"));
        Assert.assertEquals(" ", new LogFieldAlign(1, 1, true).format(""));
        Assert.assertEquals("234", new LogFieldAlign(1, 3, true).format("1234"));
        Assert.assertEquals("1234", new LogFieldAlign(1, 5, false).format("1234"));
        Assert.assertEquals("1234 ", new LogFieldAlign(5, -1, false).format("1234"));
        Assert.assertEquals("1234", new LogFieldAlign(3, -1, false).format("1234"));

        boolean eb = false;
        try {
            Assert.assertEquals("1234", new LogFieldAlign(3, 2, false).format("1234"));
        } catch (Exception e) {
            eb = true;
        }

        if (!eb) {
            Assert.fail();
        }
    }

    @Test
    public void test1() {
        Assert.assertEquals("", LogFieldAlign.parse("0").format(null));
        Assert.assertEquals("", LogFieldAlign.parse("-0.0").format("1234"));
        Assert.assertEquals("4", LogFieldAlign.parse("1.1").format("1234"));
        Assert.assertEquals(" ", LogFieldAlign.parse("1.1").format(""));
        Assert.assertEquals("234", LogFieldAlign.parse("-1.3").format("1234"));
        Assert.assertEquals("1234", LogFieldAlign.parse("-1.5").format("1234"));
        Assert.assertEquals("1234 ", LogFieldAlign.parse("-5").format("1234"));
        Assert.assertEquals("1234", LogFieldAlign.parse("-3").format("1234"));
        Assert.assertEquals("234", LogFieldAlign.parse("3.3").format("1234"));
        Assert.assertEquals("234", LogFieldAlign.parse("3.3").format("234"));
        Assert.assertEquals("  1", LogFieldAlign.parse("3.3").format("1"));
    }
}
