package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.internal.FunctionSet;
import icu.etl.util.ResourcesUtils;

/**
 * 建立用户自定义方法 <br>
 * function name() { .... }
 */
public class FunctionCommand extends AbstractCommand implements LoopCommandSupported, WithBodyCommandSupported {

    /** 用户自定义方法体 */
    protected CommandList body;

    public FunctionCommand(UniversalCommandCompiler compiler, String command, CommandList body) {
        super(compiler, command);
        this.body = body;
        this.body.setOwner(this);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        FunctionSet set = FunctionSet.get(context);
        String functionName = this.body.getName();
        if (this.body.size() == 0 && set.contains(functionName)) { // 删除方法-当方法体为空时
            set.remove(functionName);
        } else { // 添加一个用户自定义方法
            CommandList old = set.add(this.body);
            boolean print = session.isEchoEnable() || forceStdout;
            if (old != null && print) {
                stdout.println(ResourcesUtils.getScriptStdoutMessage(5, session.getScriptName(), old.getName()));
            }
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableLoop() {
        return false;
    }

}
