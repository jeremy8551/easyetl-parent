package icu.etl.script;

import java.io.Reader;
import java.util.List;

public interface UniversalScriptListener {

    /**
     * 判断事件监听器是否存在
     *
     * @param cls 类信息
     * @return 返回 true 表示已存在
     */
    boolean contains(Class<? extends UniversalCommandListener> cls);

    /**
     * 添加一个事件监听器
     *
     * @param listener 事件监听器
     */
    void add(UniversalCommandListener listener);

    /**
     * 添加命令监听器集合
     *
     * @param listener 监听器集合
     */
    void addAll(UniversalScriptListener listener);

    /**
     * 移除一个监听器
     *
     * @param cls 监听器类信息
     * @return 返回true表示移除成功
     */
    boolean remove(Class<? extends UniversalCommandListener> cls);

    /**
     * 查询类信息对应的事件监听器
     *
     * @param cls 类信息
     * @return 监听器
     */
    UniversalCommandListener get(Class<? extends UniversalCommandListener> cls);

    /**
     * 返回事件监听器集合
     *
     * @return 监听器集合
     */
    List<UniversalCommandListener> values();

    /**
     * 脚本引擎执行会话之前的运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param in          命令输入流
     * @throws Exception 发生错误
     */
    void startScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws Exception;

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
     * @throws Exception 发生错误
     */
    boolean beforeCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptCommand command) throws Exception;

    /**
     * 脚本命令执行后运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本执行结果集
     * @throws Exception 发生错误
     */
    void afterCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws Exception;

    /**
     * 脚本引擎命令执行抛出异常时运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本执行结果集
     * @param e           脚本命令抛出的异常信息
     * @throws Exception 发生错误
     */
    void catchCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Exception e) throws Exception;

    /**
     * 脚本引擎命令执行抛出异常时运行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本执行结果集
     * @param e           脚本命令抛出的异常信息
     * @throws Exception 发生错误
     */
    void catchScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Exception e) throws Exception;

    /**
     * 退出脚本引擎前执行的业务逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @param result      脚本执行结果集
     * @throws Exception 发生错误
     */
    void exitScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws Exception;

}
