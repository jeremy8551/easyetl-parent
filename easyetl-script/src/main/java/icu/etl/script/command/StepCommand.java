package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptListener;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptSteper;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.CallbackCommandSupported;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.session.ScriptStep;
import icu.etl.util.ResourcesUtils;

/**
 * 建立步骤信息
 */
public class StepCommand extends AbstractTraceCommand implements CallbackCommandSupported, LoopCommandSupported {

    /** 步骤名 */
    private String message;

    public StepCommand(UniversalCommandCompiler compiler, String command, String message) {
        super(compiler, command);
        this.message = message;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String message = analysis.replaceVariable(session, context, this.message, true);
        boolean print = session.isEchoEnable() || forceStdout;
        ScriptStep obj = ScriptStep.get(context, true);

        // step 命令中不能包含 || 符号
        if (message.indexOf('\'') != -1 || message.indexOf('|') != -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(45, message));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 打印上一个步骤信息
        else if (analysis.isBlankline(message)) {
            if (print) {
                stdout.println(obj.getStep());
            }
            return 0;
        }

        // 判断 step 命令是否重复
        else if (obj.containsStep(message)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(26, message));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 表示正在执行 jump 命令
        else if (obj.containsTarget()) {
            session.addVariable(UniversalScriptVariable.SESSION_VARNAME_STEP, message);
            if (message.equals(obj.getTarget())) { // 找到了 jump 命令对应的 step 命令
                UniversalScriptListener set = context.getCommandListeners();
                set.remove(JumpCommand.JumpListener.class);
                obj.removeTarget();
                context.addGlobalVariable(UniversalScriptVariable.SESSION_VARNAME_JUMP, "false"); // 移除标示变量
                obj.addStep(message);
                if (print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(32, message));
                }
                return this.execute(session, context, stdout, stderr, forceStdout, context.getSteper(), message);
            } else { // 未找到对应的 step 命令
                if (print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(33, message));
                }
                return 0;
            }
        }

        // 成功添加一个步骤信息
        else {
            session.addVariable(UniversalScriptVariable.SESSION_VARNAME_STEP, message);
            obj.addStep(message);
            return this.execute(session, context, stdout, stderr, forceStdout, context.getSteper(), message);
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    /**
     * 发送步骤信息
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param steper      步骤信息输出接口
     * @param message     步骤信息
     * @throws SQLException
     * @throws IOException
     */
    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptSteper steper, String message) throws IOException, SQLException {
        if (steper.getWriter() == null) {
            if (session.isEchoEnable() || forceStdout) {
                stdout.println(message);
            }
        } else {
            steper.println(message);
        }
        return 0;
    }

    public boolean enableLoop() {
        return false;
    }

    public String[] getArguments() {
        return new String[]{"step", this.message};
    }

}
