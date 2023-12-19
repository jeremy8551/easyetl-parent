package icu.etl.script.session;

import java.io.File;
import java.io.Writer;

import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.io.ScriptStderr;
import icu.etl.script.io.ScriptStdout;
import icu.etl.script.io.ScriptWriterFactory;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;

/**
 * 脚本引擎进程上运行的并发任务
 *
 * @author jeremy8551@qq.com
 */
public class ScriptProcessJob implements Runnable {
//    private final static Log log = LogFactory.getLog(ScriptProcessJob.class);

    /** true表示线程正在执行或已执行过 {@linkplain ScriptProcessJob#run()} 方法 */
    private volatile boolean alreadyRun;

    /** true表示正在执行 {@linkplain ScriptProcessJob#run()} 方法 */
    private volatile boolean running;

    private volatile boolean alive;

    /** 进程运行环境 */
    private ScriptProcessEnvironment environment;

    /** 进程 */
    private ScriptProcess processInfo;

    /** true 表示终止后台命令 */
    private volatile boolean terminate;

    /**
     * 初始化
     *
     * @param environment 进程的运行环境
     */
    public ScriptProcessJob(ScriptProcessEnvironment environment) {
        super();
        this.alreadyRun = false;
        this.running = false;
        this.terminate = false;
        this.environment = environment;
        this.start();
    }

    /**
     * 设置子线程
     *
     * @param process 子线程
     */
    public void setObserver(ScriptProcess process) {
        this.processInfo = Ensure.notNull(process);
    }

    /**
     * 启动进程
     */
    public synchronized void start() {
        this.alreadyRun = false;
        this.running = false;
        this.terminate = false;
        this.alive = true;
    }

    /**
     * 判断任务是否存活
     *
     * @return 返回true表示线程存活（还在运行）false表示线程已运行完毕
     */
    public boolean isAlive() {
        return this.alive;
    }

    /**
     * 运行线程
     */
    public void run() {
        this.environment.getWaitRun().wakeup(); // 唤醒等待启动的线程

        ScriptStdout cmdout = null;
        ScriptStderr cmderr = null;
        UniversalScriptSession session = this.environment.getSession();
        UniversalScriptContext context = this.environment.getContext();
        UniversalScriptStdout stdout = this.environment.getStdout();
        UniversalScriptStderr stderr = this.environment.getStderr();
        UniversalScriptCommand command = this.environment.getCommand();
        ScriptMainProcess process = session.getMainProcess();
        boolean forceStdout = this.environment.forceStdout();
        File logfile = this.environment.getLogfile();
        try {
            this.alreadyRun = true;
            this.running = true;

            int exitcode = UniversalScriptCommand.COMMAND_ERROR;
            if (this.terminate) {
                exitcode = UniversalScriptCommand.TERMINATE;
            } else {
                FileUtils.assertCreateFile(logfile); // 创建日志文件

                // 标准信息与错误信息均写入日志文件
                ScriptWriterFactory factory = new ScriptWriterFactory(logfile.getAbsolutePath(), true);
                Writer out = factory.build(session, context);
                cmdout = new ScriptStdout(out, stdout.getFormatter());
                cmderr = new ScriptStderr(out, stderr.getFormatter());

                UniversalCommandResultSet result = process.execute(session, context, stdout, stderr, forceStdout, command);
                exitcode = result.getExitcode();
            }

            // 通知进程停止
            this.processInfo.notifyStop(exitcode);
        } catch (Throwable e) {
            if (cmderr == null) {
                stderr.println(command.getScript(), e);
            } else {
                cmderr.println(command.getScript(), e);
            }
            throw new UniversalScriptException(command.getScript(), e);
        } finally {
            this.running = false;
            this.alive = false;
            this.environment.getWaitDone().wakeup();
            IO.close(cmdout, cmderr);
        }
    }

    /**
     * 终止任务
     *
     * @return 返回true表示任务终止操作执行成功
     */
    public boolean terminate() {
        this.terminate = true;
        if (this.environment.getCommand() != null && this.running) {
            try {
                this.environment.getCommand().terminate();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断任务已运行过
     *
     * @return 返回true表示任务已运行 false表示任务还未执行
     */
    public boolean alreadyRun() {
        return this.alreadyRun;
    }

    /**
     * 判断任务是否正在运行
     *
     * @return 返回true表示正在运行任务
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * 判断任务是否已被终止
     *
     * @return 返回true表示任务已被终止 false表示任务未终止
     */
    public boolean isTerminate() {
        return this.terminate;
    }

}
