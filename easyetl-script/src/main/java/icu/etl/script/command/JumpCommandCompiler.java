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

@ScriptCommand(name = "jump", keywords = {"jump"})
public class JumpCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        String str = command.substring("jump".length());
        String message = analysis.trim(str, 0, 1);

        if (analysis.isBlankline(message)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr095", command));
        } else {
            return new JumpCommand(this, orginalScript, message);
        }
    }

}
