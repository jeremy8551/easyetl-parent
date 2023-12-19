package icu.etl.script.command;

import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 执行 SQL 语句
 */
public class SQLCommand extends AbstractCommand implements JumpCommandSupported, NohupCommandSupported {

    /** SQL语句 */
    private String sql;

    /** 数据库操作类 */
    private JdbcDao dao;

    public SQLCommand(UniversalCommandCompiler compiler, String command, String sql) {
        super(compiler, command);
        this.sql = sql;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        this.dao = dataSource.getDao();
        try {
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String sql = analysis.replaceVariable(session, context, this.sql, true);
            if (!this.dao.isConnected()) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(65, sql));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            if (session.isEchoEnable() || forceStdout) {
                stdout.println(sql);
            }

            int rows = this.dao.execute(sql, null);
            session.addVariable(UniversalScriptVariable.VARNAME_UPDATEROWS, rows);
            session.removeValue();
            session.putValue("rows", rows);
            return 0;
        } finally {
            this.dao = null;
        }
    }

    public void terminate() throws Exception {
        if (this.dao != null) {
            Ensure.isTrue(this.dao.terminate());
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
