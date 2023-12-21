package icu.etl.script.command;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptUsage;

public abstract class AbstractCommandCompiler implements UniversalCommandCompiler {

    public int match(String name, String script) {
        return 0;
    }

    public abstract String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws Exception;

    public abstract UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String script) throws Exception;

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        out.println(new ScriptUsage(this.getClass()).toString());
    }

}
