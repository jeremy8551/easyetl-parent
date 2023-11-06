package icu.etl.os.ssh;

import java.util.List;

import icu.etl.os.OSFile;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.os.WithSSHRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 端口转发测试
 */
@Ignore
public class SSHPortForwardTest {

    public WithSSHRule rule = new WithSSHRule();

    // 本地服务器host与端口
    public String localHost = "127.0.0.1";
    public int localPort = 990;// 本地端口

    // 目标服务器host与端口
    public String remoteHost = "9.124.47.29";
    public int remotePort = 990;

    @Test
    public void test() throws Exception {
        OSSecureShellCommand ssh = rule.getContext().getBean(OSSecureShellCommand.class);
        try {
            Assert.assertTrue(ssh.connect("130.1.16.54", 22, "root", "passw0rd"));
            localPort = ssh.localPortForward(0, remoteHost, remotePort);
            localPort = ssh.localPortForward(0, remoteHost, remotePort);
            this.testSSH();
        } finally {
            ssh.close();
        }
    }

    public void testSSH() throws Exception {
        OSFtpCommand sftp = rule.getContext().getBean(OSFtpCommand.class, "sftp");
        try {
            Assert.assertTrue(sftp.connect(localHost, localPort, "H_TEST_1", "7vs54b%)e1vw5l"));
            System.out.println("pwd " + sftp.pwd());
            List<OSFile> list = sftp.ls("/");
            for (OSFile file : list) {
                System.out.println(file.getLongname());
            }
            System.out.println("exists /creditdatafile/data " + sftp.exists("/creditdatafile/data"));
            System.out.println("exists /creditdatafile/log/file " + sftp.exists("/creditdatafile/log/file"));
            System.out.println("exists /creditdatafile/data/history " + sftp.exists("/creditdatafile/data/history"));
        } finally {
            sftp.close();
        }
    }

}