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

public class BreakCommand extends AbstractSlaveCommand implements LoopCommandKind {

    public final static int KIND = 40;

    public BreakCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (this.existsOwner()) {
//			if (session.isEchoEnable() || forceStdout) {
//				stdout.println("break");
//			}
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr001"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public int kind() {
        return BreakCommand.KIND;
    }

}
