package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.io.ScriptNullStderr;
import icu.etl.script.io.ScriptNullStdout;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 以静默方式执行下一个命令，即使下一个命令执行报错也不会输出任何信息
 */
public class QuietCommand extends AbstractTraceCommand implements UniversalScriptInputStream, LoopCommandSupported, JumpCommandSupported, NohupCommandSupported {
    private final static Log log = LogFactory.getLog(QuietCommand.class);

    /** 子命令 */
    private UniversalScriptCommand subcommand;

    public QuietCommand(UniversalCommandCompiler compiler, String command, UniversalScriptCommand subcommand) {
        super(compiler, command);
        this.subcommand = subcommand;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException, SQLException {
        if (this.subcommand != null) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "quiet", this.subcommand.getScript()));
        }

        String script = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        this.command = "quiet " + script;
        List<UniversalScriptCommand> list = parser.read(script);
        if (list.size() == 1) {
            this.subcommand = list.get(0);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(78, script));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(session.getAnalysis().replaceVariable(session, context, this.command, false));
        }

        try {
            this.subcommand.execute(session, context, new ScriptNullStdout(stdout), new ScriptNullStderr(stderr), forceStdout);
        } catch (Throwable e) {
            if (log.isDebugEnabled() && this.subcommand != null) {
                log.debug(this.subcommand.getScript(), e);
            }
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {
        if (this.subcommand != null) {
            this.subcommand.terminate();
        }
    }

    public boolean enableNohup() {
        return this.subcommand == null ? false : (this.subcommand instanceof NohupCommandSupported) && ((NohupCommandSupported) this.subcommand).enableNohup();
    }

    public boolean enableJump() {
        return this.subcommand == null ? false : (this.subcommand instanceof JumpCommandSupported) && ((JumpCommandSupported) this.subcommand).enableJump();
    }

    public boolean enableLoop() {
        return this.subcommand == null ? false : (this.subcommand instanceof LoopCommandSupported) && ((LoopCommandSupported) this.subcommand).enableLoop();
    }

}
