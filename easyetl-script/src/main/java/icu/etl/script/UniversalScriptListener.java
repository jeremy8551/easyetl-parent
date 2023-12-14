package icu.etl.script;

import java.io.Reader;
import java.util.List;

public interface UniversalScriptListener {

    /**
     * 判断事件监听器是否存在
     *
     * @param cls
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
     * @param listener
     */
    void addAll(UniversalScriptListener listener);

    /**
     * 移除一个监听器
     *
     * @param cls 监听器类信息
     * @return
     */
    boolean remove(Class<? extends UniversalCommandListener> cls);

    /**
     * 查询类信息对应的事件监听器
     *
     * @param cls 类信息
     * @return
     */
    UniversalCommandListener get(Class<? extends UniversalCommandListener> cls);

    /**
     * 返回事件监听器集合
     *
     * @return
     */
    List<UniversalCommandListener> values();

    /**
     * 脚本引擎执行会话之前的运行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param in
     * @throws Exception
     */
    void startScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws Exception;

    /**
     * 脚本命令执行前的运行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param command
     * @return 返回 true 表示可以执行 {@linkplain UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法 <br>
     * 返回 false 表示跳过 {@linkplain UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法执行下一个命令
     * @throws Exception
     */
    boolean beforeCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptCommand command) throws Exception;

    /**
     * 脚本命令执行后运行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param command
     * @param result
     * @throws Exception
     */
    void afterCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws Exception;

    /**
     * 脚本引擎命令执行抛出异常时运行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param command
     * @param result
     * @param e
     * @throws Exception
     */
    void catchCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Exception e) throws Exception;

    /**
     * 脚本引擎命令执行抛出异常时运行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param command
     * @param result
     * @param e
     * @throws Exception
     */
    void catchScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result, Exception e) throws Exception;

    /**
     * 退出脚本引擎前执行的业务逻辑
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param command
     * @param result
     * @throws Exception
     */
    void exitScript(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, UniversalCommandResultSet result) throws Exception;

}
