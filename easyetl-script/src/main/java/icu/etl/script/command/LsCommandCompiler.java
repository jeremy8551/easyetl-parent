package icu.etl.script.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "ls", keywords = {"ls"})
public class LsCommandCompiler extends AbstractFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        CommandExpression expr = new CommandExpression(analysis, "ls -l ", command);
        List<String> filepathList = new ArrayList<String>(expr.getParameters());
        for (int index = 0; index < filepathList.size(); index++) {
            filepathList.set(index, filepathList.get(index));
        }

        boolean localhost = expr.containsOption("-l");
        return new LsCommand(this, orginalScript, filepathList, localhost);
    }

}
