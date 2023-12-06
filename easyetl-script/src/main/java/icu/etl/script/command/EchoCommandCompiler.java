package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.Ensure;

@ScriptCommand(name = "echo", keywords = {"echo"})
public class EchoCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        boolean nonewline = false;
        String str = command.length() >= 5 ? command.substring(5) : ""; // 截取 echo 的前缀
        if (analysis.startsWith(str, "off", 0, true)) {
            return new EchoCommand(this, command, false);
        } else if (analysis.startsWith(str, "on", 0, true)) {
            return new EchoCommand(this, command, true);
        } else if (analysis.startsWith(str, "-n", 0, true)) {
            int index = str.indexOf("-n");
            Ensure.isFromZero(index);
            int next = index + 2;
            if (next >= str.length() || Character.isWhitespace(str.charAt(next))) {
                str = str.substring(next + 1);
                nonewline = true;
            }
        }

        return new EchoCommand(this, orginalScript, str, nonewline);
    }

}
