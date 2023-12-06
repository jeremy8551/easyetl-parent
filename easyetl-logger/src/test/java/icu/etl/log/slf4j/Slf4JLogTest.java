package icu.etl.log.slf4j;

import icu.etl.log.Log;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.log.apd.DefaultLogTest;
import icu.etl.log.cxt.LogContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class Slf4JLogTest {

    @Test
    public void test1() {
        Assert.assertTrue(Slf4jLogBuilder.support());
        LogContext context = new LogContextImpl();
        context.updateLevel("", LogLevel.TRACE);
        context.setBuilder(new Slf4jLogBuilder());

        Log log = LogFactory.getLog(context, DefaultLogTest.class, null, false);
        log.trace("print trace");
        log.trace("print\n trace");
        log.trace("print trace", new NullPointerException());

        log.debug("print debug");
        log.debug("print\n debug");
        log.debug("print debug", new NullPointerException());

        log.info("print info");
        log.info("print\n info");
        log.info("print info", new NullPointerException());

        log.warn("print warn");
        log.warn("print\n warn");
        log.warn("print warn", new NullPointerException());

        log.error("print error");
        log.error("print\n error");
        log.error("print error", new NullPointerException());

        log.fatal("print fatal");
        log.fatal("print\n fatal");
        log.fatal("print fatal", new NullPointerException());
    }

    @Test
    public void test2() {
        LogContext context = new LogContextImpl();
        context.updateLevel("", LogLevel.TRACE);
        context.setBuilder(new Slf4jLogBuilder());

        Log log = LogFactory.getLog(context, DefaultLogTest.class, null, false);
        Assert.assertFalse(log.isTraceEnabled());
        Assert.assertFalse(log.isDebugEnabled());
        Assert.assertTrue(log.isInfoEnabled());
        Assert.assertTrue(log.isWarnEnabled());
        Assert.assertTrue(log.isErrorEnabled());
        Assert.assertTrue(log.isFatalEnabled());

        Assert.assertEquals(DefaultLogTest.class.getName(), log.getName());
    }

}
