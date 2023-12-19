package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ProgressMap;
import icu.etl.script.internal.ScriptProgress;
import icu.etl.util.ResourcesUtils;

/**
 * 打印进度信息
 */
public class ProgressCommand extends AbstractTraceCommand {

    /** 任务编号 */
    private String taskId;

    public ProgressCommand(UniversalCommandCompiler compiler, String command, String taskId) {
        super(compiler, command);
        this.taskId = taskId;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        ScriptProgress progress = null;
        if (this.taskId.length() == 0) { // 使用默认的进度输出组件
            progress = ProgressMap.getProgress(context);
        } else { // 查询多任务进度输出组件
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String name = analysis.replaceShellVariable(session, context, this.taskId, true, true, true, false);
            progress = ProgressMap.getProgress(context, name);
        }

        if (progress == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(73, this.command));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            progress.print(session.isEchoEnable() || forceStdout);
            return 0;
        }
    }

    public void terminate() throws Exception {
    }

}
