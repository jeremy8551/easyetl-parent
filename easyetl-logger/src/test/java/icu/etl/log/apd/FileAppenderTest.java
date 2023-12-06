package icu.etl.log.apd;

import java.io.File;
import java.io.IOException;

import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogTest;
import icu.etl.log.apd.file.FileAppender;
import icu.etl.log.cxt.LogContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class FileAppenderTest {

    @Test
    public void test1() throws IOException {
        LogContext context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());

        String pattern = "%d|%p|%level|%processId|%t|%l|%c|%C.%M(%F:%L)|mills=%r|%X{test}|%m%ex%n";
        FileAppender appender = new FileAppender(null, LogTest.charsetName, pattern);
        Assert.assertNotNull(appender.setup(context));

        LogFactory.getLog(context, LogTest.class).trace("test level is {}", "trace");

        String file = appender.getFile();
        Assert.assertTrue(new File(file).exists());
        System.out.println(file);
    }

}
