package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "exists", keywords = {"exists"})
public class ExistsCommandCompiler extends AbstractFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "!exists -l {0-1}", command);
        String filepath = expr.getParameter();
        boolean localhost = expr.containsOption("-l");
        boolean reverse = expr.isReverse();
        return new ExistsCommand(this, orginalScript, filepath, localhost, reverse);
    }

}
