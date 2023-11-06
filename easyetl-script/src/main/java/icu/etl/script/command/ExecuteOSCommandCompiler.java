package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.command.feature.DefaultCommandSupported;

@ScriptCommand(name = "os", keywords = {"os"})
public class ExecuteOSCommandCompiler extends AbstractTraceCommandCompiler implements DefaultCommandSupported {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        if (analysis.startsWith(command, "os", 0, true)) {
            command = command.substring("os".length());
        }

        String oscommand = analysis.unQuotation(command);
        return new ExecuteOSCommand(this, orginalScript, oscommand);
    }

}
