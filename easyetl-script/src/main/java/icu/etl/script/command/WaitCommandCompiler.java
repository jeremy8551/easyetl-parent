package icu.etl.script.command;

import java.io.IOException;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "wait", keywords = {"wait", UniversalScriptVariable.VARNAME_PID})
public class WaitCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        List<String> list = analysis.split(command, '=');
        Ensure.exists("wait", list.get(0));

        String id = null, timeout = null;
        for (int i = 1; i < list.size(); i++) {
            String str = list.get(i);

            if (str.equalsIgnoreCase("pid")) {
                int next = i + 1;
                if (next < list.size()) {
                    id = list.get(next);
                } else {
                    throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(99, command));
                }
            } else {
                timeout = StringUtils.join(list.subList(i, list.size()), ""); // 1min
            }
        }

        return new WaitCommand(this, orginalScript, id, timeout);
    }

}
