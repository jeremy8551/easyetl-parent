package icu.etl.script.command;

import java.io.IOException;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.Word;
import icu.etl.expression.WordIterator;
import icu.etl.expression.WordQuery;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "if", keywords = {"if", "then", "elseif", "else", "fi"})
public class IfCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("if", "fi");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        IfCommand ifcommand = new IfCommand(this, command);
        QueryTail rule = new QueryTail();
        WordIterator it = analysis.parse(command);
        it.assertNext("if");
        it.assertLast("fi");

        String condition = it.readUntil("then");
        if (analysis.isBlankline(condition)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr091", command));
        }

        // 搜索 elseif 与 else 关键字
        String ifbody = it.read(rule); // 读取 then 关键到 elseif 或 else 关键字之间的逻辑
        ifcommand.setIf(condition, parser.read(ifbody));

        while (true) {
            if (it.equals("elseif")) { // 如果下一个起始单词是 elseif 关键字
                condition = it.readUntil("then");
                if (analysis.isBlankline(condition)) {
                    throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr091", command));
                }

                String elseifbody = it.read(rule);
                ifcommand.addElseIf(condition, parser.read(elseifbody));
            } else { // 下一个起始单词是 else 关键字
                String elsebody = it.readOther();
                ifcommand.setElse(parser.read(elsebody));
                break;
            }
        }

        return ifcommand;
    }

    /**
     * 搜索 if 语句中代码逻辑块的结束位置
     */
    static class QueryTail implements WordQuery {

        public int indexOf(CharSequence src, List<Word> list, int index, int last) {
            int i = index;
            for (; i <= last && i < list.size(); i++) {
                Word obj = list.get(i);
                String word = obj.getContent();

                // 如果是 elseif 或 else 关键字
                if (StringUtils.inArrayIgnoreCase(word, "elseif", "else")) {
                    return i;
                }

                // 嵌套 if 语句
                if (word.equalsIgnoreCase("if")) {
                    for (int j = i + 1; j <= last && j < list.size(); j++) {
                        obj = list.get(j);
                        word = obj.getContent();
                        if (word.equalsIgnoreCase("fi")) { // 嵌套语句结束位置
                            i = j;
                            break;
                        }
                    }
                    continue;
                }
            }

            return i;
        }
    }

}
