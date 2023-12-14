package icu.etl.script.session;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.CommandResultSet;
import icu.etl.util.StringUtils;

/**
 * 主线程
 *
 * @author jeremy8551@qq.com
 */
public class ScriptMainProcess {

    /** 会话创建时间 */
    private Date create;

    /** 最后执行的命令的返回值 */
    private Integer exitcode;

    /** 最近一次运行失败的命令 */
    private UniversalScriptCommand failCommand;

    /** 编号与命令的映射关系 */
    private LinkedHashMap<String, UniversalScriptCommand> cache;

    /**
     * 初始化
     */
    public ScriptMainProcess() {
        this.cache = new LinkedHashMap<String, UniversalScriptCommand>();
        this.create = new Date();
    }

    /**
     * 返回最后一个命令的返回值
     *
     * @return
     */
    public Integer getExitcode() {
        return this.exitcode;
    }

    /**
     * 执行命令
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param command     脚本命令
     * @return
     * @throws Exception
     */
    public UniversalCommandResultSet execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command) throws Exception {
        String key = StringUtils.toRandomUUID();
        try {
            this.cache.put(key, command);
            boolean can = context.getCommandListeners().beforeCommand(session, context, stdout, stderr, command); // 脚本命令执行前执行的逻辑代码
            CommandResultSet resultSet = new CommandResultSet();
            try {
                int exitcode = 0;
                if (can) {
                    exitcode = command.execute(session, context, stdout, stderr, forceStdout);
                }

                resultSet.setExitcode(exitcode);
                this.exitcode = exitcode;
                if (exitcode != 0) {
                    resultSet.setExitSession(true);
                    this.failCommand = command;
                }

                context.getCommandListeners().afterCommand(session, context, stdout, stderr, forceStdout, command, resultSet); // 脚本命令执行完毕后执行的逻辑代码
            } catch (Exception e) { // 脚本命令执行报错后执行的逻辑代码
                this.failCommand = command;
                context.getCommandListeners().catchCommand(session, context, stdout, stderr, forceStdout, command, resultSet, e);
            }

            return resultSet;
        } finally {
            this.cache.remove(key);
        }
    }

    /**
     * 终止所有命令
     *
     * @throws Exception
     */
    public void terminate() throws Exception {
        Set<String> keys = this.cache.keySet();
        for (String key : keys) {
            UniversalScriptCommand command = this.cache.get(key);
            if (command != null) {
                command.terminate();
            }
        }
    }

    /**
     * 返回所有正在执行命令的遍历器
     *
     * @return
     */
    public Iterator<UniversalScriptCommand> iterator() {
        return Collections.unmodifiableCollection(this.cache.values()).iterator();
    }

    /**
     * 主线程创建时间
     *
     * @return
     */
    public Date getCreateTime() {
        return this.create;
    }

    /**
     * 返回最近一次执行失败的命令
     *
     * @return
     */
    public UniversalScriptCommand getErrorCommand() {
        return this.failCommand;
    }

    /**
     * 返回最后执行的语句
     *
     * @return 脚本语句
     */
    public String getErrorScript() {
        return this.failCommand == null ? null : this.failCommand.getScript();
    }

}
