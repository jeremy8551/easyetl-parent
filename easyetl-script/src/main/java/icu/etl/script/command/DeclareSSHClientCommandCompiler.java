package icu.etl.script.command;

import java.io.IOException;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.LoginExpression;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "declare", keywords = {"declare", "ssh"})
public class DeclareSSHClientCommandCompiler extends AbstractCommandCompiler {

    public final static String REGEX = "^(?i)\\s*declare\\s+(\\S+)\\s+ssh\\s+client\\s+for\\s+connect\\s+to\\s+([^\\;|\\s]+)\\s*[\\;]*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        WordIterator it = analysis.parse(session.getAnalysis().replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("declare");
        String name = it.next();
        it.assertNext("ssh");
        it.assertNext("client");
        it.assertNext("for");
        it.assertNext("connect");
        it.assertNext("to");

        String part = it.readOther();
        LoginExpression expr = new LoginExpression(analysis, "ssh " + part);
        String host = expr.getLoginHost();
        String port = expr.getLoginPort();
        String username = expr.getLoginUsername();
        String password = expr.getLoginPassword();
        return new DeclareSSHClientCommand(this, command, name, host, port, username, password);
    }

}
