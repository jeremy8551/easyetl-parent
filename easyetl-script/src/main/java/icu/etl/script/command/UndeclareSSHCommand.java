package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.SSHClientMap;
import icu.etl.util.ResourcesUtils;

/**
 * 关闭 SSH 客户端或端口转发协议 <br>
 * undeclare name ssh [ client | tunnel ];
 */
public class UndeclareSSHCommand extends AbstractTraceCommand {

    /** SSH客户端名 或 端口转发协议名 */
    private String name;

    /** client 或 tunnel */
    private String type;

    public UndeclareSSHCommand(UniversalCommandCompiler compiler, String command, String name, String type) {
        super(compiler, command);
        this.name = name;
        this.type = type;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        boolean print = session.isEchoEnable() || forceStdout;
        if ("client".equalsIgnoreCase(this.type)) {
            if (print) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout044", this.name));
            }

            SSHClientMap.get(context).close(this.name);
            return 0;
        } else if ("tunnel".equalsIgnoreCase(this.type)) {
            if (print) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout045", this.name));
            }

            SSHClientMap.get(context).close(this.name);
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr131", this.command, this.type, "client, tunnel"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

}