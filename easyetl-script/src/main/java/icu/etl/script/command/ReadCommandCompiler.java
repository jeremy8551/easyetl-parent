package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
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
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "while", keywords = {"while", "read", "do", "done"})
public class ReadCommandCompiler extends AbstractCommandCompiler {

    public final static String REGEX = "^(?i)\\s*while\\s+read\\s+\\S+\\s+do\\s*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public UniversalCommandCompilerResult match(String name, String script) {
        return pattern.matcher(script).find() ? UniversalCommandCompilerResult.NEUTRAL : UniversalCommandCompilerResult.IGNORE;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        String loopScript = in.readPieceofScript("do", "done");
        String inputScript = in.readSinglelineScript(); // 读取 < source 表达式
        String script = loopScript + " " + inputScript; // 完整的语句
        if (StringUtils.isNotBlank(loopScript) && StringUtils.isNotBlank(inputScript) && analysis.startsWith(inputScript, "<", 0, true)) {
            return script;
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr090", script, "<"));
        }
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("while");
        it.assertNext("read");
        String name = it.next(); // 变量名
        it.assertNext("do");
        String filepath = it.last(); // 读取 < filepath
        if (filepath.startsWith("<")) {
            filepath = filepath.substring(1); // 删除第一个字符 <
        } else {
            it.assertLast("<");
        }
        it.assertLast("done");
        String body = it.readOther();
        List<UniversalScriptCommand> commands = parser.read(body); // 读取循环体中的代码

        // 在语句中不能使用的语句
        for (UniversalScriptCommand cmd : commands) {
            if ((cmd instanceof LoopCommandSupported) && !((LoopCommandSupported) cmd).enableLoop()) {
                throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "while read line do .. done", cmd.getScript()));
            }
        }

        CommandList cmdlist = new CommandList(name, commands);
        return new ReadCommand(this, command, cmdlist, filepath);
    }

}
