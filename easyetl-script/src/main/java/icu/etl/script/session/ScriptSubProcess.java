package icu.etl.script.session;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import icu.etl.log.STD;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 子进程
 */
public class ScriptSubProcess {

    /** 命令编号与命令的映射关系 */
    private LinkedHashMap<String, ScriptProcess> map;

    /**
     * 初始化
     */
    public ScriptSubProcess() {
        this.map = new LinkedHashMap<String, ScriptProcess>();
    }

    /**
     * 创建子进程
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @param command
     * @param logfile
     * @return
     */
    public ScriptProcess create(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, UniversalScriptCommand command, File logfile) {
        ScriptProcessEnvironment environment = new ScriptProcessEnvironment(session, context, stdout, stderr, forceStdout, command, logfile);
        ScriptProcessThread thread = new ScriptProcessThread(environment);
        ScriptProcess process = new ScriptProcess(environment, thread);
        this.map.put(process.getPid(), process);
        return process;
    }

    /**
     * 判断是否已添加命令
     *
     * @param pid 进程编号
     * @return
     */
    public boolean contains(String pid) {
        return this.map.containsKey(pid);
    }

    /**
     * 返回进程
     *
     * @param pid 进程编号
     * @return
     */
    public ScriptProcess get(String pid) {
        if (StringUtils.isBlank(pid)) {
            throw new IllegalArgumentException(pid);
        } else {
            return this.map.get(pid);
        }
    }

    /**
     * 移除进程
     *
     * @param pid 进程编号
     */
    public ScriptProcess remove(String pid) {
        return this.map.remove(pid);
    }

    /**
     * 返回进程迭代器
     *
     * @return
     */
    public Iterator<ScriptProcess> iterator() {
        return Collections.unmodifiableCollection(this.map.values()).iterator();
    }

    /**
     * 终止所有后台进程
     *
     * @throws IOException
     * @throws SQLException
     */
    public void terminate() throws IOException, SQLException {
        Throwable exception = null;
        for (Iterator<ScriptProcess> it = this.map.values().iterator(); it.hasNext(); ) {
            ScriptProcess process = it.next();
            if (process != null) {
                try {
                    process.terminate();
                } catch (Throwable e) {
                    STD.out.error(ResourcesUtils.getScriptStdoutMessage(9, process.getPid()) + " error!", e);
                    exception = e;
                }
            }
        }

        if (exception != null) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(27), exception);
        }
    }

    /**
     * 移除所有进程
     */
    public void clear() {
        this.map.clear();
    }

}
