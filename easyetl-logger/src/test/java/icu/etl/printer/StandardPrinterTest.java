package icu.etl.printer;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import icu.etl.util.FileUtils;
import icu.etl.util.TimeWatch;
import org.junit.Assert;
import org.junit.Test;

public class StandardPrinterTest {

    @Test
    public void test() {
        Writer writer = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                System.out.print(new String(cbuf, off, len));
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        };

        StandardPrinter out = new StandardPrinter();
        out.setWriter(writer);

        TimeWatch watch = new TimeWatch();
        int total = 101525401;
        Progress p = new Progress("任务1", out, "${taskId}共有 ${totalRecord} 条记录, 已加载 ${process} % ${leftTime} ..", total);
        for (int i = 1; i <= total; i++) {
            p.print();
        }

        System.out.println("遍历 " + total + " 用时: " + watch.useTime());
        Assert.assertEquals(total, p.getCount().longValue());
    }

    @Test
    public void test1() {
        CharArrayWriter writer = new CharArrayWriter();

        StandardPrinter p = new StandardPrinter(writer);
        p.print(true);
        p.print('1');
        p.print(new char[]{'2', '3', '4'});
        p.print("567");
        p.print(890.123);
        p.print((float) 456.789);
        p.print(0);
        p.print((long) 123456);
        p.print(new StringBuilder().append("7890"));
        Assert.assertEquals("true1234567890.123456.78901234567890", writer.toString());

        writer.reset();
        p.println();
        Assert.assertEquals(FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println(true);
        Assert.assertEquals("true" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println('0');
        Assert.assertEquals("0" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println(new char[]{'1', '2', '3'});
        Assert.assertEquals("123" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println("456");
        Assert.assertEquals("456" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println((double) 789.0123);
        Assert.assertEquals("789.0123" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println((float) 456.789);
        Assert.assertEquals("456.789" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println(0);
        Assert.assertEquals("0" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println((long) 123456);
        Assert.assertEquals("123456" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println(new StringBuilder().append("7890"));
        Assert.assertEquals("7890" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        p.println("test", new Exception("common exception"));

        writer.reset();
        p.println("task1", "task1");

        writer.reset();
        p.println("task2", "task2");

        writer.reset();
        p.println("task3", "task3");

        writer.reset();
        p.println("task4", "task4");

        p.close();
    }

    @Test
    public void test2() {
        StandardPrinter p = new StandardPrinter();
        p.println("test", new Exception("common exception"));
        p.println("task1", "task1");
        p.println("task2", "task2");
        p.println("task3", "task3");
        p.println("task4", "task4");
        p.close();
    }

}
