package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptUsage;

/**
 * 打印脚本引擎中最近一次发生的异常信息 <br>
 * 脚本命令规则: stacktrace -s -l <br>
 * -s 选项表示打印发生异常的脚本语句 <br>
 * -l 选项表示打印发生异常的脚本语句所在行号
 *
 * @author jeremy8551@qq.com
 */
@ScriptCommand(name = "stacktrace", keywords = {"stacktrace"})
public class StacktraceCommandCompiler extends AbstractTraceCommandCompiler {

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "stacktrace -s -l", command);
        return new StacktraceCommand(this, orginalScript, expr.containsOption("-s"), expr.containsOption("-l"));
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        out.println(new ScriptUsage(this.getClass(), context.getFormatter().getClass().getName()));
    }

}
