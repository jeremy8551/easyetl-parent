package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "tail", keywords = {"tail"})
public class TailCommandCompiler extends AbstractFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "tail -n: --lang {0-1}", command);
        String charsetName = expr.getOptionValue("-lang");
        int line = StringUtils.parseInt(expr.getOptionValue("-n"), 10);
        String filepath = analysis.trim(expr.getParameter(), 0, 1);

        if (line <= 0) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr087", command, line));
        } else {
            return new TailCommand(this, orginalScript, line, charsetName, filepath);
        }
    }

}
