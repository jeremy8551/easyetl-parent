package icu.etl.os;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class FtpClientCase {

    /**
     * ftp相关测试案例
     */
    public static void run(OSFileCommand ftp) throws IOException {
        String charsetName = "UTF-8";
        String pwd = ftp.pwd();
        Assert.assertNotNull(pwd);
        Assert.assertTrue(ftp.exists(pwd));
        Assert.assertFalse(ftp.isFile(pwd));
        Assert.assertTrue(ftp.isDirectory(pwd));

        ftp.ls(pwd);

        String testDir0 = pwd + "/test_dir";
        if (ftp.exists(testDir0)) {
            Assert.assertTrue(ftp.rm(testDir0));
            Assert.assertFalse(ftp.exists(testDir0));
        }
        Assert.assertTrue(ftp.mkdir(testDir0));

        File dir = FileUtils.getTempDir("test", SftpClientTest.class.getSimpleName());
        File tempDir = FileUtils.allocate(dir, "f" + Dates.format14(new Date()));
        FileUtils.createDirectory(tempDir);

        File tdf1 = new File(tempDir, "test.txt");
        FileUtils.createFile(tdf1);
        FileUtils.write(tdf1, StringUtils.CHARSET, false, "file content 1");

        File tdf2 = new File(tempDir, "test.txt");
        FileUtils.createFile(tdf2);

        ftp.upload(tdf2, testDir0);
        Assert.assertTrue(ftp.exists(testDir0 + "/" + tdf2.getName()));
        Assert.assertTrue(ftp.isFile(testDir0 + "/" + tdf2.getName()));

        ftp.upload(tempDir, testDir0);
        Assert.assertTrue(ftp.exists(testDir0 + "/" + tempDir.getName()));
        Assert.assertTrue(ftp.isDirectory(testDir0 + "/" + tempDir.getName()));

        List<OSFile> ls = ftp.ls(testDir0);
        assertEquals(2, ls.size());

        File tempDir1 = FileUtils.allocate(dir, "f" + Dates.format08(new Date()));
        FileUtils.createDirectory(tempDir1);

        File dest3 = ftp.download(testDir0 + "/" + tempDir.getName(), tempDir1);
        File rs1 = new File(tempDir1.getAbsolutePath() + "/" + tempDir.getName() + "/" + tdf1.getName());
        Assert.assertTrue(rs1.exists() && "file content 1".equals(FileUtils.readline(rs1, StringUtils.CHARSET, 1)));
        Assert.assertEquals(dest3.getAbsolutePath(), rs1.getParent());

        Assert.assertTrue(ftp.cd(testDir0 + "/" + tempDir.getName()));

        String filepath = testDir0 + "/" + tempDir.getName() + "/" + tdf1.getName();
        String read = ftp.read(filepath, charsetName, 0);
        Assert.assertEquals("file content 1", read);

        ftp.write(filepath, StringUtils.defaultString(ftp.getCharsetName(), charsetName), false, "test11111");
        read = ftp.read(filepath, charsetName, 0);
        Assert.assertEquals("test11111", read);

        ftp.write(filepath, charsetName, true, "22");
        read = ftp.read(filepath, charsetName, 0);
        Assert.assertEquals("test1111122", read);

        String newfilepath = testDir0 + "/" + tempDir.getName() + "/copydir";
        Assert.assertTrue(ftp.mkdir(newfilepath));
        Assert.assertTrue(ftp.copy(filepath, newfilepath));
        read = ftp.read(newfilepath + "/" + FileUtils.getFilename(filepath), charsetName, 0);
        Assert.assertEquals("test1111122", read);

        List<OSFile> find = ftp.find(testDir0, tempDir.getName(), 'd', null);
        for (OSFile f : find) {
            Assert.assertEquals(f.getName(), tempDir.getName());
            Assert.assertEquals(f.getParent(), testDir0);
        }

        String newdir = pwd + "/test_dir_12";
        Assert.assertTrue(ftp.rename(testDir0, newdir));

        Assert.assertTrue(ftp.rm(newdir + "/" + tdf2.getName()));
        Assert.assertTrue(ftp.rm(newdir + "/" + tempDir.getName()));
        Assert.assertTrue(ftp.rm(newdir));

        List<OSFile> ls2 = ftp.ls(pwd);
        for (OSFile f : ls2) {
            System.out.println(f);
        }
    }

}
