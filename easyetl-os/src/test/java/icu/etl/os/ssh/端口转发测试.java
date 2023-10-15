package icu.etl.os.ssh;

import java.util.List;

import icu.etl.ioc.BeanFactory;
import icu.etl.os.OSFile;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.util.Ensure;

public class 端口转发测试 {

    // 本地服务器host与端口
    public static String localHost = "127.0.0.1";
    public static int localPort = 990;// 本地端口

    // 目标服务器host与端口
    public static String remoteHost = "9.124.47.29";
    public static int remotePort = 990;

    public static void testSSH() throws Exception {
        OSFtpCommand client = BeanFactory.get(OSFtpCommand.class, "sftp");
        try {
            Ensure.isTrue(client.connect(localHost, localPort, "HEBYH_TEST_1", "7vs54b%)e1vw5l"));
            System.out.println("pwd " + client.pwd());
            List<OSFile> list = client.ls("/");
            for (OSFile file : list) {
                System.out.println(file.getLongname());
            }
            System.out.println("exists /creditdatafile/data " + client.exists("/creditdatafile/data"));
            System.out.println("exists /creditdatafile/log/file " + client.exists("/creditdatafile/log/file"));
            System.out.println("exists /creditdatafile/data/history " + client.exists("/creditdatafile/data/history"));
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        OSSecureShellCommand server = BeanFactory.get(OSSecureShellCommand.class);
        try {
            Ensure.isTrue(server.connect("130.1.16.54", 22, "root", "passw0rd"));
            localPort = server.localPortForward(0, remoteHost, remotePort);
            localPort = server.localPortForward(0, remoteHost, remotePort);
            testSSH();
        } finally {
            server.close();
        }

    }
}