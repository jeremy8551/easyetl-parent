package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.DatabaseException;
import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.script.internal.ScriptStatement;
import icu.etl.script.internal.StatementMap;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 建立数据库批处理程序 <br>
 * <br>
 * DECLARE name Statement by 1000 batch WITH insert into v1_test (f1, f2, f3) values (?,?, ?);
 */
public class DeclareStatementCommand extends AbstractCommand {

    /** 批处理名字 */
    private String name;

    /** SQL语句 */
    private String sql;

    /** 批量提交记录数 */
    private String batchRecords;

    /** 数据库操作类 */
    private JdbcDao dao;

    public DeclareStatementCommand(UniversalCommandCompiler compiler, String command, String name, String sql, String batchRecords) {
        super(compiler, command);
        this.name = name;
        this.sql = sql;
        this.batchRecords = batchRecords;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String sql = analysis.replaceVariable(session, context, this.sql, true);
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        this.dao = dataSource.getDao();
        try {
            if (!this.dao.isConnected()) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr065", this.command));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            String name = analysis.replaceVariable(session, context, this.name, false);
            UniversalScriptChecker checker = context.getChecker();
            if (!checker.isVariableName(name) || checker.isDatabaseKeyword(name)) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr079", this.command, name));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            if (session.isEchoEnable() || forceStdout) {
                stdout.println("declare " + name + " statement with " + sql);
            }

            int batch = Ensure.isInt(analysis.replaceVariable(session, context, this.batchRecords, true));

            ScriptStatement statement = new ScriptStatement(this.dao, context.getFormatter(), batch, name, sql);
            StatementMap.get(context).put(name, statement);
            return 0;
        } catch (Exception e) {
            throw new DatabaseException(sql, e);
        } finally {
            this.dao = null;
        }
    }

    public void terminate() throws Exception {
        if (this.dao != null) {
            Ensure.isTrue(this.dao.terminate());
        }
    }

}
