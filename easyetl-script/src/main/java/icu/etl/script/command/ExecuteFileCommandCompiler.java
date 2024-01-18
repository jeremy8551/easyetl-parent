package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = {"."}, keywords = {UniversalScriptVariable.SESSION_VARNAME_SCRIPTNAME, UniversalScriptVariable.SESSION_VARNAME_SCRIPTFILE, UniversalScriptVariable.SESSION_VARNAME_LINESEPARATOR})
public class ExecuteFileCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        if (command.startsWith(".")) {
            String filepath = analysis.trim(command.substring(1), 0, 1); // 脚本文件路径
            return new ExecuteFileCommand(this, orginalScript, filepath);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr033", command));
        }
    }

}
