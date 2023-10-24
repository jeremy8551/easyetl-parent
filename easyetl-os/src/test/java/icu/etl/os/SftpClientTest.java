package icu.etl.os;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import icu.etl.os.ssh.SecureShellCommand;
import icu.etl.os.ssh.SftpCommand;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import icu.jsch.JSchException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SftpClientTest {

    @Test
    public void test1() throws JSchException {
        SftpCommand ftp = new SftpCommand();
        try {
            ftp.connect(TestEnv.getSSHHost(), TestEnv.getSSHPort(), TestEnv.getSSHUsername(), TestEnv.getSSHPassword());
            test(ftp);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            ftp.close();
        }
    }

    @Test
    public void test2() throws JSchException {
        SecureShellCommand ssh = new SecureShellCommand();
        try {
            ssh.connect(TestEnv.getSSHHost(), TestEnv.getSSHPort(), TestEnv.getSSHUsername(), TestEnv.getSSHPassword());
            ssh.execute("pwd");

            String bs = ssh.getStdout();
            System.out.println("before " + bs);
            Ensure.isTrue(StringUtils.isNotBlank(bs), bs);

            test(ssh.getFileCommand());

            ssh.execute("pwd");
            String fs = ssh.getStdout();
            Ensure.isTrue(StringUtils.isNotBlank(fs), fs);
            System.out.println("after " + bs);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            ssh.close();
        }
    }

    public static void test(OSFileCommand ftp) throws IOException {
        String pwd = ftp.pwd();
        assertTrue(pwd != null);
        assertTrue(ftp.exists(pwd));
        assertTrue(!ftp.isFile(pwd));
        assertTrue(ftp.isDirectory(pwd));

        ftp.ls(pwd);

        String testDir0 = pwd + "/test_dir";
        if (ftp.exists(testDir0)) {
            Ensure.isTrue(ftp.rm(testDir0), testDir0);
            Ensure.isTrue(!ftp.exists(testDir0), testDir0);
        }
        assertTrue(ftp.mkdir(testDir0));

        File dir = FileUtils.getTempDir(SftpClientTest.class);
        File tempDir = FileUtils.getFileNoRepeat(dir, "f" + Dates.format14(new Date()));
        FileUtils.createDirectory(tempDir);

        File tdf1 = new File(tempDir, "test.txt");
        FileUtils.createFile(tdf1);
        FileUtils.write(tdf1, StringUtils.CHARSET, false, "file content 1");

        File tdf2 = new File(tempDir, "test.txt");
        FileUtils.createFile(tdf2);

        ftp.upload(tdf2, testDir0);
        assertTrue(ftp.exists(testDir0 + "/" + tdf2.getName()));
        assertTrue(ftp.isFile(testDir0 + "/" + tdf2.getName()));

        ftp.upload(tempDir, testDir0);
        assertTrue(ftp.exists(testDir0 + "/" + tempDir.getName()));
        assertTrue(ftp.isDirectory(testDir0 + "/" + tempDir.getName()));

        List<OSFile> ls = ftp.ls(testDir0);
        assertTrue(ls.size() == 2);

        File tempDir1 = FileUtils.getFileNoRepeat(dir, "f" + Dates.format08(new Date()));
        FileUtils.createDirectory(tempDir1);

        File dest3 = ftp.download(testDir0 + "/" + tempDir.getName(), tempDir1);
        File rs1 = new File(tempDir1.getAbsolutePath() + "/" + tempDir.getName() + "/" + tdf1.getName());
        assertTrue(rs1.exists() && "file content 1".equals(FileUtils.readline(rs1, StringUtils.CHARSET, 1)));
        Ensure.isTrue(dest3.getAbsolutePath().equals(rs1.getParent()), dest3, rs1);

        assertTrue(ftp.cd(testDir0 + "/" + tempDir.getName()));

        String filepath = testDir0 + "/" + tempDir.getName() + "/" + tdf1.getName();
        String read = ftp.read(filepath, "UTF-8", 0);
        Ensure.isTrue("file content 1".equals(read), read);

        ftp.write(filepath, ftp.getCharsetName(), false, "test11111");
        read = ftp.read(filepath, "UTF-8", 0);
        Ensure.isTrue("test11111".equals(read), read);

        ftp.write(filepath, null, true, "22");
        read = ftp.read(filepath, "UTF-8", 0);
        Ensure.isTrue("test1111122".equals(read), read);

        String newfilepath = testDir0 + "/" + tempDir.getName() + "/copydir";
        Ensure.isTrue(ftp.mkdir(newfilepath), newfilepath);
        Ensure.isTrue(ftp.copy(filepath, newfilepath), filepath, newfilepath);
        read = ftp.read(newfilepath + "/" + FileUtils.getFilename(filepath), "UTF-8", 0);
        Ensure.isTrue("test1111122".equals(read), read);

        List<OSFile> find = ftp.find(testDir0, tempDir.getName(), 'd', null);
        for (OSFile f : find) {
            Ensure.isTrue(f.getName().equals(tempDir.getName()), tempDir);
            Ensure.isTrue(f.getParent().equals(testDir0));
        }

        String newdir = pwd + "/test_dir_12";
        Ensure.isTrue(ftp.rename(testDir0, newdir), testDir0, newdir);

        Ensure.isTrue(ftp.rm(newdir + "/" + tdf2.getName()));
        Ensure.isTrue(ftp.rm(newdir + "/" + tempDir.getName()));
        Ensure.isTrue(ftp.rm(newdir), newdir);

        List<OSFile> ls2 = ftp.ls("/home/user/shell/qyzx/r_file");
        for (OSFile f : ls2) {
            System.out.println(f);
        }

        assertTrue(true);
    }
}
