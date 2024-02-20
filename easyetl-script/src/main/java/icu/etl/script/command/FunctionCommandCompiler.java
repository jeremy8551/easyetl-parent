package icu.etl.script.command;

import java.io.IOException;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "function", keywords = {"function"})
public class FunctionCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("{", "}");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("function");

        String name = it.next(); // functionName()
        if (!name.endsWith("()")) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr080", command));
        }

        String functionName = name.substring(0, name.length() - 2); // 自定义方法名
        UniversalScriptChecker checker = context.getChecker();
        if (!checker.isVariableName(functionName)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr081", command, functionName));
        }

        String part = it.readOther(); // { ... }
        String body = analysis.trim(part, 2, 2, '{', '}'); // 删除二侧的大括号
        List<UniversalScriptCommand> commands = parser.read(body);
        for (UniversalScriptCommand cmd : commands) { // 在语句中不能使用的语句
            if ((cmd instanceof LoopCommandSupported) && !((LoopCommandSupported) cmd).enableLoop()) {
                throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "function name() {}", cmd.getScript()));
            }
        }

        CommandList cmdlist = new CommandList(functionName, commands);
        return new FunctionCommand(this, command, cmdlist);
    }

}
