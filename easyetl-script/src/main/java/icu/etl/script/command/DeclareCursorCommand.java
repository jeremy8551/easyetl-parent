package icu.etl.script.command;

import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.CursorMap;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 建立游标 <br>
 * <br>
 * DECLARE name CURSOR WITH RETURN FOR select * from table ;
 */
public class DeclareCursorCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** 游标名 */
    private String name;

    /** SQL语句 */
    private String sql;

    /** 数据库操作类 */
    private JdbcDao dao;

    public DeclareCursorCommand(UniversalCommandCompiler compiler, String command, String name, String sql) {
        super(compiler, command);
        this.name = name;
        this.sql = sql;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        this.dao = dataSource.getDao();
        try {
            if (!this.dao.isConnected()) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr065", this.command));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            UniversalScriptAnalysis analysis = session.getAnalysis();
            String name = analysis.replaceVariable(session, context, this.name, false);
            UniversalScriptChecker checker = context.getChecker();
            if (!checker.isVariableName(name) || checker.isDatabaseKeyword(name)) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr077", this.command, name));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            String sql = analysis.replaceVariable(session, context, this.sql, true);
            if (session.isEchoEnable() || forceStdout) {
                stdout.println("declare " + name + " cursor with return for " + sql);
            }

            JdbcQueryStatement cursor = new JdbcQueryStatement(sql);
            CursorMap map = CursorMap.get(context);
            map.put(name, cursor);
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

}
