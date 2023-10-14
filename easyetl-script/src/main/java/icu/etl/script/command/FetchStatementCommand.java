package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.database.DatabaseException;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.internal.ScriptStatement;
import icu.etl.script.internal.StatementMap;
import icu.etl.util.ResourcesUtils;

/**
 * 将变量更新到数据库表中 <Br>
 * <p>
 * FETCH tmp_ywdate, tmp_orgcode, tmp_finishcode INSERT statmentName;
 */
public class FetchStatementCommand extends AbstractCommand implements JumpCommandSupported {

    /** 批处理名 */
    private String name;

    /** 变量名数组 */
    private List<String> variableNames;

    public FetchStatementCommand(UniversalCommandCompiler compiler, String command, String name, List<String> variableNames) {
        super(compiler, command);
        this.name = name;
        this.variableNames = variableNames;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        if (!context.getChecker().isVariableName(this.name)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(88, this.command, this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        StatementMap map = StatementMap.get(context);
        if (!map.contains(this.name)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(3, this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 变量个数 与 SQL语句中参数一致
        ScriptStatement statement = map.get(this.name);
        int size = statement.getParameterCount();
        if (size != this.variableNames.size()) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(138, this.command, size));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        try {
            for (int i = 0; i < size; i++) {
                String variableName = this.variableNames.get(i); // variable name
                Object value = context.getAttribute(variableName); // variable value
                statement.setParameter(i, value);
            }
            statement.executeBatch();
            return 0;
        } catch (Exception e) {
            throw new DatabaseException(this.command, e);
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableJump() {
        return true;
    }

}
