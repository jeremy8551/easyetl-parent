package icu.etl.os.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;

import icu.etl.log.STD;
import icu.etl.os.OSConnectCommand;
import icu.etl.os.OSException;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;
import icu.jsch.JSch;
import icu.jsch.Session;

/**
 * SSH 端口转发协议的实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-08-10
 */
public class SecureShellForwardCommand implements OSConnectCommand {

    /** 超时时间，单位秒 */
    public static int CLOSE_PORTFORWARDLOCAL_TIMEOUT = 120;

    /** JSch 组件 */
    protected JSch jsch = new JSch();

    /** ssh connection transaction */
    protected Session session;

    /** 代理服务器配置 */
    protected String proxySSHHost;
    protected int proxySSHPort;
    protected String proxySSHUsername;
    protected String proxySSHPassword;

    /** 分配的本地端口 */
    protected int localport;

    protected String charsetName;
    protected OutputStream stdout;
    protected OutputStream stderr;

    /**
     * 初始化
     */
    public SecureShellForwardCommand() {
    }

    /**
     * 设置错误信息输出接口
     *
     * @param output
     */
    public void setStderr(OutputStream output) {
        this.stderr = output;
    }

    /**
     * 设置标准信息输出接口
     *
     * @param output
     */
    public void setStdout(OutputStream output) {
        this.stdout = output;
    }

    /**
     * 设置输出字符串的字符集编码
     *
     * @param charsetName
     */
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    /**
     * 输出标准信息
     *
     * @param str
     * @throws IOException
     */
    public void stdout(String str) throws IOException {
        if (this.stdout == null) {
            STD.out.info(str);
        } else {
            this.stdout.write(StringUtils.toBytes(str, this.charsetName));
            this.stdout.flush();
        }
    }

    /**
     * 输出错误信息
     *
     * @param str
     * @param o
     */
    public void stderr(String str, Throwable o) {
        if (this.stderr == null) {
            STD.out.error(str);
        } else {
            try {
                this.stderr.write(StringUtils.toBytes(str, this.charsetName));
                if (o != null) {
                    this.stderr.write(StringUtils.toBytes(StringUtils.toString(o), this.charsetName));
                }
                this.stderr.flush();
            } catch (Throwable e) {
                throw new OSException(str, e);
            }
        }
    }

    public boolean connect(String proxyHost, int proxySSHPort, String proxySSHUsername, String proxySSHPassword) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug("ssh " + proxySSHUsername + "@" + proxyHost + ":" + proxySSHPort + "?password=" + proxySSHPassword);
        }

        try {
            if (this.session != null && this.session.isConnected()) {
                this.close();
            }

            this.session = this.jsch.getSession(proxySSHUsername, proxyHost, proxySSHPort);
            this.session.setPassword(proxySSHPassword);

            // Set the prompt when logging in for the first time, optional value: (ask | yes | no)
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.connect();

            this.stdout(this.session.getServerVersion());

            this.proxySSHHost = proxyHost;
            this.proxySSHPort = proxySSHPort;
            this.proxySSHUsername = proxySSHUsername;
            this.proxySSHPassword = proxySSHPassword;
            return true;
        } catch (Throwable e) {
            this.stderr("ssh " + proxySSHUsername + "@" + proxyHost + ":" + proxySSHPort + "?password=" + proxySSHPassword + " fail!", e);
            return false;
        }
    }

    /**
     * 使用本地端口建立转发隧道
     *
     * @param localPort  本地端口
     * @param remoteHost 目标服务器
     * @param remotePort 目标服务器的SSH端口
     * @return
     */
    public int localPortForward(int localPort, String remoteHost, int remotePort) {
        if (StringUtils.isBlank(remoteHost)) {
            throw new IllegalArgumentException(remoteHost);
        }
        if (remotePort <= 0) {
            throw new IllegalArgumentException(String.valueOf(remotePort));
        }

        if (this.session == null || !this.session.isConnected()) {
            throw new OSException("use connect() establish an connection first!");
        }

        if (this.localport > 0) {
            try {
                this.session.delPortForwardingL(this.localport);
            } catch (Throwable e) {
                this.stderr("delete Port Forward Local fail!", e);
            }
        }

        if (localPort <= 0) {
            ServerSocket server = null;
            try {
                server = new ServerSocket(0);
                localPort = server.getLocalPort();
            } catch (Throwable e) {
                throw new OSException("get localhost port fail!", e);
            } finally {
                IO.closeQuiet(server);
                IO.closeQuiet(server);
                IO.closeQuiet(server);
            }
        }

        try {
            // register local port forward
            int port = this.session.setPortForwardingL(localPort, remoteHost, remotePort);
            this.localport = port;

            // remote port forward
            // assinged_port = session.setPortForwardingR(990, "", 990);
            // delete forward port
            // session.delPortForwardingL(localPort);
            // session.disconnect();

            String localServer = String.valueOf(port);
            String proxyServer = this.proxySSHUsername + "@" + this.proxySSHHost + ":" + proxySSHPort;
            String remotServer = remoteHost + ":" + remotePort;
            this.stdout(ResourcesUtils.getSSH2JschMessage(16, localServer, proxyServer, remotServer));
            return port;
        } catch (Throwable e) {
            this.stderr("establishing an SSH tunnel fail!", e);
            this.localport = -1;
            return -1;
        }
    }

    public void close() {
        if (this.session != null) {
            TimeWatch watch = new TimeWatch();
            while (true) {
                try {
                    if (this.session != null) {
                        this.session.disconnect(); // disconnect ssh server
                        this.session = null;
                        break;
                    }
                } catch (Throwable e) {
                    STD.out.error("shutdown port forward local error!", e);
                    if (watch.useSeconds() <= SecureShellForwardCommand.CLOSE_PORTFORWARDLOCAL_TIMEOUT) {
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

}
