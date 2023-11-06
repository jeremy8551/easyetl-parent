package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "daemon", keywords = {})
public class DaemonCommandCompiler extends ExecuteFileCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        if (analysis.startsWith(command, "daemon", 0, false)) {
            String filepath = analysis.trim(command.substring("daemon".length()), 0, 1); // 脚本文件路径
            return new DaemonCommand(this, command, filepath);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(33, command));
        }
    }

}
