package icu.etl.script.command;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import icu.etl.database.Jdbc;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.CursorMap;
import icu.etl.util.ResourcesUtils;

/**
 * 从游标中读取信息到变量中 <br>
 * <p>
 * FETCH cno INTO tmp_ywdate, tmp_orgcode, tmp_finishcode;
 */
public class FetchCursorCommand extends AbstractCommand {

    /** 游标名 */
    private String name;

    /** 变量名数组 */
    private List<String> variableNames;

    public FetchCursorCommand(UniversalCommandCompiler compiler, String command, String name, List<String> variableNames) {
        super(compiler, command);
        this.name = name;
        this.variableNames = variableNames;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        if (!context.getChecker().isVariableName(this.name)) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr088", this.command, this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        CursorMap map = CursorMap.get(context);
        if (!map.contains(this.name)) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr002", this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        JdbcQueryStatement query = map.get(this.name);
        ResultSet result = query.getResultSet();
        int column = Jdbc.getColumnCount(result);

        int size = this.variableNames.size();
        if (size > column) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr013", size, column));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        for (int i = 0; i < size; i++) {
            context.addLocalVariable(this.variableNames.get(i), result.getObject(i + 1));
        }
        return 0;
    }

    public void terminate() throws Exception {
    }

}
