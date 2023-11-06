package icu.etl.os;

import javax.script.SimpleBindings;

import com.jcraft.jsch.JSchException;
import icu.etl.os.ssh.SecureShellCommand;
import icu.etl.os.ssh.SftpCommand;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class SftpClientTest {

    @Rule
    public WithSSHRule rule = new WithSSHRule();

    @Test
    public void test1() throws JSchException {
        SimpleBindings env = rule.getEnvironment();
        String sshhost = (String) env.get("ssh.host");
        int sshport = Integer.parseInt((String) env.get("ssh.port"));
        String sshusername = (String) env.get("ssh.username");
        String sshpassword = (String) env.get("ssh.password");
        String homedir = (String) env.get("ssh.homedir");

        SftpCommand ftp = new SftpCommand();
        try {
            Assert.assertTrue(ftp.connect(sshhost, sshport, sshusername, sshpassword));
            if (StringUtils.isNotBlank(homedir)) {
                Assert.assertTrue(ftp.cd(homedir));
            }
            FtpClientCase.run(ftp);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            ftp.close();
        }
    }

    @Test
    public void test2() throws JSchException {
        SimpleBindings env = rule.getEnvironment();
        String sshhost = (String) env.get("ssh.host");
        int sshport = Integer.parseInt((String) env.get("ssh.port"));
        String sshusername = (String) env.get("ssh.username");
        String sshpassword = (String) env.get("ssh.password");
        String homedir = (String) env.get("ssh.homedir");

        SecureShellCommand ssh = new SecureShellCommand();
        try {
            Assert.assertTrue(ssh.connect(sshhost, sshport, sshusername, sshpassword));
            ssh.execute("pwd");
            String sout = ssh.getStdout();
            System.out.println("before dir: " + sout);
            Assert.assertTrue(StringUtils.isNotBlank(sout));

            // 运行文件测试案例
            OSFileCommand filecmd = ssh.getFileCommand();
            if (StringUtils.isNotBlank(homedir)) {
                Assert.assertTrue(filecmd.cd(homedir));
            }
            FtpClientCase.run(filecmd);

            ssh.execute("pwd");
            String stdout = ssh.getStdout();
            Assert.assertTrue(StringUtils.isNotBlank(stdout));
            System.out.println("after dir: " + sout);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            ssh.close();
        }
    }

}
