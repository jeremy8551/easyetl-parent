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

@ScriptCommand(name = "continue", keywords = {"continue"})
public class ContinueCommandCompiler extends AbstractSlaveCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        if ("continue".equalsIgnoreCase(command)) {
            return new ContinueCommand(this, orginalScript);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(109, command, "continue"));
        }
    }

}
