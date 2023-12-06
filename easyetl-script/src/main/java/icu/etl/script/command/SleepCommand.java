package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.expression.MillisExpression;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 休眠
 */
public class SleepCommand extends AbstractTraceCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 休眠时间，格式: 10min */
    private String time;

    public SleepCommand(UniversalCommandCompiler compiler, String command, String time) {
        super(compiler, command);
        this.time = time;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.time)) {
            this.time = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "sleep", this.time));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        long compileMillis = session.getCompiler().getCompileMillis();
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String time = analysis.replaceShellVariable(session, context, this.time, true, true, true, false);
        long millis = new MillisExpression(time).value();

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("sleep " + millis + " millisecond");
        }

        // 计算休眠时间
        long sleep = millis - (System.currentTimeMillis() - compileMillis);
        if (sleep <= 0) {
            return 0;
        }

        try {
            Thread.sleep(millis);
            return 0;
        } catch (Throwable e) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(42), e);

            sleep = millis - (System.currentTimeMillis() - compileMillis);
            if (sleep > 0) {
                Dates.sleep(sleep);
            }
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
        try {
            Thread.interrupted();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean enableNohup() {
        return true;
    }

}
