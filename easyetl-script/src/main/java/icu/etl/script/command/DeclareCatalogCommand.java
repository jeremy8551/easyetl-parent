package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.CollUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立数据库编目 <br>
 * <br>
 * 使用参数建立数据库编目： declare global name catalog configuration use driver com.ibm.xxx.driver url db2:url://xxxx:xx username login password login; <br>
 * 使用文件建立数据库编目： declare global name catalog configuration use file filepath; <br>
 */
public class DeclareCatalogCommand extends AbstractGlobalCommand {

    /** 数据库编目名 */
    protected String name;

    /** 数据库编目信息，属性名可以是 driver url username password admin adminpw */
    protected Properties catalog;

    public DeclareCatalogCommand(UniversalCommandCompiler compiler, String command, String name, Properties catalog, boolean global) {
        super(compiler, command);
        this.name = name;
        this.catalog = catalog;
        this.setGlobal(global);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String name = analysis.trim(analysis.replaceShellVariable(session, context, this.name, true, true, false, false), 0, 0);

        Set<String> keys = CollUtils.stringPropertyNames(this.catalog);
        for (String key : keys) {
            String value = analysis.replaceShellVariable(session, context, this.catalog.getProperty(key), true, true, true, true);
            this.catalog.setProperty(key, value);
        }

        boolean print = session.isEchoEnable() || forceStdout;
        if (this.catalog.containsKey(DeclareCatalogCommandCompiler.file)) {
            String filepath = FileUtils.replaceFolderSeparator(this.catalog.getProperty(DeclareCatalogCommandCompiler.file));
            if (!FileUtils.exists(filepath)) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(39, this.command, filepath));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            if (this.isGlobal()) {
                Object old = context.addGlobalCatalog(name, filepath);
                if (old != null && print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(30, filepath, name, StringUtils.toString(old)));
                }
            } else {
                Properties old = context.addLocalCatalog(name, filepath);
                if (old != null && print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(30, filepath, name, StringUtils.toString(old)));
                }
            }
        } else {
            if (this.isGlobal()) {
                Object old = context.addGlobalCatalog(name, this.catalog);
                if (old != null && print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(30, this.catalog, name, StringUtils.toString(old)));
                }
            } else {
                Properties old = context.addLocalCatalog(name, this.catalog);
                if (old != null && print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(30, this.catalog, name, StringUtils.toString(old)));
                }
            }
        }

        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

}
