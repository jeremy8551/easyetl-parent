package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "wait", keywords = {"wait", UniversalScriptVariable.VARNAME_PID})
public class WaitCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        WordIterator it = analysis.parse(command);
        it.assertNext("wait");
        String pidExpression = it.next();
        String waitTime = analysis.trim(it.readOther(), 0, 1);

        String[] array = StringUtils.splitPropertyForce(pidExpression);
        Ensure.equals("pid", array[0]);
        return new WaitCommand(this, orginalScript, array[1], waitTime);
    }

}
