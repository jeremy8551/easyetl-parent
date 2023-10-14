package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;

/**
 * 命令替换功能
 *
 * @author jeremy8551@qq.com
 */
public class SubCommand extends AbstractCommand implements NohupCommandSupported {

    /** 命令替换语句 */
    private String script;

    public SubCommand(UniversalCommandCompiler compiler, String command, String script) {
        super(compiler, command);
        this.script = script;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(this.command);
        }

        return context.getEngine().eval(session, context, stdout, stderr, this.script);
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
