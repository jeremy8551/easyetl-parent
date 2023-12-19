package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptContextAware;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "^", keywords = {})
public class PipeCommandCompiler extends AbstractCommandCompiler implements UniversalScriptContextAware {

    private UniversalScriptAnalysis analysis;

    private UniversalScriptContext context;

    public void setContext(UniversalScriptContext context) {
        this.context = context;
    }

    public int match(String name, String line) {
        if (this.analysis == null) {
            this.analysis = this.context.getFactory().buildCompiler().getAnalysis();
        }
        return this.containsPipe(line) ? 1 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        List<UniversalScriptCommand> commands = new ArrayList<UniversalScriptCommand>();
        List<String> list = this.splitPipe(command);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String script = it.next();

            if (analysis.isBlankline(script)) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(21, command));
            }

            List<UniversalScriptCommand> cmds = parser.read(script);
            if (cmds.size() != 1) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(29, command));
            } else {
                commands.add(cmds.get(0));
            }
        }

        return new PipeCommand(this, command, commands);
    }

    public List<String> splitPipe(String script) {
        char delimiter = '|';
        List<String> list = new ArrayList<String>();
        if (script == null) {
            return list;
        }

        int begin = 0;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                i = analysis.indexOfParenthes(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            if (c == '[') {
                i = analysis.indexOfBracket(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            if (c == '{') {
                i = analysis.indexOfBrace(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略字符常量中的空白
            else if (c == '\'') {
                i = analysis.indexOfQuotation(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                i = analysis.indexOfDoubleQuotation(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 二个重音符表示内部命令
            else if (c == '`') {
                i = analysis.indexOfAccent(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略字符数组中的字符
            else if (c == delimiter) {
                list.add(script.subSequence(begin, i).toString());
                for (int j = i + 1; j < script.length(); j++) {
                    char nc = script.charAt(j);
                    if (nc == delimiter) {
                        i++;
                    } else {
                        break; // 表示字符串起始位置
                    }
                }
                begin = i + 1;
            }
        }

        if (begin < script.length()) {
            list.add(script.subSequence(begin, script.length()).toString());
        } else if (begin == script.length()) {
            list.add("");
        }

        return list;
    }

    /**
     * 判断是否存在管道符
     *
     * @param script
     * @return
     */
    private boolean containsPipe(String script) {
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            if (c == '(') {
                int end = analysis.indexOfParenthes(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            if (c == '[') {
                int end = analysis.indexOfBracket(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            if (c == '{') {
                int end = analysis.indexOfBrace(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            // 忽略字符常量中的空白
            if (c == '\'') {
                int end = analysis.indexOfQuotation(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                int end = analysis.indexOfDoubleQuotation(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            // 忽略命令替换中的管道符
            else if (c == '`') {
                int end = analysis.indexOfAccent(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            } else if (c == '|') {
                int index = StringUtils.indexOfNotBlank(script, i + 1, -1);
                if (index == -1) {
                    return true;
                } else if (script.charAt(index) == '|') { // 连续2个竖线 ||
                    i = index;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

}
