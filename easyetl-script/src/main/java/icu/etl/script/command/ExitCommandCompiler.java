package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "exit", keywords = {"exit"})
public class ExitCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        String exitcode = analysis.trim(command.substring("exit".length()), 0, 1);
        if (exitcode.length() == 0) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(84, command));
        } else {
            return new ExitCommand(this, orginalScript, exitcode);
        }
    }

}
