package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "undeclare", keywords = {"undeclare"})
public class UndeclareHandlerCommandCompiler extends AbstractGlobalCommandCompiler {

    public final static String REGEX = "^(?i)\\s*undeclare\\s+([global\\s+]*)handler\\s+for\\s+.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("undeclare");
        boolean global = it.isNext("global");
        if (global) {
            it.assertNext("global");
        }
        it.assertNext("handler");
        it.assertNext("for");

        String condition = it.readOther();
        boolean isExitHandler = analysis.indexOf(condition, "exitcode", 0, 1, 1) != -1;
        return new UndeclareHandlerCommand(this, command, condition, isExitHandler, global);
    }

}
