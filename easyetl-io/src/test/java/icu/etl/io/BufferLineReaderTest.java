package icu.etl.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/22
 */
public class BufferLineReaderTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return 返回临时文件
     */
    private File createfile() throws IOException {
        String name = FileUtils.getFilenameRandom("BufferedReaderTestfile", "_tmp") + ".txt";
        File dir = FileUtils.getTempDir(FileUtils.class);
        FileUtils.createDirectory(dir);
        File file = new File(dir, name);
        FileUtils.createFile(file);
        System.out.println(BufferedReader.class.getSimpleName() + " testfile: " + file.getAbsolutePath());
        return file;
    }

    @Test
    public void test() throws IOException {
        File file = this.createfile();

        FileUtils.write(file, "utf-8", false, "1\r2\n3\r\n4\n");
        BufferedReader in = IO.getBufferedReader(file, "utf-8");
        Assert.assertEquals("1", in.readLine());
        Assert.assertEquals("2", in.readLine());
        Assert.assertEquals("3", in.readLine());
        Assert.assertEquals("4", in.readLine());
        in.close();
    }
}
