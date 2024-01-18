package icu.etl.script.command;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.io.ScriptFile;
import icu.etl.script.io.ScriptStdbuf;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;

/**
 * {@literal while read line do ... done < filename}
 */
public class ReadCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** while 循环体 */
    private CommandList body;

    /** 文件路径或命令表达式 */
    private String inputStr;

    /** 正在运行的脚本命令 */
    protected UniversalScriptCommand command;

    public ReadCommand(UniversalCommandCompiler compiler, String command, CommandList body, String inputExpr) {
        super(compiler, command);
        this.body = body;
        this.body.setOwner(this);
        this.inputStr = inputExpr;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        File file = new ScriptFile(session, context, this.inputStr);
        UniversalScriptAnalysis analysis = session.getAnalysis();

        BufferedReader in = null;
        if (!file.exists()) {
            String command;
            if (session.getAnalysis().startsWith(this.inputStr, "`", 0, false)) {
                int end = analysis.indexOfAccent(this.inputStr, 0);
                if (end == -1) {
                    throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr108", this.inputStr));
                }
                if (end != this.inputStr.length() - 1) {
                    throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr066", this.inputStr));
                }
                command = this.inputStr.substring(1, end);
            } else {
                command = this.inputStr;
            }

            ScriptStdbuf cache = new ScriptStdbuf(stdout);
            int exitcode = context.getEngine().eval(session, context, cache, stderr, command);
            if (exitcode != 0) {
                return exitcode;
            }

            String message = cache.rtrimBlank();
            in = IO.getBufferedReader(new CharArrayReader(message.toCharArray()));
        } else {
            in = IO.getBufferedReader(file, context.getCharsetName());
        }

        try {
            return this.execute(session, context, stdout, stderr, in, forceStdout, this.body);
        } finally {
            in.close();
        }
    }

    /**
     * 执行循环读取命令 <br>
     * while read line do ... done
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标注信息输出接口
     * @param stderr      错误信息输出接口
     * @param in          脚本语句输入流
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @return
     * @throws Exception
     */
    protected int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, BufferedReader in, boolean forceStdout, CommandList body) throws Exception {
        try {
            ScriptMainProcess process = session.getMainProcess();
            boolean isbreak = false, iscontinue = false;
            String line = null;
            while (!session.isTerminate() && (line = in.readLine()) != null) {
                iscontinue = false;
                context.addLocalVariable(body.getName(), line);

                for (int i = 0; !session.isTerminate() && i < body.size(); i++) {
                    UniversalScriptCommand command = body.get(i);
                    this.command = command;
                    if (command == null) {
                        continue;
                    }

                    UniversalCommandResultSet result = process.execute(session, context, stdout, stderr, forceStdout, command);
                    int value = result.getExitcode();
                    if (value != 0) {
                        return value;
                    }

                    if (command instanceof LoopCommandKind) {
                        LoopCommandKind cmd = (LoopCommandKind) command;
                        int type = cmd.kind();
                        if (type == BreakCommand.KIND) { // break
                            isbreak = true;
                            break;
                        } else if (type == ContinueCommand.KIND) { // continue
                            iscontinue = true;
                            break;
                        } else if (type == ExitCommand.KIND) { // Exit script
                            return value;
                        } else if (type == ReturnCommand.KIND) { // Exit the result set loop
                            return value;
                        }
                    }
                }

                if (isbreak) {
                    break;
                }

                if (iscontinue) {
                    continue;
                }
            }

            if (session.isTerminate()) {
                return UniversalScriptCommand.TERMINATE;
            } else {
                return 0;
            }
        } finally {
            this.command = null;
        }
    }

    public void terminate() throws Exception {
        if (this.command != null) {
            this.command.terminate();
        }
    }

}
