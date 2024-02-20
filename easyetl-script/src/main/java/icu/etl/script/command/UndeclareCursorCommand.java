package icu.etl.script.command;

import java.io.File;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.CursorMap;
import icu.etl.util.ResourcesUtils;

/**
 * undeclare name cursor;
 *
 * @author jeremy8551@qq.com
 */
public class UndeclareCursorCommand extends AbstractTraceCommand {

    /** 游标名 */
    private String name;

    public UndeclareCursorCommand(UniversalCommandCompiler compiler, String command, String name) {
        super(compiler, command);
        this.name = name;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println("undeclare " + this.name + " cursor");
        }

        CursorMap map = CursorMap.get(context);
        if (map.remove(this.name) == null) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr002", this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            return 0;
        }
    }

    public void terminate() throws Exception {
    }

}