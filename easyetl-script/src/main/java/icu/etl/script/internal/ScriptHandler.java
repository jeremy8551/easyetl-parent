package icu.etl.script.internal;

import java.sql.SQLException;

import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptExpression;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.command.BreakCommand;
import icu.etl.script.command.ContinueCommand;
import icu.etl.script.command.ExitCommand;
import icu.etl.script.command.ReturnCommand;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * declare global (exit | continue) handler for ( exception | exitcode != 0 | sqlstate == '02501' | errorcode -803 ) begin .. end 语句
 */
public class ScriptHandler {

    /** 异常处理逻辑的执行条件（已删除空白字符且字符串转为小写字符） */
    private String key;

    /** 异常处理逻辑的执行代码 */
    private CommandList body;

    /** true 表示执行完异常处理逻辑后退出脚本引擎 false表示执行完异常处理逻辑后继续向下执行 */
    private boolean exit;

    /** 异常处理逻辑的匹配条件，exception | exitcode != 0 | sqlstate == -803 | errorcode == -803 */
    private String condition;

    /** 正在运行的脚本命令 */
    protected UniversalScriptCommand command;

    /**
     * 初始化
     *
     * @param exitOrContinue exit 或 continue
     * @param condition      异常处理逻辑的执行条件: exception | exitcode != 0 | sqlstate == '02501' | errorcode == -803
     * @param body           异常处理逻辑
     */
    public ScriptHandler(String exitOrContinue, String condition, CommandList body) {
        this.body = body;
        Ensure.exists(exitOrContinue, "continue", "exit");
        this.exit = "exit".equalsIgnoreCase(exitOrContinue);
        this.condition = StringUtils.trimBlank(condition);
        this.key = ScriptHandler.toKey(this.condition);
    }

    /**
     * 返回处理逻辑中的命令集合
     *
     * @return
     */
    public CommandList getList() {
        return body;
    }

    public ScriptHandler clone() {
        CommandList list = this.body.clone();
        String exit = this.exit ? "exit" : "continue";
        return new ScriptHandler(exit, this.condition, list);
    }

    /**
     * 删除异常处理逻辑执行条件中的空白字符
     *
     * @param condition 异常处理逻辑执行条件
     * @return
     */
    public static String toKey(String condition) {
        return StringUtils.removeBlank(condition).toLowerCase();
    }

    /**
     * 返回 error 或 step 或 echo 或 handle
     *
     * @return
     */
    public String getName() {
        return this.body.getName();
    }

    /**
     * 返回异常处理逻辑的执行条件
     *
     * @return
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 异常处理逻辑的匹配规则（无空白字符）
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * true表示执行逻辑命令集合后退出脚本<br>
     * false意味着继续执行
     *
     * @return
     */
    public boolean isReturnExit() {
        return exit;
    }

    /**
     * 返回 true 表示异常处理逻辑是JAVA异常处理
     *
     * @return
     */
    public boolean isExceptionHandler() {
        if (this.condition == null) {
            return false;
        } else {
            String str = this.condition.toLowerCase();
            return str.contains("exception") || str.contains("sqlstate") || str.contains("errorcode");
        }
    }

    /**
     * 执行退出脚本引擎的处理逻辑代码
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param exitcode    脚本引擎的退出值
     * @return 返回true表示匹配异常错误处理逻辑
     */
    public boolean executeExitcode(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Integer exitcode) {
        if (this.condition == null) {
            return false;
        }

        // 保存内置变量
        session.addVariable(UniversalScriptVariable.VARNAME_EXITCODE, exitcode);

        // 判断是否满足异常处理逻辑的执行条件
        if (new UniversalScriptExpression(session, context, stdout, stderr, this.condition).booleanValue()) {
            try {
                this.execute(session, context, stdout, stderr, forceStdout, this.body, new String[]{"handler"});
            } catch (Throwable e) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr061", this.toString()), e);
            }
            return true;
        }

        return false;
    }

    /**
     * 判断是否匹配异常错误处理逻辑 <br>
     * <br>
     * exception | sqlstate == '02501' | errorcode == -803 statement
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param script      发生错误的脚本语句
     * @param exception   异常信息
     * @return 返回true表示匹配异常错误处理逻辑
     */
    public boolean executeException(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, String script, Throwable exception) {
        if (exception == null || this.condition == null) {
            return false;
        }

        try {
            // 保存内置变量
            session.addVariable(UniversalScriptVariable.VARNAME_ERRORSCRIPT, script);
            session.addVariable(UniversalScriptVariable.VARNAME_EXCEPTION, context.getFormatter().format(exception));

            if (UniversalScriptVariable.VARNAME_EXCEPTION.equalsIgnoreCase(this.condition)) {
                this.execute(session, context, stdout, stderr, forceStdout, this.body, new String[]{"handler"});
                return true;
            }

            Throwable cause = exception;
            while (cause != null) {
                if (cause instanceof SQLException) { // 如果是数据库错误
                    SQLException sqlExp = (SQLException) cause;
                    while (sqlExp != null) {
                        String sqlstate = sqlExp.getSQLState();
                        String errorcode = String.valueOf(sqlExp.getErrorCode()); // 如：-803 表示主键冲突

                        // 保存内置变量
                        session.addVariable(UniversalScriptVariable.VARNAME_SQLSTATE, StringUtils.isInt(sqlstate) ? Integer.parseInt(sqlstate) : sqlstate);
                        session.addVariable(UniversalScriptVariable.VARNAME_ERRORCODE, StringUtils.isInt(errorcode) ? Integer.parseInt(errorcode) : errorcode);

                        // 判断是否满足异常处理逻辑的执行条件
                        if (new UniversalScriptExpression(session, context, stdout, stderr, this.condition).booleanValue()) {
                            this.execute(session, context, stdout, stderr, forceStdout, this.body, new String[]{"handler"});
                            return true;
                        }

                        sqlExp = sqlExp.getNextException();
                    }
                }

                cause = cause.getCause();
            }
        } catch (Throwable e1) {
            try {
                stderr.println(script, exception);
            } catch (Throwable e) {
                exception.printStackTrace();
            }

            try {
                stderr.println(script, e1);
            } catch (Throwable e) {
                e1.printStackTrace();
            }
        }

        return false;
    }

    protected int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, CommandList body, String[] args) throws Exception {
        try {
            if (session.isTerminate()) {
                return UniversalScriptCommand.TERMINATE;
            }

            session.setFunctionParameter(args);
            for (int i = 0; !session.isTerminate() && i < body.size(); i++) {
                UniversalScriptCommand command = body.get(i);
                if (command == null) {
                    continue;
                } else {
                    this.command = command;
                }

                int exitcode = command.execute(session, context, stdout, stderr, forceStdout);
                if (exitcode != 0) {
                    return exitcode;
                }

                if (command instanceof LoopCommandKind) {
                    LoopCommandKind cmd = (LoopCommandKind) command;
                    int type = cmd.kind();
                    if (type == ExitCommand.KIND) { // Exit script
                        return exitcode;
                    } else if (type == ReturnCommand.KIND) { // Exit method
                        return exitcode;
                    } else if (type == BreakCommand.KIND) { // break
                        throw new UnsupportedOperationException(ResourcesUtils.getMessage("script.message.stderr031"));
                    } else if (type == ContinueCommand.KIND) { // continue
                        throw new UnsupportedOperationException(ResourcesUtils.getMessage("script.message.stderr032"));
                    }
                }
            }

            if (session.isTerminate()) {
                return UniversalScriptCommand.TERMINATE;
            } else {
                return 0;
            }
        } finally {
            session.removeFunctionParameter();
            this.command = null;
        }
    }

    /**
     * 清空所有信息
     */
    public void clear() {
        this.body.clear();
//		this.parent = null;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("declare");
        buf.append(this.exit ? " exit" : " continue");
        buf.append(" handler for ");
        buf.append(this.condition);
        buf.append(" begin .. end");
        return buf.toString();
    }

}
