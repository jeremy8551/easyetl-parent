package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.DefaultCommandSupported;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 设置脚本引擎的默认命令 <br>
 * default sql;
 */
public class DefaultCommand extends AbstractTraceCommand implements UniversalScriptInputStream {

    /** 默认命令的语句信息 */
    private String script;

    public DefaultCommand(UniversalCommandCompiler compiler, String command, String script) {
        super(compiler, command);
        this.script = script;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.script)) {
            this.script = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "default", this.script));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptCompiler compiler = session.getCompiler();
        UniversalCommandRepository repository = compiler.getRepository();

        UniversalScriptAnalysis analysis = session.getAnalysis();
        String script = StringUtils.trimBlank(analysis.replaceShellVariable(session, context, this.script, true, true, true, false));
        boolean print = session.isEchoEnable() || forceStdout;
        if (StringUtils.isBlank(script)) { // 打印默认命令
            UniversalCommandCompiler obj = repository.getDefault();
            if (obj != null && print) {
                stdout.println(ResourcesUtils.getScriptStdoutMessage(25, obj.getClass().getName()));
            }
            return 0;
        } else { // 设置默认命令
            UniversalCommandCompiler obj = repository.get(script);
            if (!(obj instanceof DefaultCommandSupported)) {
                throw new UnsupportedOperationException(StringUtils.toString(obj));
            }

            repository.setDefault(obj);
            if (obj != null && print) {
                stdout.println(ResourcesUtils.getScriptStdoutMessage(25, obj.getClass().getName()));
            }
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

}
