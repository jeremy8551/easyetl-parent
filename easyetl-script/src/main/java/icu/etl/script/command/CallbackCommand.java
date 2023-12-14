package icu.etl.script.command;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandListener;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptListener;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.CallbackCommandSupported;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CallbackMap;
import icu.etl.script.internal.CommandList;
import icu.etl.util.StringUtils;

/**
 * 定义一个脚本命令的回调函数
 */
public class CallbackCommand extends AbstractGlobalCommand implements LoopCommandSupported, WithBodyCommandSupported {

    public final static String NAME = "CallbackCommand";

    /** 异常处理逻辑的执行方法 */
    private CommandList body;

    /** 回调函数对应的脚本命令信息 */
    private Class<?> cls;

    public CallbackCommand(UniversalCommandCompiler compiler, String command, Class<?> cls, CommandList body, boolean global) {
        super(compiler, command);
        this.cls = cls;
        this.body = body;
        this.body.setOwner(this);
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(StringUtils.escapeLineSeparator(this.command));
        }

        CallbackMap.get(context, this.isGlobal()).add(this.cls, this.body);
        UniversalScriptListener set = context.getCommandListeners();
        if (!set.contains(CallbackListener.class)) {
            set.add(new CallbackListener());
        }
        return 0;
    }

    public void terminate() {
    }

    public boolean enableLoop() {
        return false;
    }

    class CallbackListener implements UniversalCommandListener {

        public CallbackListener() {
        }

        public void startScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws IOException, SQLException {
        }

        public boolean beforeCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptCommand command) throws IOException, SQLException {
            return true;
        }

        public void afterCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws Exception {
            if (result.getExitcode() != 0) { // 如果上一个命令返回值是非0
                return;
            } else if (command instanceof CallbackCommandSupported) {
                CallbackCommandSupported obj = (CallbackCommandSupported) command;
                String[] args = obj.getArguments(); // 命令的参数数组
                Class<? extends UniversalCommandCompiler> cls = command.getCompiler().getClass();

                // 执行局部方法
                CallbackMap local = CallbackMap.get(context, false); // 局部
                local.executeCallback(session, context, stdout, stderr, forceStdout, cls, args);

                // 执行全局方法
                CallbackMap global = CallbackMap.get(context, true); // 全局
                global.executeCallback(session, context, stdout, stderr, forceStdout, cls, args);
            }
        }

        public boolean catchCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Throwable e) throws IOException, SQLException {
            return false;
        }

        public boolean catchScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Throwable e) throws IOException, SQLException {
            return false;
        }

        public void exitScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) {
        }
    }

}
