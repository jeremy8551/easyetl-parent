package icu.etl.log.cxt;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.apd.field.CategoryField;
import icu.etl.log.apd.file.FileAppender;
import org.junit.Assert;
import org.junit.Test;

public class LogContextImplTest {

    @Test
    public void test1() {
        LogContextImpl context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());
        context.updateLevel("", LogLevel.TRACE);

        Assert.assertEquals(LogLevel.TRACE, context.getLevel(LogContextImplTest.class));

        Log log = LogFactory.getLog(context, LogContextImplTest.class, null, false);
        Log log1 = LogFactory.getLog(context, ConsoleAppender.class, null, false);
        Log log2 = LogFactory.getLog(context, CategoryField.class, null, false);
        Log log3 = LogFactory.getLog(context, FileAppender.class, null, false);

        Assert.assertTrue(log1.isTraceEnabled());
        Assert.assertTrue(log2.isTraceEnabled());
        Assert.assertTrue(log3.isTraceEnabled());

        Assert.assertTrue(log.isTraceEnabled());
        Assert.assertTrue(log.isDebugEnabled());
        Assert.assertTrue(log.isInfoEnabled());

        // all log is info
        context.updateLevel("", LogLevel.INFO);
        Assert.assertFalse(log.isTraceEnabled());
        Assert.assertFalse(log.isDebugEnabled());
        Assert.assertTrue(log.isInfoEnabled());

        // error
        context.updateLevel(FileAppender.class.getPackage().getName(), LogLevel.ERROR);
        Assert.assertFalse(log3.isInfoEnabled());
        Assert.assertTrue(log3.isErrorEnabled());

        // fatal
        context.updateLevel(CategoryField.class.getPackage().getName(), LogLevel.FATAL);
        Assert.assertFalse(log2.isInfoEnabled());
        Assert.assertFalse(log2.isErrorEnabled());
        Assert.assertTrue(log2.isFatalEnabled());

        // info
        Assert.assertFalse(log1.isTraceEnabled());
        Assert.assertFalse(log1.isDebugEnabled());
        Assert.assertTrue(log1.isInfoEnabled());

        // info
        Assert.assertFalse(log.isTraceEnabled());
        Assert.assertFalse(log.isDebugEnabled());
        Assert.assertTrue(log.isInfoEnabled());
    }

    @Test
    public void test2() throws NoSuchFieldException {
        LogContextImpl context = new LogContextImpl();
        try {
            context.setBuilder(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        context.setBuilder(new DefaultLogBuilder());

        try {
            context.addAppender(null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            context.removeAppender(new Integer(0));
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(true);
        }

        Log log = LogFactory.getLog(context, String.class, null, false);
        log.info(null);
    }

}
