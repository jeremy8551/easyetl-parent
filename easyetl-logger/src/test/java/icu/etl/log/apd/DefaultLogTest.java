package icu.etl.log.apd;

import icu.etl.log.Log;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import org.junit.Test;

public class DefaultLogTest {

    @Test
    public void test1() {
        LogContext context = LogFactory.getContext();
        context.updateLevel("", LogLevel.TRACE);
        context.setBuilder(new DefaultLogBuilder());

        Log log = LogFactory.getLog(context, DefaultLogTest.class, null, false);
        log.trace("print trace");
        log.trace("print trace", new NullPointerException());

        log.debug("print debug");
        log.debug("print debug", new NullPointerException());

        log.info("print info");
        log.info("print info", new NullPointerException());

        log.warn("print warn");
        log.warn("print warn", new NullPointerException());

        log.error("print error");
        log.error("print error", new NullPointerException());

        log.fatal("print fatal");
        log.fatal("print fatal", new NullPointerException());
    }
}
