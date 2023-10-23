package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.os.OSSecureShellCommand;
import icu.etl.printer.OutputStreamPrinter;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.io.ScriptNullStdout;
import icu.etl.util.ResourcesUtils;

/**
 * 建立 SSH 客户端 <br>
 * {@literal ssh user@127.0.0.1:22?password=user && ./test.sh && ./run.sh}
 */
public class SSH2Command extends AbstractTraceCommand implements JumpCommandSupported, NohupCommandSupported {

    /** 登录信息 */
    private String oscommand;
    private String host;
    private String port;
    private String username;
    private String password;
    private OSSecureShellCommand client;

    public SSH2Command(UniversalCommandCompiler compiler, String command, String host, String port, String username, String password, String oscommand) {
        super(compiler, command);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.oscommand = oscommand;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String host = analysis.replaceShellVariable(session, context, this.host, false, true, true, false);
        String port = analysis.replaceShellVariable(session, context, this.port, false, true, true, false);
        String username = analysis.replaceShellVariable(session, context, this.username, false, true, true, false);
        String password = analysis.replaceShellVariable(session, context, this.password, false, true, true, false);
        String oscommand = analysis.replaceShellVariable(session, context, this.oscommand, true, true, false, true);

        boolean print = session.isEchoEnable() || forceStdout;
        if (print) {
            stdout.println("ssh " + username + "@" + host + ":" + port + "?password=" + password);
        }

        if (analysis.isBlankline(oscommand)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(98, this.command));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 登陆服务器执行命令
        this.client = context.getFactory().getContext().get(OSSecureShellCommand.class);
        try {
            if (!this.client.connect(host, Integer.parseInt(port), username, password)) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(38, "ssh2"));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            this.client.setStdout(new OutputStreamPrinter(print ? stdout : new ScriptNullStdout(stdout), this.client.getCharsetName()));
            this.client.setStderr(new OutputStreamPrinter(stderr, this.client.getCharsetName()));

            if (print) {
                stdout.println(oscommand);
            }

            int exitcode = this.client.execute(oscommand, 0);
            if (exitcode == 0) {
                return 0;
            } else {
                String errout = this.client.getStderr();
                if (!analysis.isBlankline(errout)) {
                    stderr.println(errout);
                }
                stderr.println(ResourcesUtils.getScriptStderrMessage(20, oscommand, exitcode, host));
                return exitcode;
            }
        } finally {
            this.client.close();
        }
    }

    public void terminate() throws IOException, SQLException {
        if (this.client != null) {
            this.client.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
