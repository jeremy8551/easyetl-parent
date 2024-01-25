package icu.etl.os.linux;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import icu.etl.collection.ByteBuffer;
import icu.etl.io.BufferedLineReader;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSCommand;
import icu.etl.os.OSCommandException;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSShellCommand;
import icu.etl.os.internal.OSCommandStdoutsImpl;
import icu.etl.os.internal.OSCommandUtils;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 执行 linux 命令
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-03-15
 */
public class LinuxCommand implements OSCommand {
    private final static Log log = LogFactory.getLog(LinuxCommand.class);

    protected ByteBuffer stdout;
    protected ByteBuffer stderr;
    private Properties config;
    protected OutputStream stdoutOS;
    protected OutputStream stderrOS;
    protected String charsetName;
    private volatile boolean terminate;

    /**
     * 初始化
     */
    public LinuxCommand() {
        this.stdout = new ByteBuffer();
        this.stderr = new ByteBuffer();
        this.config = new Properties();
    }

    /**
     * 按集合中的先后顺序执行命令（忽略命令执行时发生错误继续向下执行），并返回每个命令的标准输出信息
     *
     * @param commands 命令数组
     * @return 返回命令对应的标准输出信息集合
     * @throws OSCommandException 运行命令发生错误
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
     * @throws OSCommandException 运行命令发生错误
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

                if (log.isDebugEnabled()) {
                    log.debug(stdout);
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
            if (log.isDebugEnabled()) {
                log.debug(allStdout);
            }
            return OSCommandUtils.splitMultiCommandStdout(allStdout);
        }
    }

    public synchronized int execute(String command) {
        return this.execute(command, 0, this.stdoutOS, this.stderrOS);
    }

    public synchronized int execute(String command, long timeout) throws OSCommandException {
        return this.execute(command, timeout, this.stdoutOS, this.stderrOS);
    }

    public synchronized int execute(String command, long timeout, OutputStream stdout, OutputStream stderr) throws OSCommandException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("os.standard.output.msg003", command, timeout));
        }

        this.terminate = false;
        this.config.remove("pid");
        this.config.remove("terminate");
        this.stdout.clear();
        this.stderr.clear();

        long timeoutSec = (timeout / 1000);
        TimeWatch watch = new TimeWatch();
        Process process = null;
        try {
            String cmd = this.toShellCommand(command);
            process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});

            // 从进程的标准输出流中读取信息
            byte[] array = new byte[1024];
            InputStream in = process.getInputStream();
            for (int len = in.read(array, 0, array.length); len != -1; len = in.read(array, 0, array.length)) {
                if (timeout > 0 && watch.useSeconds() > timeoutSec) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("os.standard.output.msg007", cmd));
                    }
                    break;
                }

                int size = this.extractPid(array, len);
                this.stdout.append(array, 0, size);

                if (log.isDebugEnabled()) {
                    log.debug(new String(array, 0, size, this.getCharsetName()));
                }

                if (stdout != null) {
                    stdout.write(array, 0, size);
                    stdout.flush();
                }
            }

            InputStream is = process.getErrorStream();
            for (int len = is.read(array, 0, array.length); len != -1; len = is.read(array, 0, array.length)) {
                if (timeout > 0 && watch.useSeconds() > timeoutSec) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("os.standard.output.msg008", cmd));
                    }
                    break;
                }

                this.stderr.append(array, 0, len);

                if (log.isDebugEnabled()) {
                    log.debug(new String(array, 0, len, this.getCharsetName()));
                }

                if (stderr != null) {
                    stderr.write(array, 0, len);
                    stderr.flush();
                }
            }

            if (timeout > 0) {
                int count = 0;
                while (process.waitFor() != 0) {
                    if (++count <= 100 && log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("os.standard.output.msg006", cmd));
                    }
                }
                // while (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                // if (++count <= 100 && log.isDebugEnabled()) {
                // log.debug(ResourcesUtils.getMessage("os.standard.output.msg006", cmd));
                // }
                // }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("os.standard.output.msg006", cmd));
                }
                process.waitFor();
            }

            return process.exitValue();
        } catch (Throwable e) {
            throw new OSCommandException(ResourcesUtils.getMessage("os.standard.output.msg004", command), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("os.standard.output.msg009", command, this.getStdout(), this.getStderr()));
            }
        }
    }

    public void terminate() {
        if (this.terminate) {
            return;
        } else {
            this.terminate = true;
        }

        String pid = this.getPid();
        if (log.isDebugEnabled()) {
            log.debug("kill porcess and pid is " + pid);
        }

        if (StringUtils.isBlank(pid)) {
            return;
        }

        String command = "kill -9 " + pid;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            process.waitFor();
            process.exitValue();
            return;
        } catch (Throwable e) {
            throw new OSCommandException(ResourcesUtils.getMessage("os.standard.output.msg004", command), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("os.standard.output.msg005", command));
            }
        }
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * 从字符数组中截取当前命令执行的进程编号
     *
     * @param array  字节数组
     * @param length 数组长度
     * @return 进程编号的长度
     */
    protected int extractPid(byte[] array, int length) {
        if (StringUtils.isNotBlank(this.config.getProperty("pid"))) {
            return length;
        }

        String logstr = new String(array, 0, length); // 日志输出信息
        int begin = 0, end = 0;
        String flagStr = "[ system shell pid is ";
        if (logstr != null //
                && (begin = logstr.indexOf(flagStr)) != -1 //
                && (end = logstr.indexOf("]", begin + flagStr.length())) != -1 // 进程编号的结束位置
        ) {
            int start = begin + flagStr.length(); // 进程编号的起始位置
            String pid = StringUtils.trimBlank(logstr.substring(start, end)); // 截取进程编号
            if (StringUtils.isNotBlank(pid)) {
                this.setPid(pid);
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg008", "localhost", pid));
                }
                int next = end + 1; // 中括号的结束位置的下一个位置
                if (next >= logstr.length()) {
                    return 0;
                } else {
                    byte[] byteArray = StringUtils.toBytes(logstr.substring(next), this.getCharsetName());
                    System.arraycopy(byteArray, 0, array, 0, byteArray.length);
                    return byteArray.length;
                }
            }
        }

        return length;
    }

    /**
     * 保存命令的进程编号
     *
     * @param pid 进程编号
     */
    protected void setPid(String pid) {
        this.config.setProperty("pid", pid);
    }

    /**
     * 返回命令的进程编号
     *
     * @return 进程编号
     */
    public String getPid() {
        return this.config.getProperty("pid");
    }

    /**
     * 在shell 命令中添加前缀和后缀信息
     *
     * @param command 命令
     * @return 命令
     */
    private String toShellCommand(String command) {
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

    /**
     * 加载用户配置文件，当前所在目录，默认的字符集编码
     *
     * @throws OSCommandException 运行命令发生错误
     */
    protected synchronized void prepared() throws OSCommandException {
        String[] command = new String[]{"source profiles", ". /etc/profile; . .profile; . .bash_profile; . .bash_login; . .bashrc;", //
                "echo ls", "ls /etc/profile; ls -a | grep profile; ls -a | grep bash_login; ls -a | grep bashrc;", //
                "echo pwd", "pwd", //
                "echo lang", "echo $LANG" //
        };

        OSCommandStdouts map = this.execute(command);
        String pwd = FileUtils.rtrimFolderSeparator(StringUtils.removeBlank(StringUtils.join(map.get("echo pwd"), "")));

        List<String> profiles = ArrayUtils.asList("/etc/profile", ".profile", ".bash_profile", ".bash_login", ".bashrc");
        List<String> list = StringUtils.trimBlank(map.get("echo ls"));
        List<String> profileList = new ArrayList<String>(profiles);
        profileList.removeAll(list);
        profiles.removeAll(profileList);

        StringBuilder buf = new StringBuilder();
        for (String profile : profiles) {
            if (StringUtils.isNotBlank(profile)) {
                buf.append(". ");
                if (!profile.startsWith("/")) {
                    buf.append(pwd);
                    buf.append("/");
                }
                buf.append(profile);
                buf.append("; ");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("source profiles command is " + buf);
        }
        this.config.setProperty(OSShellCommand.profiles, buf.toString());

        String lang = StringUtils.removeBlank(StringUtils.join(map.get("echo lang"), ""));
        String[] array = StringUtils.split(lang, '.');
        switch (array.length) {
            case 1:
                if (StringUtils.lookupCharset(array[0]) != null) {
                    this.setCharsetName(array[0]);
                    return;
                }
                break;

            case 2:
                if (StringUtils.lookupCharset(array[0]) != null) {
                    this.setCharsetName(array[0]);
                    return;
                }
                if (StringUtils.lookupCharset(array[1]) != null) {
                    this.setCharsetName(array[1]);
                    return;
                }
                break;
        }
        this.setCharsetName(StringUtils.CHARSET);
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public boolean supportStdout() {
        return true;
    }

    public boolean supportStderr() {
        return true;
    }

    public void setStdout(OutputStream out) {
        this.stdoutOS = out;
    }

    public void setStderr(OutputStream out) {
        this.stderrOS = out;
    }

    public String getStdout() {
        return this.stdout.toString();
    }

    public String getStderr() {
        return this.stderr.toString();
    }

    public String getStdout(String charsetName) {
        return this.stdout.toString(charsetName);
    }

    public String getStderr(String charsetName) {
        return this.stderr.toString(charsetName);
    }

    public String getCharsetName() {
        return StringUtils.charset(this.charsetName);
    }

    public Object getAttribute(String key) {
        return this.config.get(key);
    }

}
