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
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.LoopCommandSupported;
import icu.etl.script.internal.CommandList;
import icu.etl.script.internal.ScriptUsage;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "declare", keywords = {"declare", "global", "handler", "begin", "end", UniversalScriptVariable.VARNAME_SQLSTATE, UniversalScriptVariable.VARNAME_EXCEPTION, UniversalScriptVariable.VARNAME_ERRORCODE, UniversalScriptVariable.VARNAME_EXITCODE, UniversalScriptVariable.VARNAME_ERRORSCRIPT})
public class DeclareHandlerCommandCompiler extends AbstractGlobalCommandCompiler {

    public final static String REGEX = "^(?i)\\s*declare\\s+([global\\s+]*)(\\S+)\\s+handler\\s+for\\s+(.*)\\s+begin\\s*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("begin", "end");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException, SQLException {
        WordIterator it = analysis.parse(command);
        it.assertNext("declare");
        boolean global = it.isNext("global");
        if (global) {
            it.assertNext("global");
        }

        String exitOrContinue = it.next();
        it.assertNext("handler");
        it.assertNext("for");

        String condition = it.readUntil("begin");
        boolean isExitHandler = analysis.indexOf(condition, "exitcode", 0, 1, 1) != -1;
        if (!isExitHandler // 异常处理逻辑的执行条件必须要有关键字
                && analysis.indexOf(condition, "exception", 0, 1, 1) == -1 //
                && analysis.indexOf(condition, "sqlstate", 0, 1, 1) == -1 //
                && analysis.indexOf(condition, "errorcode", 0, 1, 1) == -1 //
                && analysis.indexOf(condition, "exitcode", 0, 1, 1) == -1 //
        ) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(60, command, "exception", "sqlstate", "errorcode", "exitcode"));
        }

        it.assertLast("end");
        String body = it.readOther();
        List<UniversalScriptCommand> commands = parser.read(body);
        for (UniversalScriptCommand cmd : commands) { // 在 declare handler 语句中不能使用的语句
            if ((cmd instanceof LoopCommandSupported) && !((LoopCommandSupported) cmd).enableLoop()) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(30, "declare handler", cmd.getScript()));
            }
        }

        CommandList cmdlist = new CommandList("declareHandlerFor", commands);
        return new DeclareHandlerCommand(this, command, cmdlist, exitOrContinue, condition, isExitHandler, global);
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        out.println(new ScriptUsage(this.getClass() //
                , StringUtils.left(UniversalScriptVariable.VARNAME_EXCEPTION, 15, ' ') //
                , UniversalScriptVariable.VARNAME_EXCEPTION //
                , StringUtils.left(UniversalScriptVariable.VARNAME_ERRORCODE, 15, ' ') //
                , UniversalScriptVariable.VARNAME_ERRORCODE //
                , StringUtils.left(UniversalScriptVariable.VARNAME_SQLSTATE, 15, ' ') //
                , UniversalScriptVariable.VARNAME_SQLSTATE //
                , StringUtils.left(UniversalScriptVariable.VARNAME_ERRORSCRIPT, 15, ' ') //
                , UniversalScriptVariable.VARNAME_ERRORSCRIPT //
                , StringUtils.left(UniversalScriptVariable.VARNAME_EXITCODE, 15, ' ') //
                , UniversalScriptVariable.VARNAME_EXITCODE //
        ));
    }

}
