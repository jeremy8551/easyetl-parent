package icu.etl.printer;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ProgressTest {

    @Test
    public void test0() throws IOException {
        StandardPrinter printer = new StandardPrinter();

        long count = 100;
        String str = "测试进度 ${process}%, ${leftTime}";
        Progress process = new Progress(printer, str, count);

        for (int i = 1; i <= count; i++) {
            if (i % 2 == 0) {
                process.print();
            } else {
                process.print(true);
            }
        }

        process.reset();

        Assert.assertEquals(str, process.getMessage());
        Assert.assertEquals(process.getPrinter(), printer);
        Assert.assertNotNull(process.toString());
    }

    @Test
    public void test1() throws IOException {
        StandardPrinter printer = new StandardPrinter();

        long count = 100;
        String str = "测试进度 ${process}%, ${leftTime}";
        Progress process = new Progress("taskid", printer, str, count);

        for (int i = 1; i <= count; i++) {
            if (i % 2 == 0) {
                process.print();
            } else {
                process.print(true);
            }
        }

        Assert.assertEquals("taskid", process.getTaskId());
        process.reset();
        process.setCount(99);
        process.print();
        Assert.assertEquals(100, process.getCount().longValue());
        Assert.assertEquals(str, process.getMessage());
        Assert.assertEquals(process.getPrinter(), printer);
        Assert.assertNotNull(process.toString());
    }

}
