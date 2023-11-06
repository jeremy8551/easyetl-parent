package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.command.feature.DefaultCommandSupported;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

@ScriptCommand(name = "quiet", keywords = {"quiet"})
public class QuietCommandCompiler extends AbstractTraceCommandCompiler implements DefaultCommandSupported {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        String word = in.readSingleWord();
        if (word.endsWith(String.valueOf(analysis.getToken()))) { // 单词右侧不能有语句分隔符
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(78, word));
        } else {
            Ensure.exists("quiet", word);
            return word;
        }
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException, SQLException {
        UniversalScriptCommand subcommand = parser.read();
        String script = command + " " + subcommand.getScript();
        return new QuietCommand(this, script, subcommand);
    }

}
