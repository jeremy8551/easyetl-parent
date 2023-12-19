package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;

/**
 * undeclare global command callback for exit | quit | echo | step[;]
 *
 * @author jeremy8551@qq.com
 */
@ScriptCommand(name = "undeclare", keywords = {"undeclare"})
public class UndeclareCallbackCommandCompiler extends AbstractGlobalCommandCompiler {

    public final static String REGEX = "^(?i)\\s*undeclare\\s+([global\\s+]*)command\\s+callback\\s+for\\s+.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(session.getAnalysis().replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("undeclare");
        boolean global = it.isNext("global");
        if (global) {
            it.assertNext("global");
        }
        it.assertNext("command");
        it.assertNext("callback");
        it.assertNext("for");
        String commandExpr = it.readOther();
        UniversalCommandCompiler compiler = session.getCompiler().getRepository().get(commandExpr);
        if (compiler == null) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(47, commandExpr));
        } else {
            Class<? extends UniversalCommandCompiler> cls = compiler.getClass();
            return new UndeclareCallbackCommand(this, command, cls, global);
        }
    }

}
