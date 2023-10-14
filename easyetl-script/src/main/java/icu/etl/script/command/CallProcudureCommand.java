package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.DatabaseProcedureParameterList;
import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 执行数据库存储过程，call schema.procudure(?, 'test');
 */
public class CallProcudureCommand extends AbstractTraceCommand implements JumpCommandSupported, NohupCommandSupported {

    /** SQL语句 */
    private String sql;

    /** 数据库操作类 */
    private JdbcDao dao;

    public CallProcudureCommand(UniversalCommandCompiler compiler, String command, String sql) {
        super(compiler, command);
        this.sql = sql;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
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

            UniversalScriptChecker checker = context.getChecker();
            DatabaseProcedure obj = this.dao.callProcedureByJdbc(sql);
            DatabaseProcedureParameterList parameters = obj.getParameters();
            for (int i = 0, size = parameters.size(); i < size; i++) {
                DatabaseProcedureParameter parameter = parameters.get(i);
                if (parameter.isOutMode() && checker.isVariableName(parameter.getExpression())) {
                    List<String> list = StringUtils.splitVariable(parameter.getExpression(), new ArrayList<String>());
                    if (list.size() != 1) {
                        stderr.println(ResourcesUtils.getScriptStderrMessage(43, sql, parameter.getPlaceholder(), parameter.getExpression()));
                        return UniversalScriptCommand.COMMAND_ERROR;
                    } else {
                        String variableName = list.get(0);
                        context.addLocalVariable(variableName, parameter.getValue()); // 保存存储过程输出变量
                    }
                }
            }

            return 0;
        } finally {
            this.dao = null;
        }
    }

    public void terminate() throws IOException, SQLException {
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
