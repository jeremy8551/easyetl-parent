package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 退出脚本引擎
 */
public class ExitCommand extends AbstractTraceCommand implements UniversalScriptInputStream, LoopCommandKind {

    public final static int KIND = 10;

    /** 脚本返回值 */
    private String exitcode;

    public ExitCommand(UniversalCommandCompiler compiler, String command, String exitcode) {
        super(compiler, command);
        this.exitcode = exitcode;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.exitcode)) {
            this.exitcode = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "exit", this.exitcode));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        session.getCompiler().terminate();

        if (this.exitcode.length() == 0) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr011"));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else if (StringUtils.isInt(this.exitcode)) {
            // 根节点的脚本引擎需要释放资源
            if (context.getParent() == null) {
                if (session.isEchoEnable() || forceStdout) {
                    stdout.println(ResourcesUtils.getMessage("script.message.stdout999"));
                }

                context.getEngine().close();
            } else {
                if (session.isEchoEnable() || forceStdout) {
                    stdout.println("exit " + this.exitcode);
                }
            }

            int value = Integer.parseInt(this.exitcode);
            session.removeValue();
            session.putValue("exit", value);
            return value;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr012"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public int kind() {
        return ExitCommand.KIND;
    }

}
