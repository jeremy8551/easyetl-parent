package icu.etl.script.command;

import java.io.IOException;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalCommandCompilerResult;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;

@ScriptCommand(name = "declare", keywords = {"declare", "global", "progress"})
public class DeclareProgressCommandCompiler extends AbstractGlobalCommandCompiler {

    public final static String REGEX = "^(?i)\\s*declare\\s+([\\S+\\s+]*)progress\\s+use\\s+(.*)\\s+print\\s+(.*)\\s+total\\s+(\\S+)\\s+times\\s*[\\;]*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public UniversalCommandCompilerResult match(String name, String script) {
        return pattern.matcher(script).find() ? UniversalCommandCompilerResult.NEUTRAL : UniversalCommandCompilerResult.IGNORE;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        WordIterator it = analysis.parse(session.getAnalysis().replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("declare");
        boolean global = it.isNext("global");
        if (global) {
            it.assertNext("global");
        }

        String name = it.isNext("progress") ? null : it.next(); // 可能没有进度输出ID编号
        it.assertNext("progress");
        it.assertNext("use");
        String type = it.next();
        it.assertNext("print");
        String message = analysis.unQuotation(it.readUntil("total"));
        String number = it.next();
        it.assertNext("times");
        it.assertOver();

        return new DeclareProgressCommand(this, command, name, type, message, number, global);
    }

}
