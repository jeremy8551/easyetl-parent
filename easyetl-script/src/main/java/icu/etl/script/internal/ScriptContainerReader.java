package icu.etl.script.internal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.concurrent.Executor;
import icu.etl.concurrent.ExecutorReader;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptThread;
import icu.etl.script.command.ContainerCommand;
import icu.etl.time.Timer;

/**
 * 容器任务的输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-20
 */
public class ScriptContainerReader implements ExecutorReader {

    private UniversalScriptContext context;
    private UniversalScriptSession session;
    private UniversalScriptStdout stdout;
    private UniversalScriptStderr stderr;

    private Executor executor;
    private ContainerCommand container;
    private List<UniversalScriptCommand> list;
    private volatile boolean terminate;

    /**
     * 初始化
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param container
     */
    public ScriptContainerReader(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, ContainerCommand container) {
        this.terminate = false;
        this.context = context;
        this.session = session;
        this.stdout = stdout;
        this.stderr = stderr;
        this.container = container;
        this.list = new ArrayList<UniversalScriptCommand>(container.getList());
    }

    public synchronized boolean hasNext() throws IOException, SQLException {
        this.executor = null;
        while (true) {
            if (this.terminate) {
                return false;
            }

            UniversalScriptAnalysis analysis = this.session.getAnalysis();
            for (int i = 0; !this.terminate && i < this.list.size(); i++) {
                UniversalScriptCommand command = this.list.remove(i);
                UniversalScriptThread thread = (UniversalScriptThread) command;

                // 判断命令是否已准备好执行
                if (thread.start(this.session, this.context, this.stdout, this.stderr, this.container)) {
                    this.executor = thread.getExecutor();
                    this.stdout.println(analysis.replaceShellVariable(this.session, this.context, command.getScript(), true, false, true, false));
                    return true;
                }
            }

            if (this.terminate || this.list.isEmpty()) {
                return false; // 已全部执行完毕
            } else {
                Timer.sleep(2000); // 还有未准备就绪的任务, 等待2秒后再查询是否有准备就绪任务
            }
        }
    }

    public synchronized Executor next() throws IOException, SQLException {
        Executor value = this.executor;
        if (value == null) {
            if (this.hasNext()) {
                value = this.executor;
            } else {
                value = null;
            }
        }

        this.executor = null;
        return value;
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void close() {
        this.list.clear();
        this.terminate = false;
    }

}