package icu.etl.io;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import icu.etl.cn.ChinaUtils;
import icu.etl.printer.Progress;
import icu.etl.printer.StandardPrinter;
import icu.etl.util.TimeWatch;
import org.junit.Test;

public class ProcessDisplayPrintTest {

    @Test
    public void test() {
        Writer w = new Writer() {
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
        out.setWriter(w);

        TimeWatch watch = new TimeWatch();
        int total = 101525401;
        Progress p = new Progress("任务1", out, "${taskId}共有 ${totalRecord} 条记录, 已加载 ${process} % ${leftTime} ..", total);
        for (int i = 1; i <= total; i++) {
            p.print();
        }
        System.out.println("遍历 " + total + ", " + ChinaUtils.toChineseNumber(new BigDecimal(total)).replace('元', '行') + " 用时: " + watch.useTime());

        p = new Progress(out, "共有 ${totalRecord} 条记录, 已加载 ${process} % ${leftTime} ..", 0);
        p.print();

//		PrintStream ps = new PrintStream((OutputStream) null);

    }
}
