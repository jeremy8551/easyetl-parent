package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.concurrent.Executor;
import icu.etl.script.command.ContainerCommand;

/**
 * 脚本引擎中的线程接口
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptThread {

    /**
     * 线程池在启动子线程执行脚本命令时会执行脚本命令对象上的 {@linkplain #start(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, ContainerCommand)} 方法
     *
     * @param session   用户会话信息
     * @param context   脚本引擎上下文信息
     * @param stdout    标准信息输出接口
     * @param stderr    错误信息输出接口
     * @param container 线程池
     * @return 返回 true 表示命令已准备就绪可以执行，之后线程池会调用 {@linkplain #getExecutor()} 方法创建一个用于执行命令的委托任务对象 {@linkplain Executor}，并将 {@linkplain Executor} 对象添加到线程池等待执行。<br>
     * 返回 false 表示命令执行失败，线程池优先执行其他命令
     * @throws IOException
     * @throws SQLException
     */
    boolean start(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, ContainerCommand container) throws IOException, SQLException;

    /**
     * 返回脚本命令的执行模版，线程池会将模版对象添加到线程池中等待调度执行。
     *
     * @return
     */
    Executor getExecutor();

}
