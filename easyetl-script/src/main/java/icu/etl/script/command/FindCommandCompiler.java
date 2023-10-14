package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "find", keywords = {"find"})
public class FindCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "find -hdpR -enos: {1}", command);
        String filepath = expr.getParameter();
        String name = expr.getOptionValue("-n");
        Ensure.isTrue(expr.containsOption("-n"));

        boolean loop = !expr.containsOption("-R");
        boolean hidden = expr.containsOption("-h");
        boolean distinct = expr.containsOption("-d");
        boolean position = expr.containsOption("-p");
        String encoding = expr.containsOption("-e") ? expr.getOptionValue("-e") : StringUtils.CHARSET;
        String outputFile = expr.containsOption("-o") ? expr.getOptionValue("-o") : "";
        String outputDelimiter = expr.containsOption("-s") ? StringUtils.defaultString(expr.getOptionValue("-s"), "\n") : "\n";
        return new FindCommand(this, orginalScript, filepath, name, encoding, outputFile, outputDelimiter, loop, hidden, distinct, position);
    }

}
