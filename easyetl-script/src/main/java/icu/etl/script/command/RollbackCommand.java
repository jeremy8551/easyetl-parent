package icu.etl.script.command;

import java.io.File;

import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.ResourcesUtils;

/**
 * 回滚数据库事物
 */
public class RollbackCommand extends AbstractTraceCommand implements JumpCommandSupported, NohupCommandSupported {

    public RollbackCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        JdbcDao dao = dataSource.getDao();
        if (dao.isConnected()) {
            if (session.isEchoEnable() || forceStdout) {
                stdout.println(this.command);
            }

            dao.rollback();
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr065", this.command));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
