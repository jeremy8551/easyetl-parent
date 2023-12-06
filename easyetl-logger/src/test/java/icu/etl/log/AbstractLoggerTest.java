package icu.etl.log;

import icu.etl.log.apd.DefaultLog;
import icu.etl.log.cxt.LogContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLoggerTest {

    @Test
    public void test0() {
        String fqcn = null;

        try {
            new DefaultLog(null, AbstractLoggerTest.class, LogLevel.OFF, fqcn, false);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            new DefaultLog(new LogContextImpl(), null, LogLevel.TRACE, fqcn, false);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            new DefaultLog(new LogContextImpl(), AbstractLoggerTest.class, null, fqcn, false);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void test1() {
        String fqcn = null;
        DefaultLog log = new DefaultLog(new LogContextImpl(), AbstractLoggerTest.class, LogLevel.OFF, fqcn, false);

        log.setLevel(LogLevel.TRACE);
        Assert.assertTrue(log.isTraceEnabled());

        log.setLevel(LogLevel.DEBUG);
        Assert.assertTrue(log.isDebugEnabled());

        log.setLevel(LogLevel.INFO);
        Assert.assertTrue(log.isInfoEnabled());

        log.setLevel(LogLevel.WARN);
        Assert.assertTrue(log.isWarnEnabled());

        log.setLevel(LogLevel.ERROR);
        Assert.assertTrue(log.isErrorEnabled());

        log.setLevel(LogLevel.FATAL);
        Assert.assertTrue(log.isFatalEnabled());

        try {
            log.setLevel(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }
}
