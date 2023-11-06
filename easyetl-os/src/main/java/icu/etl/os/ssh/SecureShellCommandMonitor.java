package icu.etl.os.ssh;

import java.io.IOException;
import java.util.Date;

import icu.etl.io.BufferedLineReader;
import icu.etl.log.Log;
import icu.etl.os.OSCommandException;
import icu.etl.os.OSConnectCommand;
import icu.etl.time.Timer;
import icu.etl.time.TimerException;
import icu.etl.time.TimerTask;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 用于防止 SSH 协议实现类超时中断退出
 */
public class SecureShellCommandMonitor extends TimerTask {

    /** 循环检查的周期，默认3分钟 */
    public static int PERIOD = 3 * 60 * 1000;

    /** 启动命令超时监听器的阀值,单位秒 */
    public final static int START_MONITOR = 30;

    /** 最近一次执行命令的时间 */
    private Date lastRunningTime;

    /** 监听器归属的 SSH 终端 */
    private SecureShellCommand terminal;

    /** 终端使用的日志输出接口 */
    private Log log;

    /** 操作系统host */
    private String host;

    /** sshd服务的端口 */
    private int port;

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;

    /**
     * 初始化
     */
    public SecureShellCommandMonitor() {
        this.setTaskId("SSHClientMonitor" + StringUtils.toRandomUUID());
        this.setSchedule(Timer.SCHEDULE_DELAY_LOOP);
        this.setPeriod(SecureShellCommandMonitor.PERIOD);
        this.setDelay(SecureShellCommandMonitor.PERIOD);
    }

    /**
     * 启动监听器
     */
    public synchronized boolean startMonitor(SecureShellCommand client) {
        this.terminal = client;
        this.log = client.getLog();
        this.host = client.getProperty(OSConnectCommand.host);
        this.port = Integer.parseInt(client.getProperty(OSConnectCommand.port));
        this.username = client.getProperty(OSConnectCommand.username);
        this.password = client.getProperty(OSConnectCommand.password);
        return true;
    }

    public void execute() throws TimerException {
        if (this.sendKeepAliveMsg(this.terminal)) {
            return;
        }

        if (this.terminal == null) {
            this.cancel();
            return;
        }

        String pid = this.terminal.getPid();
        if (StringUtils.isBlank(pid)) {
            this.cancel();
            return;
        }

        SecureShellCommand client = new SecureShellCommand();
        try {
            if (!client.connect(this.host, this.port, this.username, this.password)) { // establish a ssh2 connection
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getSSH2JschMessage(1, this.username + "@" + this.host + ":" + this.port + "?password=" + this.password));
                }
                return;
            }

            if (!this.isRunning(client, pid) && this.isKilled(client, pid, this.lastRunningTime)) {
                this.terminal.getConfig().setProperty("terminate", "killed");
            }
        } catch (Exception e) {
            log.warn(ResourcesUtils.getSSH2JschMessage(4), e);
        } finally {
            client.close();
        }
    }

    /**
     * 判断ssh终端是否正在执行命令
     *
     * @param client ssh终端
     * @param pid    命令的进程编号
     * @return
     * @throws OSCommandException
     */
    public boolean isRunning(SecureShellCommand client, String pid) throws OSCommandException {
        String shell = "ps -p " + pid + " -o comm=";
        int exitcode = client.execute(shell, 60000, null, null);
        if (exitcode == 0 || exitcode == 1) {
            if (StringUtils.isBlank(client.getStdout())) { // not exists system process
                log.info(ResourcesUtils.getSSH2JschMessage(6, pid));

                Timer.sleep(3000); // wait 3sec
                this.terminal.terminate(); // terminate task
                this.cancel();
                return false;
            } else {
                log.info(ResourcesUtils.getSSH2JschMessage(5, pid));
                this.lastRunningTime = new Date();
                return true;
            }
        } else {
            log.warn(ResourcesUtils.getSSH2JschMessage(4, shell + ", exitcode is " + exitcode));
            return true;
        }
    }

    /**
     * 通知 SSH 服务器保持连接
     *
     * @param client
     * @return
     */
    public boolean sendKeepAliveMsg(SecureShellCommand client) {
        try {
            if (client == null) {
                return false;
            } else {
                client.getSession().sendKeepAliveMsg();
                return true;
            }
        } catch (Exception e) {
            if (log == null) {
                e.printStackTrace();
            } else {
                log.error("sendKeepAliveMsg error!", e);
            }
            return false;
        }
    }

    /**
     * 判断进程是否是被kill命令终止的
     *
     * @param client         ssh终端
     * @param pid            进程编号
     * @param lastActiveTime 进程最后活动时间(用于缩小在查找历史记录的时间范围)
     * @return
     * @throws OSCommandException
     * @throws IOException
     */
    private boolean isKilled(SecureShellCommand client, String pid, Date lastActiveTime) throws OSCommandException, IOException {
        client.execute("export HISTTIMEFORMAT=\"%F %T \" && history", 0, null, null);
        String historyLog = client.getStdout();
        int dateStartPos = -1;
        int timeEndPos = -1;
        int cmdStartPos = -1;
        BufferedLineReader in = new BufferedLineReader(historyLog);
        try {
            while (in.hasNext()) {
                String line = in.next();
                if (cmdStartPos == -1) {
                    String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(line));
                    if (array.length >= 4 && Dates.testFormat10(array[1])) {
                        dateStartPos = line.indexOf(array[1]);
                        if (dateStartPos == -1) {
                            throw new IllegalArgumentException(line);
                        }

                        int timeStartPos = line.indexOf(array[2], dateStartPos + array[1].length()); // index of timestamp string pos
                        if (timeStartPos == -1) {
                            throw new IllegalArgumentException(line);
                        }

                        timeEndPos = timeStartPos + array[2].length();
                        cmdStartPos = StringUtils.indexOfNotBlank(line, timeEndPos, -1); // shell command start position
                        if (cmdStartPos == -1) {
                            throw new IllegalArgumentException(line);
                        }
                    }
                }

                // 在用户历史命令列表中查找进程编号
                int killCmdIdx = StringUtils.indexOf(line, "kill", cmdStartPos, true);
                if (killCmdIdx != -1) {
                    int shellPidIdx = StringUtils.indexOf(line, pid, killCmdIdx, true);
                    if (shellPidIdx != -1) {
                        if (lastActiveTime == null) {
                            log.info(ResourcesUtils.getSSH2JschMessage(7, pid, line));
                            return true;
                        }

                        String dateStr = line.substring(dateStartPos, timeEndPos);
                        if (Dates.parse(dateStr).compareTo(lastActiveTime) >= 0) {
                            log.info(ResourcesUtils.getSSH2JschMessage(7, pid, line));
                            return true;
                        }
                    }
                }
            }
            return false;
        } finally {
            IO.close(in);
        }
    }

    public void terminate() throws TimerException {
    }

}
