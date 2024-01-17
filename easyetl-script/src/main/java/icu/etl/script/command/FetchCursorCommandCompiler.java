package icu.etl.script.command;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalCommandCompilerResult;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "fetch", keywords = {"fetch", "into"})
public class FetchCursorCommandCompiler extends AbstractCommandCompiler {

    public final static String REGEX = "^(?i)\\s*fetch\\s+.+\\s+into\\s+.+";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public UniversalCommandCompilerResult match(String name, String script) {
        return pattern.matcher(script).find() ? UniversalCommandCompilerResult.NEUTRAL : UniversalCommandCompilerResult.IGNORE;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        WordIterator it = analysis.parse(session.getAnalysis().replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("fetch");
        String name = it.next();
        it.assertNext("into");
        String script = it.readOther();
        List<String> variableNames = analysis.split(script, analysis.getSegment());
        for (String variableName : variableNames) {
            if (!context.getChecker().isVariableName(variableName)) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(88, command, variableName));
            }
        }
        return new FetchCursorCommand(this, command, name, variableNames);
    }

}
