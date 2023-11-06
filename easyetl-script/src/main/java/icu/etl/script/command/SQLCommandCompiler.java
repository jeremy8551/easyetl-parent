package icu.etl.script.command;

import java.io.IOException;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.DefaultCommandSupported;

@ScriptCommand(name = {"select", "insert", "update", "delete", "alter", "drop", "create", "merge", "sql", "/*", "/**", "--"}, keywords = {UniversalScriptVariable.VARNAME_UPDATEROWS})
public class SQLCommandCompiler extends AbstractCommandCompiler implements DefaultCommandSupported {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readMultilineScript();
    }

    public UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException {
        String sql = command;
        if (analysis.startsWith(sql, "sql", 0, true)) {
            sql = sql.substring("sql".length());
        }

        return new SQLCommand(this, command, analysis.unQuotation(sql));
    }

}
