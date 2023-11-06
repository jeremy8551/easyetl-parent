package icu.etl.printer;

import java.io.CharArrayWriter;
import java.io.IOException;

import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class OutputStreamPrinterTest {

    @Test
    public void test() throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        StandardPrinter printer = new StandardPrinter(writer);
        OutputStreamPrinter out = new OutputStreamPrinter(printer, null);

        out.setCharsetName(StringUtils.CHARSET);
        Assert.assertEquals(StringUtils.CHARSET, out.getCharsetName());

        out.write("123\r\n4567\r89\n0测试".getBytes(StringUtils.CHARSET));
        out.flush();

        Assert.assertEquals("123" + FileUtils.lineSeparator + "4567" + FileUtils.lineSeparator + "89" + FileUtils.lineSeparator + "0测试" + FileUtils.lineSeparator, writer.toString());

        writer.reset();
        out.write((byte) '\n');
        out.close();
    }

    @Test
    public void test1() {
        try {
            new OutputStreamPrinter(null, null);
            Assert.fail();
        } catch (Exception e) {
        }
    }
}
