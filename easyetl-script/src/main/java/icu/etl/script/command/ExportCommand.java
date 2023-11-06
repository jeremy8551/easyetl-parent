package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.CommandList;
import icu.etl.script.internal.FunctionSet;
import icu.etl.util.ResourcesUtils;

/**
 * 使变量或数据库编目信息可以被子脚本继承并访问 <br>
 * export set name=value <br>
 * export set count=select count(*) from table; <br>
 * export function name <br>
 *
 * @author jeremy8551@qq.com
 */
public class ExportCommand extends AbstractCommand {

    /** 全局命令 */
    private AbstractGlobalCommand subcommand;

    /** 用户自定义方法名 */
    private String functionName;

    /**
     * 赋值语句的初始化方法
     *
     * @param compiler
     * @param command
     * @param subcommand
     */
    public ExportCommand(UniversalCommandCompiler compiler, String command, AbstractGlobalCommand subcommand) {
        super(compiler, command);
        this.functionName = null;
        this.subcommand = subcommand;
    }

    /**
     * 用户自定义方法名的初始化方法
     *
     * @param compiler
     * @param command
     * @param name
     */
    public ExportCommand(UniversalCommandCompiler compiler, String command, String name) {
        super(compiler, command);
        this.functionName = name;
        this.subcommand = null;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        if (this.subcommand == null) { // 表示将局部用户自定义方法转为全局自定义方法
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String name = analysis.replaceShellVariable(session, context, this.functionName, true, true, true, false);

            CommandList body = FunctionSet.get(context).get(name);
            if (body == null) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(34, name));
            } else {
                FunctionSet.get(context, true).add(body); // 添加到全局域
                FunctionSet.get(context, false).remove(body.getName()); // 从局部域中移除
                return 0;
            }
        } else { // 表示 set name=value 赋值表达式
            this.subcommand.setGlobal(true);
            return this.subcommand.execute(session, context, stdout, stderr, forceStdout);
        }
    }

    public void terminate() throws IOException, SQLException {
    }

}
