package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandListener;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptListener;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.session.ScriptStep;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 使脚本引擎跳转到指定 step 命令后再向下执行命令
 */
public class JumpCommand extends AbstractTraceCommand implements UniversalScriptInputStream, JumpCommandSupported, LoopCommandSupported {

    /** 跳转的目的地 */
    private String message;

    public JumpCommand(UniversalCommandCompiler compiler, String command, String message) {
        super(compiler, command);
        this.message = message;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.message)) {
            this.message = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "jump", this.message));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String target = analysis.replaceVariable(session, context, this.message, true);
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(ResourcesUtils.getScriptStdoutMessage(31, target));
        }

        // 保存目标位置信息
        ScriptStep.get(context, true).setTarget(target);
        context.addGlobalVariable(UniversalScriptVariable.SESSION_VARNAME_JUMP, "true"); // JUMP 命令标识变量

        // 添加 jump 命令的监听器
        UniversalScriptListener c = context.getCommandListeners();
        if (!c.contains(JumpListener.class)) {
            c.add(new JumpListener());
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {
        this.terminate = true;
    }

    public boolean enableJump() {
        return true;
    }

    public boolean enableLoop() {
        return false;
    }

    class JumpListener implements UniversalCommandListener {

        public JumpListener() {
            super();
        }

        public void startScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws IOException, SQLException {
        }

        public boolean beforeCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptCommand command) throws IOException, SQLException {
            boolean value = ScriptStep.get(context, true).containsTarget() && (command instanceof JumpCommandSupported) && ((JumpCommandSupported) command).enableJump();
            return !value;
        }

        public void afterCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws IOException, SQLException {
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
