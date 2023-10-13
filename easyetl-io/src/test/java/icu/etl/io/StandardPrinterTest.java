package icu.etl.io;

import icu.etl.printer.StandardPrinter;
import org.junit.Test;

public class StandardPrinterTest {

    @Test
    public void test1() {
        StandardPrinter p = new StandardPrinter();
        p.print(true);
        p.print('1');
        p.print(new char[]{'2', '3', '4'});
        p.print("567");
        p.print(890.123);
        p.print((float) 456.789);
        p.print(0);
        p.print((long) 123456);
        p.print(new StringBuilder().append("7890"));

        p.println();
        p.println(true);
        p.println('0');
        p.println(new char[]{'1', '2', '3'});
        p.println("456");
        p.println((double) 789.0123);
        p.println((float) 456.789);
        p.println(0);
        p.println((long) 123456);
        p.println(new StringBuilder().append("7890"));
        p.println("test", new Exception("common exception"));
        p.println("task1", "task1");
        p.println("task2", "task2");
        p.println("task3", "task3");
        p.println("task4", "task4");
    }

}
