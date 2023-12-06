package icu.etl.log;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.apd.file.FileAppender;
import icu.etl.log.cxt.LogContextImpl;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class LogTest {

    public static String charsetName = "utf-8";

    /**
     * 新建日志文件
     *
     * @param n 文件编号
     * @return 日志文件
     * @throws IOException 创建文件错误
     */
    public static File createfile(int n) throws IOException {
        File logfile = new File(System.getProperty("java.io.tmpdir"), "file" + n + ".log");
        if (logfile.exists()) {
            Assert.assertTrue(logfile.delete());
        }

        // 创造目录
        logfile.getParentFile().mkdirs();
        logfile.createNewFile();
        System.out.println(logfile.getAbsolutePath());
        return logfile;
    }

    @Test
    public void test2() throws IOException {
        File logfile = createfile(0);

        String pattern = "-------|%%|%d{yyyy-MM-dd HH:mm:ss.SSS}|%-5.5p|%-5.5level|%processId|%t|%l|%c|%C.%M(%F:%L)|mills=%r|%X{test}|%m%ex%n";
        LogContext context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());
        context.findAppender(ConsoleAppender.class).pattern(pattern);
        Assert.assertNotNull(new FileAppender(logfile.getAbsolutePath(), LogTest.charsetName, pattern).setup(context));

        String str = "\r";
        str += "1\n";
        str += "12\r\n";
        str += "\n";
        str += "1345\r\n";
        str += "\n";

        MDC.put("test", "testvalue");
        Log log = LogFactory.getLog(context, LogTest.class);
        log.info(str);

        String content = FileUtils.readline(logfile, charsetName, 0);
        List<CharSequence> list = new ArrayList<CharSequence>();
        StringUtils.splitLines(content, list);
        Assert.assertEquals(6, list.size());
        for (CharSequence cs : list) {
            String s = cs.toString();
            Assert.assertTrue(s.startsWith("-------|%|"));
        }
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        File logfile = createfile(1);

        String pattern = "-------|%%|%d{yyyy-MM-dd HH:mm:ss.SSS}|%-5.5p|%-5.5level|%processId|%t|%l|%c|%C.%M(%F:%L)|%r|%X{test}|%m%ex|%c{2}|%c{0}|%c{-2}%n";
        LogContext context = new LogContextImpl();
        context.updateLevel("", LogLevel.TRACE);
        context.setBuilder(new DefaultLogBuilder());
        context.findAppender(ConsoleAppender.class).pattern(pattern);
        Assert.assertNotNull(new FileAppender(logfile.getAbsolutePath(), LogTest.charsetName, pattern).setup(context));

        String mdcvalue = "testvalue";
        MDC.put("test", mdcvalue);
        Log log = LogFactory.getLog(context, LogTest.class);
        log.trace("test level is {}", "trace");
        log.debug("test level is {}", "debug");
        log.info("test level is {}", "info");
        log.error("test level is {}", "error");
        log.fatal("test level is {}", "fatal");
        MDC.clear();

        String content = FileUtils.readline(logfile, charsetName, 0);
        List<CharSequence> list = new ArrayList<CharSequence>();
        StringUtils.splitLines(content, list);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Assert.assertEquals(5, list.size());
        for (CharSequence cs : list) {
            String str = cs.toString();
            String[] array = str.split("\\|");

            Assert.assertEquals(16, array.length);
            Assert.assertEquals("-------", array[0]);
            Assert.assertEquals("%", array[1]);

            try {
                format.parse(array[2]);
            } catch (ParseException e) {
                Assert.fail();
            }

            LogLevel.of(array[3]);
            Assert.assertEquals(5, array[3].length());

            LogLevel.of(array[4]);
            Assert.assertEquals(5, array[4].length());

            Integer.parseInt(array[5]);

            Assert.assertTrue(array[6].length() > 0);

            Assert.assertEquals(array[9], array[7]);
            Assert.assertEquals(LogTest.class.getName(), array[8]);

            Integer.parseInt(array[10]);
            Assert.assertEquals(mdcvalue, array[11]);
            Assert.assertTrue(array[12].startsWith("test level is "));
            Assert.assertEquals("log." + LogTest.class.getSimpleName(), array[13]);
            Assert.assertEquals(LogTest.class.getName(), array[14]);
            Assert.assertEquals("icu.etl", array[15]);
        }
    }

    /**
     * 测试关闭输出功能
     */
    @Test
    public void test4() throws IOException {
        File logfile = createfile(2);

        LogContext context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());
        context.updateLevel("*", LogLevel.OFF);

        String pattern = "%d|%p|%level|%processId|%t|%l|%c|%C.%M(%F:%L)|mills=%r|%X{test}|%m%ex%n";
        Assert.assertNotNull(new FileAppender(logfile.getAbsolutePath(), LogTest.charsetName, pattern).setup(context));

        Log log = LogFactory.getLog(context, LogTest.class);
        log.trace("test level is {}", "trace");
        log.debug("test level is {}", "debug");
        log.info("test level is {}", "info");
        log.error("test level is {}", "error");
        log.fatal("test level is {}", "fatal");
        context.findAppender(FileAppender.class).close();
        context.findAppender(ConsoleAppender.class).close();
        Assert.assertEquals("", FileUtils.readline(logfile, charsetName, 0));
    }

    // 测试抛出异常
    @Test
    public void test5() throws IOException {
        File logfile = createfile(3);

        String pattern = "-------|%m%ex%n";
        System.setProperty(LogFactory.PROPERTY_LOG_SOUT, "");
        LogContextImpl context = new LogContextImpl();
        context.init();

        context.setBuilder(new DefaultLogBuilder());
        context.findAppender(ConsoleAppender.class).pattern(pattern);
        FileAppender fileAppender = new FileAppender(logfile.getAbsolutePath(), LogTest.charsetName, pattern).charsetName(charsetName);
        fileAppender.setup(context);

        // 删除记录器
        context.removeAppender(fileAppender.getName());
        Assert.assertNull(context.findAppender(FileAppender.class));

        // 安装记录器
        Assert.assertNotNull(fileAppender.setup(context));
        context.removeAppender(fileAppender);
        fileAppender.setup(context);

        // 测试记录器名
        Assert.assertNotNull(fileAppender.getName());
        Assert.assertEquals(fileAppender.getName(), context.findAppender(FileAppender.class).getName());

        String str = "\r";
        str += "1\n";
        str += "12\r\n";
        str += "\n";
        str += "1345\r\n";
        str += "\n";

        LogFactory.getLog(context, LogTest.class).error(str, new NullPointerException());

        String content = FileUtils.readline(logfile, charsetName, 0);
        List<CharSequence> list = new ArrayList<CharSequence>();
        StringUtils.splitLines(content, list);
        Assert.assertTrue(list.size() >= 8);
        Assert.assertEquals("java.lang.NullPointerException", list.get(6).toString());
    }

    @Test
    public void test6() throws IOException {
        File logfile = createfile(4);

        String pattern = "-------|%m%ex%n";
        LogContext context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());
        context.findAppender(ConsoleAppender.class).pattern(pattern);
        Assert.assertNotNull(new FileAppender(logfile.getAbsolutePath(), LogTest.charsetName, pattern).setup(context));

        LogFactory.getLog(context, LogTest.class).error(null);
        String content = FileUtils.readline(logfile, charsetName, 0);
        Assert.assertEquals("-------|" + System.getProperty("line.separator"), content);
    }

}
