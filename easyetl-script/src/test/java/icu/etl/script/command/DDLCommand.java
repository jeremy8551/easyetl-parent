package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

public class DDLCommand extends AbstractTraceCommand implements NohupCommandSupported {

    private String tableName;

    private String schema;

    private JdbcDao dao;

    public DDLCommand(UniversalCommandCompiler compiler, String str, String tableName, String schema) {
        super(compiler, str);
        this.tableName = tableName;
        this.schema = schema;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        JdbcDao dao = dataSource.getDao();
        String catalog = dao.getCatalog();
        String schema = StringUtils.defaultString(this.schema, dao.getSchema());
        DatabaseTable table = dao.getTable(catalog, schema, this.tableName);
        stdout.println(dao.toDDL(table));
        return 0;
    }

    public void terminate() throws IOException, SQLException {
        if (this.dao != null) {
            Ensure.isTrue(this.dao.terminate());
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableHandler() {
        return true;
    }

    public boolean enableLoop() {
        return true;
    }

    public boolean enableFunction() {
        return true;
    }

}
