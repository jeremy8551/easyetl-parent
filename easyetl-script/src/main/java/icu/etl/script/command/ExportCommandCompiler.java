package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "export", keywords = {"export", "set", "function"})
public class ExportCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        WordIterator it = analysis.parse(command);
        it.assertNext("export");
        if (it.isNext("set")) { // 执行变量赋值表达式
            String script = it.readOther(); // set name=value
            SetCommandCompiler scp = new SetCommandCompiler();
            SetCommand subcommand = scp.compile(session, context, parser, analysis, script);
            return new ExportCommand(this, command, subcommand);
        } else if (it.isNext("function")) {
            it.assertNext("function");
            String name = it.readOther();
            return new ExportCommand(this, command, name);
        } else {
            throw new UniversalScriptException(command);
        }
    }

}
