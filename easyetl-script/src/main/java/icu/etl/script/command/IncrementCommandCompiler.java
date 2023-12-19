package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.WordIterator;
import icu.etl.io.TableColumnComparator;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptUsage;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharTable;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "extract", keywords = {"extract"})
public class IncrementCommandCompiler extends AbstractTraceCommandCompiler {

    public final static String REGEX = "^(?i)extract\\s+increment\\s+compare\\s+.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readMultilineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws Exception {
        WordIterator it = analysis.parse(analysis.replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("extract");
        it.assertNext("increment");
        it.assertNext("compare");
        String newfileExpr = it.readUntil("and");
        String oldfileExpr = it.readUntil("write");
        String script = it.readOther();
        String[] array = StringUtils.removeBlank(StringUtils.split(script, ArrayUtils.asList("write"), analysis.ignoreCase()));

        IncrementExpression newfileexpr = new IncrementExpression(session, context, newfileExpr);
        IncrementExpression oldfileexpr = new IncrementExpression(session, context, oldfileExpr);
        IncrementExpression[] writeExpr = new IncrementExpression[array.length];
        for (int i = 0; i < array.length; i++) {
            String expression = array[i];
            writeExpr[i] = new IncrementExpression(session, context, expression);
        }
        return new IncrementCommand(this, orginalScript, newfileexpr, oldfileexpr, writeExpr);
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) { // 查找接口对应的的实现类
        List<EasyBeanInfo> list = context.getContainer().getBeanInfoList(TextTableFile.class);
        CharTable table = new CharTable(context.getCharsetName());
        table.addTitle("");
        table.addTitle("");
        table.addTitle("");
        for (EasyBeanInfo beanInfo : list) {
            table.addCell(beanInfo.getName());
            table.addCell(beanInfo.getDescription());
            table.addCell(beanInfo.getType().getName());
        }

        out.println(new ScriptUsage(this.getClass(), table.toString(CharTable.Style.simple), TableColumnComparator.class.getName()));
    }

}
