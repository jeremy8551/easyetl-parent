package icu.etl.io;

import java.io.File;
import java.io.IOException;

import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BufferedFileOutputStreamTest {

    /**
     * 返回一个临时文件
     *
     * @return
     */
    public static File getFile() {
        return getFile(null);
    }

    /**
     * 使用指定用户名创建一个文件
     *
     * @param name
     * @return
     */
    public static File getFile(String name) {
        if (StringUtils.isBlank(name)) {
            name = FileUtils.getFilenameRandom("testfile", "_tmp") + ".txt";
        }

        File dir = new File(FileUtils.getTempDir(FileUtils.class), "单元测试");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建目录 " + dir.getAbsolutePath() + " 失败!");
        } else {
            return new File(dir, name); // 返回一个临时文件信息
        }
    }

    @Test
    public void testBufferedFileOutputStreamFileString() throws IOException {
        File file = getFile(null);
        BufferedLineWriter out = new BufferedLineWriter(file, "UTF-8", false, 2);
        out.writeLine("1", "\n");
        out.writeLine("2", "\n");
        out.writeLine("3", "\n");
        out.writeLine("4", "\n");
        out.writeLine("5", "\n");
        out.close();

        assertTrue(FileUtils.readline(file, "UTF-8", 1).equals("1"));
        assertTrue(FileUtils.readline(file, "UTF-8", 3).equals("3"));
        assertTrue(FileUtils.readline(file, "UTF-8", 5).equals("5"));
    }

    @Test
    public void testBufferedFileOutputStreamFileStringInt() {
    }

    @Test
    public void testBufferedFileOutputStreamFileStringBooleanInt() {
    }

    @Test
    public void testWriteLine() {
    }

    @Test
    public void testFlush() {
    }

    @Test
    public void testClose() {
    }

}
