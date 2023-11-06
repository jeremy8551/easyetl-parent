package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.internal.ScriptUsage;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = {"set", "var"}, keywords = {"set", "var"})
public class SetCommandCompiler extends AbstractGlobalCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        String line = in.previewline();
        int index = analysis.indexOf(line, "=", 0, 2, 2);
        if (index == -1) { // 没有赋值符号时，表示打印变量表达式（只有一个set关键字）
            return in.readSingleWord();
        } else if (analysis.indexOf(line, "select", index, 1, 0) != -1) { // 表示数据库查询赋值语句
            return in.readMultilineScript();
        } else {// 赋值语句
            return in.readSinglelineScript();
        }
    }

    public SetCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        String expression = command.substring("set".length()); // name=value or name=SQL
        if (analysis.isBlankline(expression)) {
            return new SetCommand(this, command, null, null, 2);
        }

        int index = expression.indexOf('=');
        Ensure.isPosition(index);

        UniversalScriptChecker checker = context.getChecker();
        String name = analysis.trim(expression.substring(0, index), 0, 0);
        if (!checker.isVariableName(name) || name.startsWith("$")) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(88, command, name));
        }

        String value = analysis.trim(expression.substring(index + 1), 0, 0);
        int start = analysis.indexOf(value, "#", 0, 2, 2); // 删除注释信息
        value = (start == -1) ? value : value.substring(0, start);

        if (analysis.indexOf(value, "select", 0, 1, 0) != -1) {
            return new SetCommand(this, command, name, value, 1);
        } else if (value.length() == 0) {
            return new SetCommand(this, command, name, value, 3);
        } else {
            return new SetCommand(this, command, name, value, 0);
        }
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        out.println(new ScriptUsage(this.getClass() //
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_PWD, 15, ' ') // 0
                , UniversalScriptVariable.SESSION_VARNAME_PWD // 1
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_SCRIPTNAME, 15, ' ') // 2
                , UniversalScriptVariable.SESSION_VARNAME_SCRIPTNAME // 3
                , StringUtils.left(UniversalScriptVariable.VARNAME_CHARSET, 15, ' ') // 4
                , StringUtils.CHARSET // 5
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_LINESEPARATOR, 15, ' ')// 6
                , StringUtils.escapeLineSeparator(FileUtils.lineSeparator) // 7
                , StringUtils.left(UniversalScriptVariable.VARNAME_EXCEPTION, 15, ' ')// 8
                , UniversalScriptVariable.VARNAME_EXCEPTION // 9
                , StringUtils.left(UniversalScriptVariable.VARNAME_ERRORSCRIPT, 15, ' ')// 10
                , UniversalScriptVariable.VARNAME_ERRORSCRIPT // 11
                , StringUtils.left(UniversalScriptVariable.VARNAME_ERRORCODE, 15, ' ')// 12
                , UniversalScriptVariable.VARNAME_ERRORCODE // 13
                , StringUtils.left(UniversalScriptVariable.VARNAME_SQLSTATE, 15, ' ') // 14
                , UniversalScriptVariable.VARNAME_SQLSTATE // 15
                , StringUtils.left(UniversalScriptVariable.VARNAME_EXITCODE, 15, ' ') // 16
                , StringUtils.left(UniversalScriptVariable.VARNAME_UPDATEROWS, 15, ' ') // 17
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_JUMP, 15, ' ') // 18
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_STEP, 15, ' ') // 19
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_TEMP, 15, ' ') // 20
                , StringUtils.left(UniversalScriptVariable.SESSION_VARNAME_SCRIPTFILE, 15, ' ') // 21
                , StringUtils.left(UniversalScriptVariable.VARNAME_CATALOG, 15, ' ') // 22
        ));
    }

}
