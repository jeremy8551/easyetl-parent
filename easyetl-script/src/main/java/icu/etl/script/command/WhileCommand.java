package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptExpression;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.session.ScriptMainProcess;

/**
 * 执行 while 循环 <br>
 * <p>
 * while .. loop ... end loop
 */
public class WhileCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** while 语句中执行代码块 */
    protected CommandList body;

    /** 正在运行的脚本命令 */
    protected UniversalScriptCommand command;

    public WhileCommand(UniversalCommandCompiler compiler, String command, CommandList body) {
        super(compiler, command);
        this.body = body;
        this.body.setOwner(this);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        try {
            ScriptMainProcess process = session.getMainProcess();
            boolean isbreak = false, iscontinue = false;
            UniversalScriptAnalysis analysis = session.getAnalysis();
            while (!session.isTerminate() && new UniversalScriptExpression(session, context, stdout, stderr, analysis.replaceShellVariable(session, context, this.body.getName(), false, false, false, true)).booleanValue()) {
                iscontinue = false;

                for (int i = 0; !session.isTerminate() && i < this.body.size(); i++) {
                    UniversalScriptCommand command = this.body.get(i);
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
                        if (type == BreakCommand.KIND) { // break
                            isbreak = true;
                            break;
                        } else if (type == ContinueCommand.KIND) { // continue
                            iscontinue = true;
                            break;
                        } else if (type == ExitCommand.KIND) { // Exit script
                            return exitcode;
                        } else if (type == ReturnCommand.KIND) { // Exit the result set loop
                            return exitcode;
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

    public void terminate() throws IOException, SQLException {
        if (this.command != null) {
            this.command.terminate();
        }
    }

}
