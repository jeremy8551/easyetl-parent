package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "terminate", keywords = {"terminate"})
public class TerminateCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "terminate -ps: {0}", command);
        String[] processid = StringUtils.removeBlank(StringUtils.split(analysis.unQuotation(expr.getOptionValue("-p")), analysis.getSegment()));
        String[] sessionid = StringUtils.removeBlank(StringUtils.split(analysis.unQuotation(expr.getOptionValue("-s")), analysis.getSegment()));
        return new TerminateCommand(this, orginalScript, sessionid, processid);
    }

}
