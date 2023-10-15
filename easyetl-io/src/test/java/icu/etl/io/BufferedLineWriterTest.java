package icu.etl.io;

import java.io.File;
import java.io.IOException;

import icu.etl.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BufferedLineWriterTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return 返回临时文件
     */
    private File createfile() {
        String name = FileUtils.getFilenameRandom("testfile", "_tmp") + ".txt";
        File dir = new File(FileUtils.getTempDir(FileUtils.class), "单元测试");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException(dir.getAbsolutePath());
        } else {
            return new File(dir, name); // 返回一个临时文件信息
        }
    }

    @Test
    public void testBufferedFileOutputStreamFileString() throws IOException {
        File file = this.createfile();

        BufferedLineWriter out = new BufferedLineWriter(file, "UTF-8", false, 2);
        out.write("0");
        out.writeLine("1", "\n");
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

}
