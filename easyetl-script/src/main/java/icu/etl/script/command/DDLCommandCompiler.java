package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.database.Jdbc;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "ddl")
public class DDLCommandCompiler extends AbstractTraceCommandCompiler {

    @Override
    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    @Override
    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(command));
        if (array.length > 1) {
            String schema = Jdbc.getSchema(array[1]);
            String tableName = Jdbc.removeSchema(array[1]);
            return new DDLCommand(this, orginalScript, tableName, schema);
        }
        throw new UniversalScriptException(command);
    }

}
