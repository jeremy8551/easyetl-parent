package icu.etl.printer;

import java.io.File;
import java.io.IOException;

import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class StandardFilePrinterTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return
     */
    public File createfile() throws IOException {
        File dir = FileUtils.getTempDir(StandardFilePrinter.class);
        FileUtils.createDirectory(dir);
        File file = new File(dir, StandardFilePrinter.class.getSimpleName() + StringUtils.toRandomUUID() + ".log");
        FileUtils.createFile(file);
        System.out.println(StandardFilePrinter.class.getSimpleName() + " log file: " + file.getAbsolutePath());
        return file;
    }

    @Test
    public void test() throws IOException {
        File logfile = this.createfile();
        String charsetName = StringUtils.CHARSET;
        StandardFilePrinter out = new StandardFilePrinter(logfile, charsetName, false);
        out.println("1");
        out.println("2");
        out.println("3");
        out.close();

        Assert.assertEquals("1", FileUtils.readline(logfile, charsetName, 1));
        Assert.assertEquals("2", FileUtils.readline(logfile, charsetName, 2));
        Assert.assertEquals("3", FileUtils.readline(logfile, charsetName, 3));
    }
}
