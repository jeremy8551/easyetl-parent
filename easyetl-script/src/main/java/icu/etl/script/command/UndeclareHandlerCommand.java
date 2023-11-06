package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.ErrorHandlerMap;
import icu.etl.script.internal.ExitHandlerMap;
import icu.etl.util.StringUtils;

/**
 * 删除异常错误处理逻辑 <br>
 * undeclare handler for ( exception | exitcode == 0 | sqlstate == 120 | sqlcode == -803 ) ;
 */
public class UndeclareHandlerCommand extends AbstractGlobalCommand implements LoopCommandSupported {

    /** 异常处理逻辑的执行条件：exception | exitcode == 0 | sqlstate == 120 | sqlcode == -803 */
    private String condition;

    /** true表示退出处理逻辑 false表示错误处理逻辑 */
    private boolean isExitHandler;

    public UndeclareHandlerCommand(UniversalCommandCompiler compiler, String command, String condition, boolean isExitHandler, boolean global) {
        super(compiler, command);
        this.condition = condition;
        this.isExitHandler = isExitHandler;
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        boolean print = session.isEchoEnable() || forceStdout;
        if (print) {
            stdout.println(StringUtils.escapeLineSeparator(this.command));
        }

        String condition = session.getAnalysis().replaceVariable(session, context, this.condition, false);
        if (this.isExitHandler) {
            ExitHandlerMap map = ExitHandlerMap.get(context, this.isGlobal());
            map.remove(condition);
            return 0;
        } else {
            ErrorHandlerMap map = ErrorHandlerMap.get(context, this.isGlobal());
            map.remove(condition);
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableLoop() {
        return false;
    }

}