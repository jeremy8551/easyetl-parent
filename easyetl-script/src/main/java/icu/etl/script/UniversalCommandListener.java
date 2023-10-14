package icu.etl.script;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

public interface UniversalCommandListener {

    /**
     * 脚本会话执行前运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param in          脚本语句的输入流
     * @throws IOException
     * @throws SQLException
     */
    void startScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws IOException, SQLException;

    /**
     * 脚本会话执行抛出异常时运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本命令执行结果
     * @param e           执行脚本命令时抛出了异常信息
     * @return 返回 true 表示执行了业务逻辑，false 表示无业务逻辑
     * @throws IOException
     * @throws SQLException
     */
    boolean catchScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Throwable e) throws IOException, SQLException;

    /**
     * 退出脚本会话前执行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本命令执行结果
     */
    void exitScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws IOException, SQLException;

    /**
     * 脚本命令执行前的运行的业务逻辑
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param stdout  标准信息输出接口
     * @param stderr  错误信息输出接口
     * @param command 脚本命令
     * @return 返回 true 表示可以执行 {@linkplain UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法 <br>
     * 返回 false 表示跳过 {@linkplain UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法执行下一个命令
     * @throws IOException
     * @throws SQLException
     */
    boolean beforeCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptCommand command) throws IOException, SQLException;

    /**
     * 脚本命令执行后运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本命令执行结果
     * @throws IOException
     * @throws SQLException
     */
    void afterCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws IOException, SQLException;

    /**
     * 脚本引擎命令执行抛出异常时运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本命令执行结果
     * @param e           执行脚本命令时抛出了异常信息
     * @return 返回 true 表示执行了业务逻辑，false 表示无业务逻辑
     * @throws IOException
     * @throws SQLException
     */
    boolean catchCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Throwable e) throws IOException, SQLException;

}
