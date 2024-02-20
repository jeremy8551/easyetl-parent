package icu.etl.script.command;

import java.io.File;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.util.ResourcesUtils;

/**
 * 执行下一次循环
 */
public class ContinueCommand extends AbstractSlaveCommand implements LoopCommandKind {

    public final static int KIND = 30;

    public ContinueCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (this.existsOwner()) {
//			if (session.isEchoEnable() || forceStdout) {
//				stdout.println(this.command);
//			}
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr005"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public int kind() {
        return ContinueCommand.KIND;
    }
}
