package icu.etl.log.cxt;

import icu.etl.log.Log;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.slf4j.Slf4jLogBuilder;
import org.junit.Assert;
import org.junit.Test;

public class LogContextLoaderTest {

    @Test
    public void test1() {
        LogContext context = new LogContextImpl();
        Assert.assertTrue(context.load("sout:trace"));
        Assert.assertEquals(DefaultLogBuilder.class, context.getBuilder().getClass());

        Log log = LogFactory.getLog(context, LogContextLoaderTest.class, null, false);
        Assert.assertTrue(log.isTraceEnabled());
        Assert.assertTrue(log.isDebugEnabled());
        Assert.assertTrue(log.isInfoEnabled());

        ConsoleAppender appender = context.findAppender(ConsoleAppender.class);
        Assert.assertNotNull(appender);
        Assert.assertEquals("", appender.getPattern());
    }

    @Test
    public void test2() {
        System.setProperty(LogFactory.PROPERTY_LOG_SOUT, "");
        LogContext context = new LogContextImpl();
        Assert.assertTrue(context.load("error:sout+"));
        Assert.assertEquals(DefaultLogBuilder.class, context.getBuilder().getClass());

        Log log = LogFactory.getLog(context, LogContextLoaderTest.class, null, false);
        Assert.assertFalse(log.isTraceEnabled());
        Assert.assertFalse(log.isDebugEnabled());
        Assert.assertFalse(log.isInfoEnabled());
        Assert.assertFalse(log.isWarnEnabled());
        Assert.assertTrue(log.isErrorEnabled());

        ConsoleAppender appender = context.findAppender(ConsoleAppender.class);
        Assert.assertNotNull(appender);
        Assert.assertEquals(LogFactory.DEFAULT_LOG_PATTERN, appender.getPattern());
    }

    @Test
    public void test3() {
        LogContext context = new LogContextImpl();
        Assert.assertFalse(context.load("slf4j|info"));
    }

    @Test
    public void test4() {
        LogContext context = new LogContextImpl();
        Assert.assertTrue(context.load("slf4j:"));
        Assert.assertEquals(Slf4jLogBuilder.class, context.getBuilder().getClass());
    }

    @Test
    public void test5() {
        LogContext context = new LogContextImpl();
        try {
            Assert.assertTrue(context.load("sout:info:test"));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

}
