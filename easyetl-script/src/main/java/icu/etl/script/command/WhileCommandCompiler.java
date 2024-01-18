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
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "while", keywords = {"while", "loop", "end"})
public class WhileCommandCompiler extends AbstractCommandCompiler {

    public final static String REGEX = "^(?i)\\s*while\\s+[^read]+.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public UniversalCommandCompilerResult match(String name, String script) {
        return pattern.matcher(script).find() ? UniversalCommandCompilerResult.NEUTRAL : UniversalCommandCompilerResult.IGNORE;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("loop", "end loop");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("while");
        String condition = it.readUntil("loop");
        if (analysis.isBlankline(condition)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr100", command));
        }
        it.assertLast("loop");
        it.assertLast("end");

        String body = it.readOther();
        List<UniversalScriptCommand> commands = parser.read(body);
        for (UniversalScriptCommand cmd : commands) {
            if ((cmd instanceof LoopCommandSupported) && !((LoopCommandSupported) cmd).enableLoop()) { // 在语句中不能使用的语句
                throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "while .. loop .. end loop", cmd.getScript()));
            }
        }

        CommandList cmdlist = new CommandList(condition, commands);
        return new WhileCommand(this, command, cmdlist);
    }

//	public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
//		WordReader in = analysis.parse(command);
//		in.assertNextWord("while");
//		
//		int key = analysis.indexOf(command, "while", 0, 0, 1);
//		Objects.requirePositive(key, "while", command);
//		
//		int start = key + "while".length();
//		int loop = analysis.indexOf(command, "loop", start, 1, 1);
//		Objects.requirePositive(loop, "loop", command);
//		
//		String condition = command.substring(start, loop);
//		if (analysis.isBlankline(condition)) {
//			throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr100", command));
//		}
//		
//		int end = analysis.lastIndexOf(command, "end", command.length() - 1, 1, 0);
//		Objects.requirePositive(end, "end", command);
//		
//		String cmdlist = command.substring(loop + "loop".length(), end);
//		List<UniversalScriptCommand> commands = parser.read(cmdlist);
//		
//		// 在语句中不能使用的语句
//		for (UniversalScriptCommand cmd : commands) {
//			if (!cmd.enableLoop()) {
//				throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "while .. loop .. end loop", cmd.getScript()));
//			}
//		}
//		
//		ScriptCommandList cmdlist0 = new ScriptCommandList(condition, commands);
//		WhileCommand cmd = new WhileCommand(command, cmdlist0);
//		cmdlist0.setOwner(cmd);
//		return cmd;
//	}

}
