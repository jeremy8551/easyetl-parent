package icu.etl.script.command;

import java.util.Iterator;
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
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.session.ScriptMainProcess;

/**
 * for语句: <br>
 * <br>
 * for i in `ls` <br>
 * loop <br>
 * .. <br>
 * end loop <br>
 * <br>
 * <br>
 * for i in ${var} <br>
 * loop <br>
 * .. <br>
 * end loop <br>
 * <br>
 * <br>
 * for i in (1,2,3,4) <br>
 * loop <br>
 * .. <br>
 * end loop <br>
 *
 * @author jeremy8551@qq.com
 */
public class ForCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** 变量名 */
    protected String name;

    /** 变量集合 */
    protected String collection;

    /** for循环体 */
    protected CommandList body;

    /** 正在运行的脚本命令 */
    protected UniversalScriptCommand command;

    public ForCommand(UniversalCommandCompiler compiler, String command, String name, String collection, CommandList body) {
        super(compiler, command);
        this.name = name;
        this.collection = collection;
        this.body = body;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String variableName = analysis.replaceShellVariable(session, context, this.name, true, true, true, false);
        String collection = analysis.replaceShellVariable(session, context, this.collection, true, true, true, true);
        String str = analysis.trim(analysis.removeSide(collection, '(', ')'), 0, 0);
        List<String> list = analysis.split(str, analysis.getSegment());

        boolean exists = context.containsLocalVariable(variableName);
        Object oldValue = context.getLocalVariable(variableName);
        try {
            ScriptMainProcess process = session.getMainProcess();
            boolean isbreak = false, iscontinue = false;
            for (Iterator<String> it = list.iterator(); !session.isTerminate() && it.hasNext(); ) {
                iscontinue = false;
                String element = it.next();
                context.addLocalVariable(variableName, element);

                // 遍历所有命令
                for (int i = 0; !session.isTerminate() && i < this.body.size(); i++) {
                    UniversalScriptCommand command = this.body.get(i);
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
            if (exists) {
                context.addLocalVariable(variableName, oldValue);
            }
        }
    }

    public void terminate() throws Exception {
        if (this.command != null) {
            this.command.terminate();
        }
    }

}
