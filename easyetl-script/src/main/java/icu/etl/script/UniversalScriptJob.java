package icu.etl.script;

import icu.etl.concurrent.EasyJob;
import icu.etl.script.command.ContainerCommand;

/**
 * 脚本引擎中的并发任务接口
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptJob {

    /**
     * 判断是否可以执行 {@linkplain #getJob()} 方法
     *
     * @param session   用户会话信息
     * @param context   脚本引擎上下文信息
     * @param stdout    标准信息输出接口
     * @param stderr    错误信息输出接口
     * @param container 线程池
     * @return 返回 true 表示可以执行 {@linkplain #getJob()} 方法，返回 false 表示不能执行 {@linkplain #getJob()} 方法，会优先运行其他任务
     * @throws Exception 读取并发任务错误
     */
    boolean hasJob(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, ContainerCommand container) throws Exception;

    /**
     * 返回并发任务，并发任务会添加到线程池中等待调度执行
     *
     * @return 并发任务
     */
    EasyJob getJob();

}
