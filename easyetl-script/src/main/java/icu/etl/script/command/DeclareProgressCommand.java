package icu.etl.script.command;

import icu.etl.printer.Printer;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ProgressMap;
import icu.etl.script.internal.ScriptProgress;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立进度输出: <br>
 * declare global {taskId} progress use {step} print {message} total {999} times; <br>
 * <br>
 * 输出进度信息: <br>
 * progress
 */
public class DeclareProgressCommand extends AbstractGlobalCommand {

    /** 任务编号（输出多任务信息时,用以区分不同任务） */
    private String name;

    /** 输出方式: out, err, step */
    private String type;

    /** 输出进度内容 */
    private String message;

    /** 总计次数 */
    private String number;

    public DeclareProgressCommand(UniversalCommandCompiler compiler, String command, String name, String type, String message, String number, boolean global) {
        super(compiler, command);
        this.name = name;
        this.type = type;
        this.message = message;
        this.number = number;
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        boolean global = this.isGlobal();

        if (!StringUtils.isInt(this.number)) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr130", this.command, this.number));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        Printer out = null;
        if ("out".equalsIgnoreCase(this.type)) {
            out = stdout;
        } else if ("err".equalsIgnoreCase(this.type)) {
            out = stderr;
        } else if ("step".equalsIgnoreCase(this.type)) {
            out = context.getSteper();
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr131", this.command, this.type, "out, err, step"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        int total = Integer.parseInt(this.number);
        ScriptProgress progress = null;
        if (session.getAnalysis().isBlankline(this.name)) {
            progress = new ScriptProgress(out, this.message, total);
        } else {
            progress = new ScriptProgress(this.name, out, this.message, total);
        }
        ProgressMap.get(context, global).add(progress);
        return 0;
    }

    public void terminate() throws Exception {
    }

}
