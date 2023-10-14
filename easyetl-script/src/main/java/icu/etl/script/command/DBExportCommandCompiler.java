package icu.etl.script.command;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.annotation.ScriptCommand;
import icu.etl.database.JdbcObjectConverter;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.UserListener;
import icu.etl.expression.WordIterator;
import icu.etl.io.TextTable;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.BeanConfig;
import icu.etl.ioc.BeanFactory;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptUsage;
import icu.etl.util.CharTable;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "db", keywords = {})
public class DBExportCommandCompiler extends AbstractTraceCommandCompiler {

    public final static String REGEX = "^(?i)db\\s+export\\s+to\\s*.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readMultilineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        WordIterator it = analysis.parse(analysis.replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("db");
        it.assertNext("export");
        it.assertNext("to");
        String filepath = analysis.unQuotation(it.readUntil("of"));
        String filetype = it.next();

        CommandAttribute attrs = new CommandAttribute( //
                "charset:", "codepage:", "rowdel:", "coldel:", "escape:", //
                "chardel:", "column:", "colname:", "catalog:", "message:", //
                "listener:", "convert:", "charhide:", "writebuf:", "append", //
                "maxrows:", "dateformat:", "timeformat:", "timestampformat:", //
                "progress:", "escapes:", "title", "sleep:" //
        );

        if (it.isNext("modified")) {
            it.assertNext("modified");
            it.assertNext("by");

            while (!it.isNext("select")) { // 如果下一个单词不是 select
                String word = it.next();
                String[] array = StringUtils.splitProperty(word);
                if (array == null) {
                    attrs.setAttribute(word, ""); // 无值参数
                } else {
                    attrs.setAttribute(array[0], array[1]);
                }
            }
        }

        String sql = it.readOther();
        return new DBExportCommand(this, orginalScript, filepath, filetype, sql, attrs);
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        // 查找接口对应的的实现类
        List<BeanConfig> list1 = BeanFactory.getContext().getImplements(TextTableFile.class);
        CharTable ct1 = new CharTable(context.getCharsetName());
        ct1.addTitle("");
        ct1.addTitle("");
        ct1.addTitle("");
        for (BeanConfig anno : list1) {
            EasyBeanClass annotation = anno.getAnnotationAsImplement();
            ct1.addValue(annotation.kind());
            ct1.addValue(annotation.description());
            ct1.addValue(anno.getImplementClass().getName());
        }

        // 查找接口对应的的实现类
        List<BeanConfig> list2 = BeanFactory.getContext().getImplements(ExtractWriter.class);
        CharTable ct2 = new CharTable(context.getCharsetName());
        ct2.addTitle("");
        ct2.addTitle("");
        ct2.addTitle("");
        for (BeanConfig anno : list2) {
            EasyBeanClass annotation = anno.getAnnotationAsImplement();
            ct2.addValue(annotation.kind());
            ct2.addValue(annotation.description());
            ct2.addValue(anno.getImplementClass().getName());
        }

        out.println(new ScriptUsage(this.getClass() //
                , TextTable.class.getName() // 0
                , EasyBeanClass.class.getName() // 1
                , UserListener.class.getName() // 2
                , JdbcObjectConverter.class.getName() // 3
                , ExtractWriter.class.getName() // 4
                , ct1.removeLeftBlank().toSimpleShape() // 5
                , ct2.removeLeftBlank().toSimpleShape() // 6
                , TextTable.class.getName() // 7
        ));
    }

}
