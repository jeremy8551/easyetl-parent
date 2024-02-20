package icu.etl.script.command;

import java.io.IOException;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
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

@ScriptCommand(name = "cursor", keywords = {"cursor"})
public class CursorCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("loop", "end loop");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("cursor");
        String name = it.next();
        it.assertNext("loop");
        it.assertLast("loop");
        it.assertLast("end");
        String body = it.readOther();
        List<UniversalScriptCommand> commands = parser.read(body);
        for (UniversalScriptCommand cmd : commands) {
            if ((cmd instanceof LoopCommandSupported) && !((LoopCommandSupported) cmd).enableLoop()) {
                throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "cursor loop", cmd.getScript())); // 在语句中不能使用的语句
            }
        }

        CommandList list = new CommandList(name, commands);
        return new CursorCommand(this, command, name, list);
    }

}
