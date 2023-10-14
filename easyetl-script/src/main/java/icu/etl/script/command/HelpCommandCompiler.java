package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.StringUtils;

@ScriptCommand(name = {"help", "man"}, keywords = {"help", "man"})
public class HelpCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        String str = StringUtils.ltrimBlank(command);
        int index = StringUtils.indexOfBlank(str, 0, -1);
        String parameter = index == -1 ? "" : StringUtils.trimBlank(str.substring(index));
        return new HelpCommand(this, orginalScript, parameter);
    }

}
