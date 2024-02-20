package icu.etl.script.command;

import java.util.Date;

import icu.etl.crypto.DESEncrypt;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.printer.OutputStreamPrinter;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.SSHClientMap;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立 SSH 协议的客户端 <br>
 * declare name SSH client for connect to name@host:port?password=str <br>
 */
public class DeclareSSHClientCommand extends AbstractCommand {

    /** 客户端名 */
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;

    public DeclareSSHClientCommand(UniversalCommandCompiler compiler, String command, String name, String host, String port, String username, String password) {
        super(compiler, command);
        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        OSSecureShellCommand client = context.getContainer().getBean(OSSecureShellCommand.class);
        if (client.connect(this.host, Integer.parseInt(this.port), this.username, this.password)) {
            if (session.isEchoEnable() || forceStdout) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout040", this.name, this.username + "@" + this.host + ":" + this.port + "?password=" + DESEncrypt.encrypt(this.password, context.getCharsetName(), StringUtils.toBytes(Dates.format08(new Date()), context.getCharsetName()))));
            }

            client.setStdout(new OutputStreamPrinter(stdout, client.getCharsetName()));
            client.setStderr(new OutputStreamPrinter(stderr, client.getCharsetName()));

            String name = analysis.replaceShellVariable(session, context, this.name, true, true, true, false);
            SSHClientMap.get(context).add(name, client);
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr038", "ssh2"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

}
