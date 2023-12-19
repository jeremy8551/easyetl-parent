package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

/**
 * 删除数据库编目信息 <br>
 * undeclare name catalog configuration <br>
 */
public class UndeclareCatalogCommand extends AbstractGlobalCommand {

    /** 数据库编目名 */
    private String name;

    public UndeclareCatalogCommand(UniversalCommandCompiler compiler, String command, String name, boolean global) {
        super(compiler, command);
        this.name = name;
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        if (this.isGlobal()) {
            context.removeGlobalCatalog(this.name);
        }
        context.removeLocalCatalog(this.name);
        return 0;
    }

    public void terminate() throws Exception {
    }

}
