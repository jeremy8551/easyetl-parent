package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "wc", keywords = {"wc"})
public class WcCommandCompiler extends AbstractFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "wc --lang -clw {0-1}", command);
        String charsetName = expr.getOptionValue("-lang");
        String filepath = analysis.trim(expr.getParameter(), 0, 1);
        boolean words = expr.containsOption("-w");
        boolean bytes = expr.containsOption("-c");
        boolean lines = expr.containsOption("-l");

        if (!lines && !words && !bytes) {
            words = true;
            bytes = true;
            lines = true;
        }
        return new WcCommand(this, orginalScript, filepath, charsetName, filepath, words, bytes, lines);
    }

}
