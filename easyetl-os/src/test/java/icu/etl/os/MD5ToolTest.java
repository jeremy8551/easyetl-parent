package icu.etl.os;

import java.io.File;
import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.crypto.MD5Encrypt;
import icu.etl.io.BufferedLineWriter;
import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.Numbers;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class MD5ToolTest {

    @Test
    public void testString() {
        try {
            String str = "测试字符串阿斯蒂芬阿斯兰的军开发lkjsadlfsadlfj就";
            String md5 = MD5Encrypt.encrypt(str, null);
            System.out.println("MD5Encrypt.encrypt value is " + md5);

            AnnotationEasyetlContext context = new AnnotationEasyetlContext();
            OSSecureShellCommand shell = context.getBean(OSSecureShellCommand.class);
            try {
                shell.connect(TestEnv.getSSHHost(), TestEnv.getSSHPort(), TestEnv.getSSHUsername(), TestEnv.getSSHPassword());
                shell.execute("echo -n " + str + " | md5sum "); // 判断md5值与linux上是否一致
                String stdout = shell.getStdout();
                System.out.println("ssh2 stdout: " + stdout);
                String linuxMD5 = StringUtils.splitByBlank(StringUtils.trimBlank(stdout))[0];
                Ensure.isTrue(linuxMD5.toLowerCase().equals(md5.toLowerCase()), linuxMD5 + " != " + md5);
            } finally {
                shell.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * 对文件生成md5的功能进行测试
     *
     * @throws IOException
     * @throws ScriptException
     * @throws OSCommandException
     */
    @Test
    public void testFile() throws IOException, ScriptException, OSCommandException { // 对MD5功能进行测试
        String filename = "md5testfile.txt";
        File file = new File(FileUtils.getTempDir(this.getClass()), filename);
        System.out.println("filepath: " + file.getAbsolutePath());

        BufferedLineWriter os = new BufferedLineWriter(file, StringUtils.CHARSET);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            int column = Numbers.getRandom();
            for (int j = 1; j <= column; j++) {
                buf.append(Dates.currentTimeStamp());
                buf.append("||");
            }
            os.writeLine(buf.toString(), String.valueOf(FileUtils.lineSeparator));
        }
        os.close();

        String md5 = MD5Encrypt.encrypt(file, null);
        System.out.println("md5: " + md5);

        String host = TestEnv.getSSHHost();
        int port = TestEnv.getSSHPort();
        String username = TestEnv.getSSHUsername();
        String password = TestEnv.getSSHPassword();

        ScriptEngineManager sm = new ScriptEngineManager();
        ScriptEngine e = sm.getEngineByExtension("etl");
        try {
            e.eval("sftp " + username + "@" + host + ":" + port + "?password=" + password + ";");
            e.eval("put " + file.getAbsolutePath() + " `pwd`");
            e.eval("bye");

            AnnotationEasyetlContext context = new AnnotationEasyetlContext();
            OSSecureShellCommand shell = context.getBean(OSSecureShellCommand.class);
            try {
                shell.connect(host, port, username, password);
                shell.execute("md5sum `pwd`/" + filename); // 判断md5值与linux上是否一致
                String md5value = shell.getStdout();
                String val = StringUtils.splitByBlank(StringUtils.trimBlank(md5value))[0];
                Ensure.isTrue(val.equals(md5), val + " != " + md5);
            } finally {
                shell.close();
            }

        } finally {
            e.eval("exit 0");
        }

        // 27763e3a87c331b150c27ea4ba9fce2d
//
//		try {
//			SecureShell.executeCommand("130.1.10.10", 22, "etl@user@130.1.10.104", "xxx", ". ~/.bashrc && . ~/.profile && md5sum -b /home/user/shell/qyzx/ECCD_FINISH.dat ");
//		} catch (SecureShellException e) { // 如果发生异常自动打印
//			e.printStackTrace();
//		}
    }
}
