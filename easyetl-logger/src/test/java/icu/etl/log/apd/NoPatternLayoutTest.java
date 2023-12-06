package icu.etl.log.apd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import icu.etl.log.Appender;
import icu.etl.log.ExecutorImpl;
import icu.etl.log.Log;
import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogTest;
import icu.etl.log.apd.file.FileAppender;
import icu.etl.log.cxt.LogContextImpl;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class NoPatternLayoutTest {

    @Test
    public void test() throws IOException {
        File file = LogTest.createfile(10);

        LogContext context = new LogContextImpl();
        Assert.assertTrue(context.load("info:sout"));

        ConsoleAppender appender = context.findAppender(ConsoleAppender.class);
        Assert.assertNotNull(appender);

        String pattern = appender.getPattern();
        new FileAppender(file.getAbsolutePath(), LogTest.charsetName, pattern).setup(context);

        Log log = LogFactory.getLog(context, NoPatternLayoutTest.class);
        log.info("test", new NullPointerException());

        String content = FileUtils.readline(file, LogTest.charsetName, 0);
        ArrayList<CharSequence> list = new ArrayList<CharSequence>();
        StringUtils.splitLines(content, list);
        Assert.assertTrue(content, list.size() >= 2);
        Assert.assertEquals("test", list.get(0));
        Assert.assertEquals("java.lang.NullPointerException", list.get(1).toString().trim());
    }

    @Test
    public void test1() throws IOException {
        File file = LogTest.createfile(40);

        LogContext context = new LogContextImpl();
        Assert.assertTrue(context.load("info:sout"));

        ConsoleAppender appender = context.findAppender(ConsoleAppender.class);
        Assert.assertNotNull(appender);

        String pattern = appender.getPattern();
        Appender appender1 = new FileAppender(new ExecutorImpl(), file.getAbsolutePath(), LogTest.charsetName, pattern, 5000).setup(context);

        Log log = LogFactory.getLog(context, NoPatternLayoutTest.class);
        log.info("test", new NullPointerException());
        appender1.close();

        String content = FileUtils.readline(file, LogTest.charsetName, 0);
        ArrayList<CharSequence> list = new ArrayList<CharSequence>();
        StringUtils.splitLines(content, list);
        Assert.assertTrue(content, list.size() >= 2);
        Assert.assertEquals("test", list.get(0));
        Assert.assertEquals("java.lang.NullPointerException", list.get(1).toString().trim());
    }

}
