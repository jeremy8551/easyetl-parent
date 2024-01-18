package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptExpression;
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
 * 日期命令 <br>
 * date -d [日期字符串] [日期格式表达式] [ +|- 数字 day|month|year] <br>
 * date yyyyMMdd +1day, 当前日期 + 1 天 <br>
 * date yyyyMMdd + 1day <br>
 * date yyyyMMdd + 1 day <br>
 */
public class DateCommand extends AbstractTraceCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 日期运算表达式: +1day -2hour */
    private String formula;

    /** 日期表达式: 20200101 or null */
    private String dateStr;

    /** 输出日期格式: yyyyMMdd hh:mm:ss */
    private String pattern;

    public DateCommand(UniversalCommandCompiler compiler, String command, String formula, String dateStr, String pattern) {
        super(compiler, command);
        this.formula = formula;
        this.dateStr = dateStr;
        this.pattern = pattern;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.dateStr)) {
            this.dateStr = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "date", this.dateStr));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        Date date = this.dateStr != null ? Dates.parse(analysis.replaceShellVariable(session, context, this.dateStr, true, true, true, false)) : new Date();
        String formula = analysis.replaceShellVariable(session, context, this.formula, true, true, true, false);
        String pattern = analysis.replaceShellVariable(session, context, this.pattern, true, true, true, false);

        if (!analysis.isBlankline(formula)) { // 执行日期运算
            // 转为日期计算的表达式: '2020-01-01 00:00:00:000'+1day-1month
            UniversalScriptExpression expression = new UniversalScriptExpression(session, context, stdout, stderr, "'" + Dates.format21(date) + "'" + formula);
            date = expression.dateValue();
        }

        if (session.isEchoEnable() || forceStdout) {
            if (pattern != null) {
                stdout.println(Dates.format(date, pattern));
            } else {
                stdout.println(Dates.format19(date));
            }
        }

        session.removeValue();
        session.putValue("date", date);
        return 0;
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}