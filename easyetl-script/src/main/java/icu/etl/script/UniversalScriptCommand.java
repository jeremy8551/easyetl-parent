package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 脚本引擎的命令接口
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptCommand {

    /** 脚本返回值：语句执行错误 */
    public final static int ERROR = -1;

    /** 脚本返回值：执行脚本命令发生错误 */
    public final static int COMMAND_ERROR = -2;

    /** 脚本返回值：被强制终止并退出 */
    public final static int TERMINATE = -3;

    /** 脚本返回值：变量方法错误 */
    public final static int VARIABLE_METHOD_ERROR = -4;

    /**
     * 返回脚本命令对应的编译器
     *
     * @return
     */
    UniversalCommandCompiler getCompiler();

    /**
     * 执行命令
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @return 返回0表示正确, 返回非0表示不正确
     * @throws IOException
     * @throws SQLException
     */
    int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException;

    /**
     * 终止 {@link #execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法执行
     *
     * @throws IOException
     * @throws SQLException
     */
    void terminate() throws IOException, SQLException;

    /**
     * 返回语句
     *
     * @return
     */
    String getScript();

}
