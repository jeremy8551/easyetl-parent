package icu.etl.log;

import org.junit.Assert;
import org.junit.Test;

public class LogLevelTest {

    @Test
    public void test1() {
        LogLevel.of("trace");
        LogLevel.of("debug");
        LogLevel.of("info");
        LogLevel.of("warn");
        LogLevel.of("error");
        LogLevel.of("fatal");
        LogLevel.of("off");

        try {
            LogLevel.of("");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void test2() {
        Assert.assertTrue(LogLevel.is("trace"));
        Assert.assertTrue(LogLevel.is("debug"));
        Assert.assertTrue(LogLevel.is("info"));
        Assert.assertTrue(LogLevel.is("warn"));
        Assert.assertTrue(LogLevel.is("error"));
        Assert.assertTrue(LogLevel.is("fatal"));
        Assert.assertTrue(LogLevel.is("off"));
        Assert.assertFalse(LogLevel.is(""));
        Assert.assertFalse(LogLevel.is("1234"));
        Assert.assertFalse(LogLevel.is(null));
    }
}
