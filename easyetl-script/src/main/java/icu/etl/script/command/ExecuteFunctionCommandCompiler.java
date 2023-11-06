package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptContextAware;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.internal.FunctionSet;

@ScriptCommand(name = "*", keywords = {})
public class ExecuteFunctionCommandCompiler extends AbstractTraceCommandCompiler implements UniversalScriptContextAware {

    private UniversalScriptContext context;

    public void setContext(UniversalScriptContext context) {
        this.context = context;
    }

    public int match(String name, String line) {
        FunctionSet local = FunctionSet.get(this.context, false); // 优先从局部域中查
        if (local.contains(name)) {
            return 0;
        }

        FunctionSet global = FunctionSet.get(this.context, true); // 再次从全局域中查
        if (global.contains(name)) {
            return 0;
        }

        return 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String script) throws IOException {
        return new ExecuteFunctionCommand(this, orginalScript, script);
    }

}
