package icu.etl.os.telnet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import icu.etl.apache.net.telnet.EchoOptionHandler;
import icu.etl.apache.net.telnet.InvalidTelnetOptionException;
import icu.etl.apache.net.telnet.SuppressGAOptionHandler;
import icu.etl.apache.net.telnet.TelnetClient;
import icu.etl.apache.net.telnet.TelnetNotificationHandler;
import icu.etl.apache.net.telnet.TerminalTypeOptionHandler;
import icu.etl.annotation.EasyBean;
import icu.etl.collection.ByteBuffer;
import icu.etl.io.BufferedLineReader;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSCommandException;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSShellCommand;
import icu.etl.os.internal.OSCommandStdoutsImpl;
import icu.etl.os.internal.OSCommandUtils;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * TELNET 协议操作接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-13
 */
@EasyBean(name = "telnet", description = "apache-net")
public class TelnetCommand implements Runnable, TelnetNotificationHandler, OSShellCommand {
    private final static Log log = LogFactory.getLog(TelnetCommand.class);

    private TelnetClient client;
    private String host;
    private int port;

    /** 命令的标准输出信息 */
    private ByteBuffer stdoutLog;

    /** 命令的标准信息的输出接口 */
    private OutputStream stdout;

    /** true表示已终端当前执行的命令 */
    private volatile boolean terminate;

    public boolean connect(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.client = new TelnetClient();
        this.stdoutLog = new ByteBuffer();
        this.stdoutLog.setCharsetName(StringUtils.CHARSET);
        this.terminate = false;

        try {
            this.open(null);
            String welcome = this.connect(2000);
            log.debug(welcome);
            if (welcome.contains("Microsoft")) {
                log.debug("widows operation");
            } else {
                log.debug("linux operation");
            }
            this.execute("user", 2000);
            log.debug(this.stdoutLog.toString());
            this.execute("xxx", 4000);
            log.debug(StringUtils.replaceAll(this.stdoutLog.toString(), "[H[2J", ""));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void open(String spyfile) throws InvalidTelnetOptionException {
        TerminalTypeOptionHandler tyoopt = new TerminalTypeOptionHandler("VT220", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        this.client.addOptionHandler(tyoopt);
        this.client.addOptionHandler(echoopt);
        this.client.addOptionHandler(gaopt);

        if (null != spyfile && !"".equals(spyfile)) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(spyfile, true);
                this.client.registerSpyStream(out);
            } catch (Exception e) {
                log.error("Exception while opening the spy file: " + e.getMessage());
            }
        }
    }

    private String connect(long wait) throws IOException {
        this.client.connect(this.host, this.port);
        this.client.registerNotifHandler(this);
        Thread thread = new Thread(this);
        thread.start();
        return this.getResponse(wait);
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    public void close() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (Exception e) {
                log.error("disconnect", e);
            } finally {
                this.terminate = true;
            }
        }
    }

    public void setCharsetName(String charsetName) {
        this.stdoutLog.setCharsetName(charsetName);
    }

    public int execute(String command) {
        try {
            this.stdoutLog.clear();
            OutputStream out = this.client.getOutputStream();
            out.write(command.getBytes());
            out.write(13);
            out.write(10);
            out.flush();
            return 0;
        } catch (Exception e) {
            throw new OSCommandException(command, e);
        }
    }

    public int execute(String command, long waitTime) throws OSCommandException {
        try {
            this.stdoutLog.clear();
            OutputStream out = this.client.getOutputStream();
            out.write(command.getBytes());
            out.write(13);
            out.write(10);
            out.flush();
            this.getResponse(waitTime);
            return 0;
        } catch (Exception e) {
            throw new OSCommandException(command, e);
        }
    }

    public void receivedNegotiation(int negotiation_code, int option_code) {
        String command = null;
        if (negotiation_code == TelnetNotificationHandler.RECEIVED_DO) {
            command = "DO";
        } else if (negotiation_code == TelnetNotificationHandler.RECEIVED_DONT) {
            command = "DONT";
        } else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WILL) {
            command = "WILL";
        } else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WONT) {
            command = "WONT";
        }
        log.debug("Received " + command + " for option code " + option_code);
    }

    public void run() {
        InputStream in = this.client.getInputStream();
        try {
            byte[] buffer = new byte[1024];
            int length;
            do {
                length = in.read(buffer);
                if (length > 0) {
                    this.stdoutLog.append(buffer, 0, length);
                    if (this.stdout != null) {
                        this.stdout.write(buffer, 0, length);
                    }
                }
            } while (!this.terminate && length >= 0);
        } catch (Exception e) {
            log.error("Exception while reading socket:" + e.getMessage(), e);
        }
    }

    /**
     * 获取命令返回去
     *
     * @param waitTime 等待时间
     * @return 标准输出信息
     */
    public String getResponse(long waitTime) {
        Dates.sleep(waitTime);
        return this.stdoutLog.toString();
    }

    public int execute(String command, long timeout, OutputStream stdout, OutputStream stderr) throws OSCommandException {
        this.stdout = stdout;
        return this.execute(command, timeout);
    }

    public OSCommandStdouts execute(String... commands) throws OSCommandException {
        List<String> list = ArrayUtils.asList(commands);
        return this.execute(list);
    }

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

    public boolean supportStdout() {
        return true;
    }

    public boolean supportStderr() {
        return false;
    }

    public void setStdout(OutputStream os) {
        this.stdout = os;
    }

    public void setStderr(OutputStream out) {
    }

    public String getStdout() {
        return this.stdoutLog.toString();
    }

    public String getStderr() {
        return "";
    }

    public String getStdout(String charsetName) {
        return this.stdoutLog.toString(charsetName);
    }

    public String getStderr(String charsetName) {
        return "";
    }

    public String getCharsetName() {
        return this.stdoutLog.getCharsetName();
    }

    public Object getAttribute(String key) {
        return null;
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    public void terminate() {
        this.terminate = true;
    }

}
