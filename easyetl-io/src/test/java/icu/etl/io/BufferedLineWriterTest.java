package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BufferedLineWriterTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return 返回临时文件
     */
    private File createfile() throws IOException {
        String name = FileUtils.getFilenameRandom("testfile", "_tmp") + ".txt";
        File dir = FileUtils.getTempDir(FileUtils.class);
        FileUtils.createDirectory(dir);
        File file = new File(dir, name);
        FileUtils.createFile(file);
        return file;
    }

    @Test
    public void test() throws IOException {
        File file = this.createfile();

        BufferedLineWriter out = new BufferedLineWriter(file, "UTF-8", false, 2);
        out.write("0");
        out.writeLine("1");
        out.writeLine("2", "\n");
        out.writeLine("3", "\n");
        out.writeLine("4", "\n");
        out.writeLine("5", "\n");
        out.flush();
        out.close();

        assertEquals(5, out.getLineNumber());
        assertEquals("\n", out.getLineSeparator());
        assertEquals("UTF-8", out.getCharsetName());

        assertEquals("01", FileUtils.readline(file, "UTF-8", 1));
        assertEquals("3", FileUtils.readline(file, "UTF-8", 3));
        assertEquals("5", FileUtils.readline(file, "UTF-8", 5));
    }

    @Test
    public void test1() throws IOException {
        File file = this.createfile();

        OutputStreamWriter writer = IO.getFileWriter(file, "UTF-8", false);
        BufferedLineWriter out = new BufferedLineWriter(writer, 2);
        out.write("0");
        out.writeLine("1");
        out.writeLine("2", "\n");
        out.writeLine("3", "\n");
        out.writeLine("4", "\n");
        out.writeLine("5", "\n");
        out.flush();
        out.close();

        assertEquals(5, out.getLineNumber());
        assertEquals("\n", out.getLineSeparator());
        assertEquals(null, out.getCharsetName());

        assertEquals("01", FileUtils.readline(file, "UTF-8", 1));
        assertEquals("3", FileUtils.readline(file, "UTF-8", 3));
        assertEquals("5", FileUtils.readline(file, "UTF-8", 5));
    }

    @Test
    public void test2() throws IOException {
        File file = this.createfile();

        BufferedLineWriter out = new BufferedLineWriter(file, "UTF-8", 2);
        out.write("0");
        out.writeLine("1");
        out.writeLine("2", "\n");
        out.writeLine("3", "\n");
        out.writeLine("4", "\n");
        out.writeLine("5", "\n");
        out.flush();
        out.close();

        assertEquals(5, out.getLineNumber());
        assertEquals("\n", out.getLineSeparator());
        assertEquals("UTF-8", out.getCharsetName());

        assertEquals("01", FileUtils.readline(file, "UTF-8", 1));
        assertEquals("3", FileUtils.readline(file, "UTF-8", 3));
        assertEquals("5", FileUtils.readline(file, "UTF-8", 5));
    }

    @Test
    public void test3() throws IOException {
        File file = this.createfile();

        BufferedLineWriter out = new BufferedLineWriter(file, "UTF-8");
        out.write("0");
        out.writeLine("1");
        out.writeLine("2", "\n");
        out.writeLine("3", "\n");
        out.writeLine("4", "\n");
        out.writeLine("5", "\n");
        out.flush();
        out.close();

        assertEquals(5, out.getLineNumber());
        assertEquals("\n", out.getLineSeparator());
        assertEquals("UTF-8", out.getCharsetName());

        assertEquals("01", FileUtils.readline(file, "UTF-8", 1));
        assertEquals("3", FileUtils.readline(file, "UTF-8", 3));
        assertEquals("5", FileUtils.readline(file, "UTF-8", 5));
    }

    @Test
    public void test0() throws IOException {
        try {
            File file = this.createfile();
            OutputStreamWriter writer = IO.getFileWriter(file, "UTF-8", false);
            new BufferedLineWriter(writer, 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("0", e.getMessage());
        }

        try {
            new BufferedLineWriter(null, 0);
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }
    }

}