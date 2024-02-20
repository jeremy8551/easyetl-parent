package icu.etl.script.command;

import java.io.File;

import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.StringUtils;

public class DDLCommand extends AbstractTraceCommand implements NohupCommandSupported {

    private String tableName;

    private String schema;

    private JdbcDao dao;

    public DDLCommand(UniversalCommandCompiler compiler, String command, String tableName, String schema) {
        super(compiler, command);
        this.tableName = tableName;
        this.schema = schema;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(analysis.replaceShellVariable(session, context, this.command, true, true, true, false));
        }

        this.dao = ScriptDataSource.get(context).getDao();
        String catalog = this.dao.getCatalog();
        String schema = analysis.replaceShellVariable(session, context, this.schema, true, true, true, false);
        String tableName = analysis.replaceShellVariable(session, context, this.tableName, true, true, true, false);

        if (StringUtils.isBlank(schema)) {
            schema = this.dao.getSchema();
        }

        DatabaseTable table = this.dao.getTable(catalog, schema, tableName);
        if (table == null) {
            stdout.println("null");
        } else {
            stdout.println(this.dao.toDDL(table));
        }
        return 0;
    }

    public void terminate() throws Exception {
        if (this.dao != null) {
            this.dao.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

}
