package icu.etl.script.command;

import java.io.File;
import java.util.List;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.internal.FunctionSet;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.util.CollectionUtils;
import icu.etl.util.ResourcesUtils;

public class ExecuteFunctionCommand extends AbstractTraceCommand implements NohupCommandSupported {

    /** 自定义方法的输入参数 */
    private String parameters;

    /** 正在运行的脚本命令 */
    protected UniversalScriptCommand command;

    public ExecuteFunctionCommand(UniversalCommandCompiler compiler, String command, String parameters) {
        super(compiler, command);
        this.parameters = parameters;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String str = analysis.trim(analysis.replaceShellVariable(session, context, this.parameters, false, true, true, false), 0, 1);
        List<String> list = analysis.split(str);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, analysis.unQuotation(list.get(i)));
        }
        String[] args = str == null ? null : CollectionUtils.toArray(list);
        String name = args[0];
        boolean reverse = name.startsWith("!");

        String functionName = reverse ? name.substring(1) : name;
        CommandList body = FunctionSet.get(context, false).get(functionName); // 优先从局部域中查询自定义方法
        if (body == null) { //
            body = FunctionSet.get(context, true).get(functionName); // 从全局域中查找自定义方法
        }
        int exitcode = this.execute(session, context, stdout, stderr, forceStdout, body, args);

        if (reverse) {
            return exitcode == 0 ? UniversalScriptCommand.COMMAND_ERROR : 0;
        } else {
            return exitcode;
        }
    }

    /**
     * 执行用户自定义方法
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标注信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param body        自定义方法内容
     * @param args        自定义方法的参数, 第一个值是方法名, 从第二个值开始是方法参数
     * @return
     * @throws Exception
     */
    protected int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, CommandList body, String[] args) throws Exception {
        try {
            if (session.isTerminate()) {
                return UniversalScriptCommand.TERMINATE;
            }

            ScriptMainProcess process = session.getMainProcess();
            session.setFunctionParameter(args);
            for (int i = 0; !session.isTerminate() && i < body.size(); i++) {
                UniversalScriptCommand command = body.get(i);
                this.command = command;
                if (command == null) {
                    continue;
                }

                UniversalCommandResultSet result = process.execute(session, context, stdout, stderr, forceStdout, command);
                int exitcode = result.getExitcode();
                if (exitcode != 0) {
                    return exitcode;
                }

                if (command instanceof LoopCommandKind) {
                    LoopCommandKind cmd = (LoopCommandKind) command;
                    int type = cmd.kind();
                    if (type == ExitCommand.KIND) { // Exit script
                        return exitcode;
                    } else if (type == ReturnCommand.KIND) { // Exit method
                        return exitcode;
                    } else if (type == BreakCommand.KIND) { // break
                        throw new UnsupportedOperationException(ResourcesUtils.getMessage("script.message.stderr031"));
                    } else if (type == ContinueCommand.KIND) { // continue
                        throw new UnsupportedOperationException(ResourcesUtils.getMessage("script.message.stderr032"));
                    }
                }
            }

            if (session.isTerminate()) {
                return UniversalScriptCommand.TERMINATE;
            } else {
                return 0;
            }
        } finally {
            this.command = null;
            session.removeFunctionParameter();
        }
    }

    public void terminate() throws Exception {
        if (this.command != null) {
            this.command.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

}
