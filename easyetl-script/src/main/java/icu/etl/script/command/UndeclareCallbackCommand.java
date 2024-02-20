package icu.etl.script.command;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.CallbackMap;
import icu.etl.util.StringUtils;

/**
 * 删除命令对应的回调函数 <br>
 * undeclare global command callback for exit | quit | echo | step[;]
 */
public class UndeclareCallbackCommand extends AbstractGlobalCommand implements LoopCommandSupported {

    /** 回调函数对应的命令表达式 */
    private Class<? extends UniversalCommandCompiler> cls;

    public UndeclareCallbackCommand(UniversalCommandCompiler compiler, String command, Class<? extends UniversalCommandCompiler> cls, boolean global) {
        super(compiler, command);
        this.cls = cls;
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        boolean print = session.isEchoEnable() || forceStdout;
        if (print) {
            stdout.println(StringUtils.escapeLineSeparator(this.command));
        }

        CallbackMap.get(context, this.isGlobal()).remove(this.cls);
        return 0;
    }

    public void terminate() throws Exception {
    }

    public boolean enableLoop() {
        return false;
    }

}