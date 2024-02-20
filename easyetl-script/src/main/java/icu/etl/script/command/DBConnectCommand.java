package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import javax.sql.DataSource;

import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立数据库连接命令 <br>
 * 关闭数据库连接命令 <br>
 * <p>
 * db connect to name; <br>
 * db connect reset
 */
public class DBConnectCommand extends AbstractTraceCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 数据库编目名 */
    private String name;

    public DBConnectCommand(UniversalCommandCompiler compiler, String command, String name) {
        super(compiler, command);
        this.name = name;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.name)) {
            this.name = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "db connect to", this.name));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (this.name.length() == 0) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr004", this.command));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        ScriptDataSource dataSource = ScriptDataSource.get(context);
        JdbcDao dao = dataSource.getDao();
        boolean print = session.isEchoEnable() || forceStdout;
        if ("reset".equalsIgnoreCase(this.name)) { // 关闭当前数据库连接
            if (print) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout007", "", dataSource.getCatalog()));
            }

            dao.commit();
            dao.close();
            return 0;
        }

        if (dao.isConnected()) { // 先关闭数据库连接，再创建新的连接
            dao.commit();
            dao.close();
        }

        DataSource pool = dataSource.getPool(this.name); // 找到对应的数据库连接池
        dao.setConnection(pool.getConnection(), true);
        dataSource.setCatalog(this.name); // 切换当前数据库编目名
        context.addLocalVariable(UniversalScriptVariable.VARNAME_CATALOG, this.name); // 保存当前数据库编目名

        if (dao.isConnected()) {
            if (print) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout006", "", this.name));
            }

            if (dao.getDialect() != null) {
                context.getChecker().setDatabaseKeywords(dao.getDialect().getKeyword(dao.getConnection()));
            }
            return 0;
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr008", context, this.name));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
