package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "date", keywords = {"date", "day", "month", "year", "minute", "hour", "second", "millisecond"})
public class DateCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        int option = analysis.indexOf(command, "-d", 0, 0, 0);
        int index = analysis.indexOf(command, new char[]{'+', '-'}, option == -1 ? 0 : option + 1);
        String formula = analysis.trim((index == -1) ? null : command.substring(index), 0, 1);
        String date = (index == -1) ? command : command.substring(0, index);

        CommandExpression expr = new CommandExpression(analysis, "date -d:", date);
        String pattern = expr.getParameter();
        String dateStr = expr.containsOption("-d") ? expr.getOptionValue("-d") : null;
        return new DateCommand(this, orginalScript, formula, dateStr, pattern);
    }

}
