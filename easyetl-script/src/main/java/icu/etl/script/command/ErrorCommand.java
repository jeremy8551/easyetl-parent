package icu.etl.script.command;

import java.io.File;
import java.io.Reader;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.CallbackCommandSupported;
import icu.etl.util.IO;

/**
 * 通过脚本引擎错误信息输出流输出信息
 *
 * @author jeremy8551@qq.com
 */
public class ErrorCommand extends AbstractTraceCommand implements UniversalScriptInputStream, CallbackCommandSupported {

    protected String message;

    public ErrorCommand(UniversalCommandCompiler compiler, String command, String message) {
        super(compiler, command);
        this.message = message;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws Exception {
        this.message = IO.read(in, new StringBuilder()).toString();
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String str = analysis.replaceShellVariable(session, context, this.message, true, true, true, false);
        stderr.println(str);
        return 0;
    }

    public void terminate() throws Exception {
    }

    public String[] getArguments() {
        return new String[]{"error", this.message};
    }

}
