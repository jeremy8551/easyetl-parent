package icu.etl.script.command;

import java.io.IOException;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "nohup", keywords = {"nohup", UniversalScriptVariable.VARNAME_PID})
public class NohupCommandCompiler extends AbstractCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws Exception {
        String str = analysis.trim(command.substring("nohup".length()), 0, 1);
        String script = command;
        if (str.endsWith("&")) {
            script = str.substring(0, str.length() - 1);
        } else {
            script = str;
        }

        List<UniversalScriptCommand> list = parser.read(script);
        if (list.isEmpty()) { // nohup 命令中语句不能为空
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr024", command));
        }

        if (list.size() != 1) { // nohup 命令只能并发执行一个命令
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr022", command));
        }

        UniversalScriptCommand subcommand = list.get(0);
        if ((subcommand instanceof NohupCommandSupported) && ((NohupCommandSupported) subcommand).enableNohup()) { // 检查是否支持后台运行
            return new NohupCommand(this, command, subcommand);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr023", command, subcommand.getScript()));
        }
    }

}
