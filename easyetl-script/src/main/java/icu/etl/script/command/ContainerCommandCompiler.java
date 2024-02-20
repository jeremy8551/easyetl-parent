package icu.etl.script.command;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import icu.etl.annotation.ScriptCommand;
import icu.etl.collection.CaseSensitivMap;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptJob;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "container", keywords = {})
public class ContainerCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readPieceofScript("begin", "end");
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        WordIterator it = analysis.parse(command);
        it.assertNext("container");
        it.assertNext("to");
        it.assertNext("execute");
        it.assertNext("tasks");
        it.assertNext("in");
        it.assertNext("parallel");

        Map<String, String> attributes = new CaseSensitivMap<String>();
        if (it.isNext("using")) {
            it.assertNext("using");
            String parameter = it.readUntil("begin");
            List<String> list = analysis.split(parameter); // 解析属性信息
            for (String property : list) {
                String[] array = StringUtils.splitProperty(property);
                if (array == null) {
                    attributes.put(property, ""); // 无值参数
                } else {
                    attributes.put(array[0], array[1]);
                }
            }
        } else {
            it.assertNext("begin");
        }

        it.assertLast("end");
        String body = it.readOther();
        List<UniversalScriptCommand> cmdlist = parser.read(body);
        for (UniversalScriptCommand cmd : cmdlist) {
            if (!(cmd instanceof UniversalScriptJob)) {
                throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr030", "container to execute tasks in parallel .. begin .. end", cmd.getScript()));
            }
        }
        return new ContainerCommand(this, command, attributes, cmdlist);
    }

}
