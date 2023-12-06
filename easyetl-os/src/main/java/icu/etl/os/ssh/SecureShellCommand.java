package icu.etl.os.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import icu.etl.annotation.EasyBean;
import icu.etl.collection.ByteBuffer;
import icu.etl.io.BufferedLineReader;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSCommandException;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSConnectCommand;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.os.OSShellCommand;
import icu.etl.os.internal.OSCommandStdoutsImpl;
import icu.etl.os.internal.OSCommandUtils;
import icu.etl.time.Timer;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharsetName;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * SSH 协议的终端实现类
 */
@EasyBean(name = "linux", description = "jsch")
public class SecureShellCommand implements OSSecureShellCommand, EasyContextAware {
    private final static Log log = LogFactory.getLog(SecureShellCommand.class);

    public final static String charset = "charset";

    /** ssh连接 */
    private JSch conn;

    /** ssh协议会话信息 */
    private Session session;

    /** 命令的标准信息的输出接口 */
    private OutputStream stdout;

    /** 命令的错误信息的输出接口 */
    private OutputStream stderr;

    /** 命令的标准输出信息 */
    private ByteBuffer stdoutLog;

    /** 命令的错误输出信息 */
    private ByteBuffer stderrLog;

    /** true表示已终端当前执行的命令 */
    private volatile boolean terminate;

    /** ssh协议客户端的配置信息 */
    private Properties config;

    /** ssh协议的定时器 */
    private Timer timer;

    /** ssh协议超时监听器 */
    private SecureShellCommandMonitor monitor;

    /** SSH协议的端口转发协议 */
    private List<SecureShellForwardCommand> forwards;

    /** SFTP客户端 */
    private SftpCommand ftp;

    /** ssh协议连接信息 username@host:port */
    private String connectInfo;

    /** shell命令提示符，类似于: [was@host] */
    private String shellPrompt;

    /** true表示连接处于活动状态 false表示已通过 close() 方法关闭连接 */
    private boolean alive;

    /** 环境文件集合 */
    private List<String> envfiles;

    /** 容器上下文信息 */
    private EasyContext context;

    /**
     * 初始化
     */
    public SecureShellCommand() {
        JSch.setLogger(new JschLogger());
        this.shellPrompt = "";
        this.stdoutLog = new ByteBuffer(300);
        this.stderrLog = new ByteBuffer(300);
        this.config = new Properties();
        this.timer = new Timer();
        this.envfiles = new ArrayList<String>();
        this.ftp = new SftpCommand();
        this.forwards = new ArrayList<SecureShellForwardCommand>();
        this.monitor = new SecureShellCommandMonitor();
    }

    public void setContext(EasyContext context) {
        this.context = context;
    }

    /**
     * 返回属性信息
     *
     * @return
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * 返回会话信息
     *
     * @return
     */
    protected Session getSession() {
        return session;
    }

    /**
     * 返回属性值
     *
     * @param key 属性名
     * @return
     */
    public String getProperty(String key) {
        return this.config.getProperty(key);
    }

    /**
     * 返回命令的进程编号
     *
     * @return
     */
    public String getPid() {
        return this.config.getProperty("pid");
    }

    /**
     * 保存命令的进程编号
     *
     * @param pid
     */
    protected void setPid(String pid) {
        this.config.setProperty("pid", pid);
    }

    public String getCharsetName() {
        return StringUtils.defaultString(this.config.getProperty(SecureShellCommand.charset), StringUtils.CHARSET);
    }

    public void setCharsetName(String charsetName) {
        if (StringUtils.lookupCharset(charsetName) == null) {
            throw new IllegalArgumentException(charsetName);
        }
        this.config.setProperty(SecureShellCommand.charset, charsetName);
    }

    public synchronized boolean connect(String host, int port, String username, String password) {
        this.alive = false;
        try {
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Throwable e) {
            if (log.isTraceEnabled()) {
                log.trace(e.getLocalizedMessage(), e);
            }
        }

        this.conn = new JSch();
        try {
            this.session = this.conn.getSession(username, host, port);
            this.session.setPassword(password);
            this.session.setUserInfo(this.getUserInfo(username, password));
            this.session.setConfig("StrictHostKeyChecking", "no");
            this.session.setServerAliveInterval(60);
            this.session.setTimeout(0);
            this.session.connect(0);

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getSSH2JschMessage(13, "SSH2", username + "@" + host + ":" + port + "?password=" + password + " " + this.session.getServerVersion()));
            }

            this.config.clear();
            this.config.put(OSConnectCommand.host, host);
            this.config.put(OSConnectCommand.port, String.valueOf(port));
            this.config.put(OSConnectCommand.username, username);
            this.config.put(OSConnectCommand.password, password);
            this.terminate = false;
            this.connectInfo = username + "@" + host + ":" + port;
            this.init();

            try {
                this.ftp.closeChannelSftp();
            } catch (Throwable e) {
                if (log.isTraceEnabled()) {
                    log.trace(e.getLocalizedMessage(), e);
                }
            } finally {
                this.ftp.setSession(this.session);
                this.ftp.setRemoteServerName(this.connectInfo);
            }

            this.alive = true;
            return true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(ResourcesUtils.getSSH2JschMessage(1, "ssh " + username + "@" + host + ":" + port + "?password=" + password), e);
            }
            return false;
        }
    }

    /**
     * 判断当前 SSH2 连接是否已经中断，如果连接已中断则重新建立连接
     *
     * @throws OSCommandException 建立连接失败
     */
    public void ensureConnected() throws OSCommandException {
        if (this.alive && !this.isConnected()) {
            String host = this.config.getProperty(OSConnectCommand.host);
            String port = this.config.getProperty(OSConnectCommand.port);
            String username = this.config.getProperty(OSConnectCommand.username);
            String password = this.config.getProperty(OSConnectCommand.password);

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getSSH2JschMessage(17, host + ":" + port));
            }

            try {
                if (this.session != null) {
                    this.session.disconnect();
                }
            } catch (Throwable e) {
                if (log.isTraceEnabled()) {
                    log.trace(e.getLocalizedMessage(), e);
                }
            }

            this.conn = new JSch();
            try {
                this.session = this.conn.getSession(username, host, Integer.parseInt(port));
                this.session.setPassword(password);
                this.session.setUserInfo(this.getUserInfo(username, password));
                this.session.setConfig("StrictHostKeyChecking", "no");
                this.session.setServerAliveInterval(60);
                this.session.setTimeout(0);
                this.session.connect(0);

                try {
                    this.ftp.closeChannelSftp();
                } catch (Throwable e) {
                    if (log.isTraceEnabled()) {
                        log.trace(e.getLocalizedMessage(), e);
                    }
                } finally {
                    this.ftp.setSession(this.session);
                    this.ftp.setRemoteServerName(this.connectInfo);
                }
            } catch (Throwable e) {
                throw new OSCommandException(ResourcesUtils.getSSH2JschMessage(1, "ssh " + username + "@" + host + ":" + port + "?password=" + password), e);
            }
        }
    }

    public void close() {
        try {
            this.ftp.closeChannelSftp();
        } catch (Throwable e) {
            if (log.isTraceEnabled()) {
                log.trace(e.getLocalizedMessage(), e);
            }
        }

        this.alive = false;
        this.config.clear();
        this.terminate = false;
        try {
            if (this.session != null) {
                this.session.disconnect();
            }
            this.session = null;
            this.connectInfo = null;
        } catch (Throwable e) {
            try {
                if (this.session != null) {
                    this.session.disconnect();
                }
                this.session = null;
            } catch (Throwable e1) {
                log.error(ResourcesUtils.getSSH2JschMessage(9), e1);
            }
        } finally {
            Object[] array = this.forwards.toArray(new SecureShellForwardCommand[this.forwards.size()]);
            IO.close(array);
            this.forwards.clear();
        }
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * 返回用户信息
     *
     * @param username
     * @param password
     * @return
     */
    public UserInfo getUserInfo(String username, String password) {
        DefaultUserInfo user = new DefaultUserInfo(log);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public int execute(String command) throws OSCommandException {
        return this.execute(command, 0, this.stdout, this.stderr);
    }

    public int execute(String command, long timeout) throws OSCommandException {
        return this.execute(command, timeout, this.stdout, this.stderr);
    }

    public synchronized int execute(String command, long timeout, OutputStream outSideStdout, OutputStream outSideStderr) throws OSCommandException {
        return this.run(command, timeout, outSideStdout, outSideStderr);
    }

    /**
     * 执行命令
     *
     * @param command
     * @param timeout
     * @param outSideStdout
     * @param outSideStderr
     * @return
     * @throws OSCommandException
     */
    protected int run(String command, long timeout, OutputStream outSideStdout, OutputStream outSideStderr) throws OSCommandException {
        this.ensureConnected();
        TimeWatch watch = new TimeWatch();

        this.config.remove("pid");
        this.config.remove("terminate");
        this.stdoutLog.restore(512);
        this.stderrLog.restore(512);

        ChannelExec channel = null;
        try {
            channel = (ChannelExec) this.session.openChannel("exec");

            String[] array = this.parseSuCommand(command);
            if (array == null) {
                String str = this.toShellCommand(command);
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getSSH2JschMessage(11, this.connectInfo, str));
                }
                channel.setCommand(str);
            } else {
                String str = this.toShellCommand(array[0]);
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getSSH2JschMessage(11, this.connectInfo, str + " -> " + array[1]));
                }
                channel.setCommand(str);
                OutputStream out = channel.getOutputStream();

                channel.connect();
                out.write(array[1].getBytes());
                out.flush();
            }

            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            if (timeout > 0) {
                channel.connect((int) timeout);
            } else {
                channel.connect();
            }

            byte[] buffer = new byte[1024];
            while (true) {
                if (log.isDebugEnabled() && watch.useSeconds() > 0 && watch.useSeconds() % 30 == 0) {
                    log.debug(ResourcesUtils.getSSH2JschMessage(14, this.connectInfo, 30));
                }

                if (this.terminate || !this.isConnected()) {
                    this.stopMonitor();
                }

                while (!this.terminate && stdout.available() > 0) {
                    int length = stdout.read(buffer, 0, buffer.length);
                    if (length < 0 || !this.isConnected()) {
                        break;
                    }

                    int size = this.extractPid(buffer, length);
                    this.startMonitor(watch.useSeconds());
                    this.stdoutLog.append(buffer, 0, size);

                    try {
                        if (outSideStdout != null) {
                            outSideStdout.write(buffer, 0, size);
                            outSideStdout.flush();
                        }
                    } catch (Throwable e) {
                        if (log.isErrorEnabled()) {
                            log.error(ResourcesUtils.getSSH2JschMessage(2, this.stdoutLog.toString(this.getCharsetName())), e);
                        }
                    }
                }

                if (this.terminate || !this.isConnected()) {
                    this.stopMonitor();
                }

                if (this.terminate || channel.isClosed() || !this.isConnected()) {
                    if (!this.terminate && stdout.available() > 0) {
                        continue;
                    }

                    ByteBuffer errorLog = new ByteBuffer();
                    if (!this.terminate) {
                        try {
                            errorLog.append(stderr);
                            errorLog.write(this.stderrLog.getOutputStream());

                            if (outSideStderr != null) {
                                errorLog.write(outSideStderr);
                                outSideStderr.flush();
                            }
                        } catch (Throwable e) {
                            if (log.isErrorEnabled()) {
                                log.error(ResourcesUtils.getSSH2JschMessage(3, errorLog.toString(this.getCharsetName())), e);
                            }
                        }
                    }

                    if (this.terminate) {
                        return "killed".equalsIgnoreCase(this.config.getProperty("terminate")) ? -898 : 0;
                    } else {
                        return channel.getExitStatus();
                    }
                }

                this.startMonitor(watch.useSeconds());
                Dates.sleep(1000);
            }
        } catch (Throwable e) {
            throw new OSCommandException(ResourcesUtils.getSSH2JschMessage(4, command), e);
        } finally {
            this.terminate = false;
            this.stopMonitor();
            if (channel != null && !channel.isClosed()) {
                channel.disconnect();
            }

            if (log.isDebugEnabled()) {
                String pid = this.config.getProperty("pid");
                log.debug(ResourcesUtils.getSSH2JschMessage(15, (pid == null ? "" : pid), this.getStdout(), this.getStderr()));
            }
        }
    }

    protected String[] parseSuCommand(String command) {
        String[] array = StringUtils.split(command, ArrayUtils.asList("&&", "||", ";"), false);
        for (String cmd : array) {
            String line = StringUtils.trimBlank(cmd);
            String[] ca = StringUtils.splitByBlank(line);
            if (ca.length > 2 && ca[0].equalsIgnoreCase("su") && ca[1].equals("-")) {
                String[] result = new String[2];
                int index = command.indexOf(line);
                Ensure.isFromZero(index);
                result[0] = command.substring(0, index + line.length());
                result[1] = StringUtils.ltrimBlank(command.substring(index + line.length()), '&', '|', ';') + "; exit $?\n";
                return result;
            }
        }
        return null;
    }

    /**
     * 启动会话超时监听器
     *
     * @param seconds 命令执行的用时时间，单位秒
     */
    protected synchronized void startMonitor(long seconds) {
        if (this.timer.isStart() || this.terminate) {
            return;
        }

        String pid = this.getPid();
        if (seconds > SecureShellCommandMonitor.START_MONITOR && StringUtils.isNotBlank(pid)) {
            this.monitor.startMonitor(this);
            this.timer.start();
            this.timer.addTask(this.monitor);
        }
    }

    /**
     * 停止监听器
     */
    protected synchronized void stopMonitor() {
        try {
            if (this.timer.isStart()) {
                this.timer.stop(true);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getSSH2JschMessage(10), e);
            }
        }
    }

    /**
     * 从字节数组中截取 [ system shell pid is XX ] 内容
     *
     * @param array  字节数组
     * @param length 字节数组的长度
     * @return
     */
    protected int extractPid(byte[] array, int length) {
        if (StringUtils.isNotBlank(this.config.getProperty("pid"))) {
            return length;
        }

        String logstr = new String(array, 0, length);
        String flagStr = "[ system shell pid is ";

        int begin = 0, end = 0;
        if ((begin = logstr.indexOf(flagStr)) != -1 && (end = StringUtils.indexOfBlank(logstr, begin + flagStr.length(), -1)) != -1) {
            int searchPos = begin + flagStr.length();
            String pid = logstr.substring(searchPos, end);
            if (StringUtils.isNotBlank(pid)) {
                this.setPid(pid);
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getSSH2JschMessage(8, this.connectInfo, pid));
                }

                int endPos = logstr.indexOf("]", searchPos);
                if (endPos == -1) {
                    throw new IllegalArgumentException(logstr);
                }

                int nextPos = endPos + 1;
                if (nextPos >= logstr.length()) {
                    return 0;
                } else {
                    byte[] byteArray = StringUtils.toBytes(logstr.substring(nextPos), this.getCharsetName());
                    System.arraycopy(byteArray, 0, array, 0, byteArray.length);
                    return byteArray.length;
                }
            }
        }

        return length;
    }

    /**
     * 在命令前面添加加载环境文件与输出进程编号等语句
     *
     * @param command
     * @return
     */
    public String toShellCommand(String command) {
        StringBuilder buf = new StringBuilder(command.length() + 50);
        if (this.config.contains(OSShellCommand.profiles)) {
            buf.append(this.config.getProperty(OSShellCommand.profiles));
        }

        String str = StringUtils.trimBlank(command);
        while (str.startsWith("&&") || str.startsWith("||")) {
            str = StringUtils.ltrimBlank(str.substring(2), ';');
        }

        while (str.endsWith("&&") || str.endsWith("||")) {
            str = StringUtils.rtrimBlank(str.substring(0, str.length() - 2), ';');
        }

        buf.append("echo [ system shell pid is $$ ]; ( ").append(str).append(" )");
        return buf.toString();
    }

    public boolean supportStderr() {
        return true;
    }

    public boolean supportStdout() {
        return true;
    }

    public void setStderr(OutputStream output) {
        this.stderr = output;
    }

    public void setStdout(OutputStream output) {
        this.stdout = output;
    }

    public String getStdout() {
        return this.getStdout(this.getCharsetName());
    }

    public String getStderr() {
        return this.stderrLog.toString(this.getCharsetName());
    }

    public String getStdout(String charset) {
        String log = StringUtils.ltrimBlank(this.stdoutLog.toString(charset));
        if (log.startsWith(this.shellPrompt)) {
            return StringUtils.ltrimBlank(log.substring(this.shellPrompt.length()));
        } else {
            return StringUtils.ltrimBlank(log);
        }
    }

    public String getStderr(String charset) {
        return this.stderrLog.toString(charset);
    }

    /**
     * 加载用户配置文件，读取默认字符集，读取当前路径
     *
     * @throws OSCommandException
     * @throws IOException
     */
    protected synchronized void init() throws OSCommandException, IOException {
        // 打印shell命令提示符
        this.execute("echo ''");
        String str = this.stdoutLog.toString(CharsetName.ISO_8859_1);
        this.shellPrompt = (str == null) ? "" : StringUtils.trimBlank(str);

        if (log.isDebugEnabled()) {
            log.debug("SSH Command Stdout prompt: " + this.shellPrompt);
        }

        // 打印shell环境配置文件与字符集
        String[] commands = new String[]{ //
                "loadEnv", ". /etc/profile; . .profile; . .bash_profile; . .bash_login; . .bashrc;", // 加载环境变量配置
                "echoEnv", "ls /etc/profile; ls -a | grep 'profile\\|bash_login\\|bashrc'", //
                "echoPwd", "pwd", // 获取当前目录
                "echoLang", "echo $LANG" // 获取字符集设置
        };

        OSCommandStdouts map = this.execute(commands);
        String pwd = OSCommandUtils.join(map.get("echoPwd"));

        // 打印环境文件
        List<String> profiles = ArrayUtils.asList("/etc/profile", ".profile", ".bash_profile", ".bash_login", ".bashrc");
        List<String> list = StringUtils.trimBlank(map.get("echoEnv"));
        if (list == null) {
            list = new ArrayList<String>();
        }
        List<String> profileList = new ArrayList<String>(profiles);
        profileList.removeAll(list);
        profiles.removeAll(profileList);

        this.envfiles.clear();
        String dir = FileUtils.rtrimFolderSeparator(pwd);
        StringBuilder buf = new StringBuilder();
        for (String profile : profiles) {
            if (StringUtils.isNotBlank(profile)) {
                buf.append(". ");

                String profilepath = "";
                if (!profile.startsWith("/")) {
                    profilepath += dir + "/";
                }
                profilepath += profile;

                buf.append(profilepath);
                buf.append("; ");

                this.envfiles.add(profilepath);
            }
        }
        this.config.setProperty(OSShellCommand.profiles, buf.toString());

        if (log.isDebugEnabled()) {
            log.debug("SSH Command profiles: " + buf);
        }

        // 设置字符集
        if (StringUtils.isBlank(this.config.getProperty(SecureShellCommand.charset))) {
            String lang = OSCommandUtils.join(map.get("echoLang"));
            String[] array = StringUtils.split(lang, '.');
            switch (array.length) {
                case 1:
                    if (StringUtils.lookupCharset(array[0]) != null) {
                        this.setCharsetName(array[0]);
                    }
                    break;

                case 2:
                    if (StringUtils.lookupCharset(array[0]) != null) {
                        this.setCharsetName(array[0]);
                    } else if (StringUtils.lookupCharset(array[1]) != null) {
                        this.setCharsetName(array[1]);
                    }
                    break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SSH Command charset: " + this.getCharsetName());
        }
    }

    public synchronized OSFileCommand getFileCommand() {
        if (!this.ftp.isChannelConnected()) {
            this.ftp.openChannelSftp();
        }
        return this.ftp;
    }

    public int localPortForward(int localPort, String remoteHost, int remotePort) {
        String host = this.config.getProperty(OSConnectCommand.host);
        String port = this.config.getProperty(OSConnectCommand.port);
        String username = this.config.getProperty(OSConnectCommand.username);
        String password = this.config.getProperty(OSConnectCommand.password);

        SecureShellForwardCommand server = new SecureShellForwardCommand();
        server.setCharsetName(this.getCharsetName());
        server.setStdout(this.stdout);
        server.setStderr(this.stderr);

        Ensure.isTrue(server.connect(host, Integer.parseInt(port), username, password));
        this.forwards.add(server);
        return server.localPortForward(localPort, remoteHost, remotePort);
    }

    public boolean isConnected() {
        try {
            return this.session != null && this.session.isConnected();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("isConnected() error!", e);
            }
            return false;
        }
    }

    public Object getAttribute(String key) {
        if ("profile".equalsIgnoreCase(key)) {
            return this.envfiles;
        } else {
            return this.config.get(key);
        }
    }

    /**
     * 用户登陆验证信息
     */
    static class DefaultUserInfo implements UserInfo {
        private final Log log;
        private String username;
        private String password;

        public DefaultUserInfo(Log log) {
            this.log = log;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void showMessage(String message) {
            log.warn("*showMessage: " + message);
        }

        public boolean promptYesNo(String message) {
            log.warn("*promptYesNo: " + message);
            return false;
        }

        public String getPassphrase() {
            return this.username;
        }

        public String getPassword() {
            return password;
        }

        public boolean promptPassphrase(String message) {
            log.warn("*promptPassphrase: " + message);
            return false;
        }

        public boolean promptPassword(String message) {
            log.warn("*promptPassword: " + message);
            return false;
        }
    }

    /**
     * 按集合中的先后顺序执行命令（忽略命令执行时发生错误继续向下执行），并返回每个命令的标准输出信息
     *
     * @param commands 命令数组
     * @return 返回命令对应的标准输出信息集合
     * @throws OSCommandException
     */
    public OSCommandStdouts execute(String... commands) throws OSCommandException {
        List<String> list = ArrayUtils.asList(commands);
        return this.execute(list);
    }

    /**
     * 按集合中的先后顺序执行命令（忽略命令执行时发生错误继续向下执行），并返回每个命令的标准输出信息
     *
     * @param commands 命令集合
     * @return 返回命令对应的标准输出信息集合
     * @throws OSCommandException
     */
    public OSCommandStdouts execute(List<String> commands) throws OSCommandException {
        OSCommandStdoutsImpl map = new OSCommandStdoutsImpl();
        if (commands == null || commands.isEmpty()) {
            return map;
        }

        this.execute(OSCommandUtils.toMultiCommand(commands)); // 执行合并命令
        String allStdout = this.getStdout();
        if (StringUtils.isBlank(allStdout)) { // 执行合并命令成功
            for (int i = 0; i < commands.size(); i++) {
                String key = commands.get(i); // 命令编号
                String command = commands.get(++i); // 命令语句

                this.execute(command); // 执行命令
                String stdout = this.getStdout(); // 标准输出

                if (log.isTraceEnabled()) {
                    log.trace(stdout);
                }

                BufferedLineReader in = new BufferedLineReader(stdout);
                try {
                    ArrayList<String> list = new ArrayList<String>(); // 按行读取标准输出信息
                    while (in.hasNext()) {
                        list.add(in.next());
                    }
                    map.put(key, list);
                } finally {
                    IO.close(in);
                }
            }
            return map;
        } else { // 如果执行合并命令报错则执行分布命令
            if (log.isTraceEnabled()) {
                log.trace(allStdout);
            }
            return OSCommandUtils.splitMultiCommandStdout(allStdout);
        }
    }

}
