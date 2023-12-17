package icu.etl.script.command;

import javax.script.Bindings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import icu.etl.database.JdbcDao;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptExpression;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;

/**
 * 设置变量
 */
public class SetCommand extends AbstractGlobalCommand {

    /** 变量名 */
    private String name;

    /** 变量值 */
    private String value;

    /** 0表示赋值表达式; 1表示查询SQL; 2表示输出所有变量; 3表示删除变量 */
    private int type;

    public SetCommand(UniversalCommandCompiler compiler, String command, String name, String value, int type) {
        super(compiler, command);
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        switch (this.type) {
            case 0: // 为变量赋值
                return this.setVariable(session, context, stdout, stderr, this.value);
            case 1: // 使用SQL查询结果为变量赋值
                return this.querySQL(session, context, stdout, stderr, this.value);
            case 2: // 打印所有变量
                if (session.isEchoEnable() || forceStdout) {
                    return this.printVariable(session, context, stdout, stderr);
                } else {
                    return 0;
                }
            case 3: // 删除变量
                return this.removeVariable(session, context, stdout, stderr);
            default: // 错误
                stderr.println(ResourcesUtils.getScriptStderrMessage(131, this.command, this.type, "0, 1, 2, 3"));
                return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    /**
     * 为变量赋值
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param value
     * @return
     */
    protected int setVariable(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String value) {
        Object object = value;
        if (!session.getAnalysis().isBlankline(value)) {
            object = new UniversalScriptExpression(session, context, stdout, stderr, value).value();
        }

        if (this.isGlobal()) {
            context.addGlobalVariable(this.name, object);
        } else {
            context.addLocalVariable(this.name, object);
        }
        return 0;
    }

    /**
     * 删除变量
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @return
     */
    protected int removeVariable(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr) {
        context.removeAttribute(this.name, UniversalScriptContext.ENGINE_SCOPE);
        context.removeAttribute(this.name, UniversalScriptContext.GLOBAL_SCOPE);
        return 0;
    }

    /**
     * 将SQL查询结果为变量赋值
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param sql
     * @return
     * @throws Exception
     */
    protected int querySQL(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String sql) throws Exception {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        JdbcDao dao = dataSource.getDao();
        sql = session.getAnalysis().replaceVariable(session, context, sql, false);
        if (!dao.isConnected()) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(65, sql));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 执行查询并将结果集保存到变量域
        Object value = dao.queryFirstRowFirstCol(sql);
        Object newvalue = context.getFormatter().formatJdbcParameter(session, context, value);
        if (this.isGlobal()) {
            context.addGlobalVariable(this.name, newvalue);
        } else {
            context.addLocalVariable(this.name, newvalue);
        }
        return 0;
    }

    /**
     * 打印所有变量
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @return
     */
    protected int printVariable(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr) {
        Set<String> gks = context.getGlobalVariable().keySet(); // 全局变量名
        Set<String> lks = context.getLocalVariable().keySet(); // 局部变量名
        Set<String> eks = this.toEnvironmentNames(context.getEnvironmentVariable()); // 环境变量名
        int size = gks.size() + lks.size() + eks.size(); // 变量个数
        HashSet<String> names = new HashSet<String>(size);
        names.addAll(gks);
        names.addAll(lks);
        names.addAll(eks);

        // 排序
        ArrayList<String> list = new ArrayList<String>(size);
        list.addAll(names);
        Collections.sort(list, new StringComparator());

        // 打印
        StringBuilder buf = new StringBuilder(size * 20);
        for (String name : list) {
            if (context.containsGlobalVariable(name)) {
                buf.append(name).append('=').append(context.getGlobalVariable(name)).append(FileUtils.lineSeparator);
            } else if (context.containsLocalVariable(name)) {
                buf.append(name).append('=').append(context.getLocalVariable(name)).append(FileUtils.lineSeparator);
            } else if (context.containsEnvironmentVariable(name)) {
                buf.append(name).append('=').append(context.getEnvironmentVariable(name)).append(FileUtils.lineSeparator);
            } else {
                throw new UnsupportedOperationException(name);
            }
        }
        stdout.println(buf);
        return 0;
    }

    /**
     * 返回环境变量名集合
     *
     * @param bindings 环境变量集合
     * @return 集合
     */
    protected Set<String> toEnvironmentNames(Bindings bindings) {
        try {
            return bindings.keySet();
        } catch (Exception e) {
            return new HashSet<String>(); // 如果环境变量集合不支持读取变量名
        }
    }

}
