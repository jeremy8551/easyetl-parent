package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "cp", keywords = {})
public class CpCommandCompiler extends AbstractFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "cp {1-2}", command);
        int size = expr.getParameters().size();
        if (size == 1) {
            String dstfile = analysis.trim(expr.getParameter(1), 0, 1);
            return new CpCommand(this, orginalScript, null, dstfile);
        } else {
            String srcfile = analysis.trim(expr.getParameter(1), 0, 1);
            String dstfile = analysis.trim(expr.getParameter(2), 0, 1);
            return new CpCommand(this, orginalScript, srcfile, dstfile);
        }
    }

}
