package icu.etl.log;

import icu.etl.log.cxt.LogContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class LogFactoryTest {

    @Test
    public void test() {
        try {
            LogFactory.getLog(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void test1() {
        try {
            LogContextImpl context = new LogContextImpl();
            context.setBuilder(new ExpBuilder());
            LogFactory.getLog(context, LogFactoryTest.class, null, false);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    public static class ExpBuilder implements LogBuilder {

        public Log create(LogContext context, Class<?> type, String fqcn, boolean dynamicCategory) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
