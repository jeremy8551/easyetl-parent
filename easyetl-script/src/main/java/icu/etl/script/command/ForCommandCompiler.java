package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.internal.CommandList;

@ScriptCommand(name = "for", keywords = {"for", "loop", "end"})
public class ForCommandCompiler extends AbstractCommandCompiler {

    public final static String REGEX = "^(?i)for\\s+\\S+\\s+in\\s*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("loop", "end loop");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("for");
        String name = analysis.trim(it.next(), 0, 0);
        it.assertNext("in");
        String collection = it.readUntil("loop");
        it.assertLast("loop");
        it.assertLast("end");
        String script = it.readOther();
        List<UniversalScriptCommand> list = parser.read(script);
        CommandList cmdlist = new CommandList("for", list);
        return new ForCommand(this, command, name, collection, cmdlist);
    }

}
