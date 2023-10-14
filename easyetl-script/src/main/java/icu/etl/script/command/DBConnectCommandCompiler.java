package icu.etl.script.command;

import java.io.IOException;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;

@ScriptCommand(name = "db", keywords = {"db", UniversalScriptVariable.VARNAME_CATALOG})
public class DBConnectCommandCompiler extends AbstractTraceCommandCompiler {

    public final static String REGEX = "^(?i)\\s*db\\s+connect\\s+([^\\;]+)\\s*[\\;]*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        WordIterator it = analysis.parse(analysis.replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("db");
        it.assertNext("connect");
        if (it.isNext("to")) {
            it.assertNext("to");
            String name = it.readOther();
            return new DBConnectCommand(this, orginalScript, name);
        } else if (it.isNext("reset")) {
            String next = it.readOther();
            return new DBConnectCommand(this, orginalScript, next);
        } else {
            throw new UniversalScriptException(orginalScript);
        }
    }

}
