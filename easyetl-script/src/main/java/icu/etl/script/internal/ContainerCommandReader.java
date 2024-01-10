package icu.etl.script.internal;

import java.util.ArrayList;
import java.util.List;

import icu.etl.concurrent.EasyJob;
import icu.etl.concurrent.EasyJobReader;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptJob;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.Dates;

/**
 * 容器任务的输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-20
 */
public class ContainerCommandReader implements EasyJobReader {

    private UniversalScriptContext context;
    private UniversalScriptSession session;
    private UniversalScriptStdout stdout;
    private UniversalScriptStderr stderr;

    private EasyJob job;
    private List<UniversalScriptCommand> list;
    private volatile boolean terminate;

    /**
     * 初始化
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param stdout   标准输出流
     * @param stderr   错误输出流
     * @param commands 容器中的命令集合
     */
    public ContainerCommandReader(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, List<UniversalScriptCommand> commands) {
        this.terminate = false;
        this.context = context;
        this.session = session;
        this.stdout = stdout;
        this.stderr = stderr;
        this.list = new ArrayList<UniversalScriptCommand>(commands);
    }

    public synchronized boolean hasNext() throws Exception {
        if (this.job != null) {
            return true;
        }

        while (true) {
            if (this.terminate) {
                return false;
            }

            UniversalScriptAnalysis analysis = this.session.getAnalysis();
            for (int i = 0; !this.terminate && i < this.list.size(); i++) {
                UniversalScriptCommand command = this.list.remove(i);
                UniversalScriptJob job = (UniversalScriptJob) command;

                // 判断命令是否已准备好执行
                if (job.isPrepared(this.session, this.context, this.stdout, this.stderr)) {
                    this.job = job.getJob();
                    this.stdout.println(analysis.replaceShellVariable(this.session, this.context, command.getScript(), true, false, true, false));
                    return true;
                }
            }

            if (this.terminate || this.list.isEmpty()) {
                return false; // 已全部执行完毕
            } else {
                Dates.sleep(2000); // 还有未准备就绪的任务, 等待2秒后再查询是否有准备就绪任务
            }
        }
    }

    public synchronized EasyJob next() throws Exception {
        EasyJob value = this.job;
        if (value == null) {
            if (this.hasNext()) {
                value = this.job;
            }
        }
        this.job = null;
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