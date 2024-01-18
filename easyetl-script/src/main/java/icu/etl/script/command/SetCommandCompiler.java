package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.CommandExpression;
import icu.etl.os.linux.Linuxs;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.internal.ScriptUsage;
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

    public SetCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        CommandExpression expr = new CommandExpression(session.getAnalysis(), "set|var [-E|-e] {0|1}", command);
        int optionSize = expr.getOptionSize();
        int parameterSize = expr.getParameterSize();

        // 打印所有变量
        if (optionSize == 0 && parameterSize == 0) {
            return new SetCommand(this, command, null, null, 2);
        }

        // 只有 -e 或 -E 选项: 检查命令返回值或不检查命令返回值
        if (parameterSize == 0) {
            return new SetCommand(this, command, null, null, expr.containsOption("-e") ? 4 : 5);
        }

        String str = expr.getParameter(); // name=value or name=SQL
        int index = str.indexOf('=');
        if (index == -1) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr147", command));
        }

        // 变量名
        String name = StringUtils.trimBlank(str.substring(0, index));
        if (!context.getChecker().isVariableName(name) || name.startsWith("$")) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr088", command, name));
        }

        // 变量值
        String value = StringUtils.trimBlank(Linuxs.removeShellNote(str.substring(index + 1), null));

        // 查询SQL语句
        if (analysis.indexOf(value, "select", 0, 1, 0) != -1) {
            return new SetCommand(this, command, name, value, 1);
        }

        // 删除变量
        if (value.length() == 0) {
            return new SetCommand(this, command, name, value, 3);
        }

        // 变量赋值
        return new SetCommand(this, command, name, value, 0);
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
