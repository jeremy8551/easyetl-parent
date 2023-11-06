package icu.etl.script.session;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

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
import icu.etl.util.FileUtils;
import icu.etl.util.IO;

/**
 * 进程的运行环境
 *
 * @author jeremy8551@qq.com
 */
public class ScriptProcessThread extends Thread {

    /** true表示线程正在执行或已执行过 {@linkplain ScriptProcessThread#run()} 方法 */
    private volatile boolean alreadyRun;

    /** true表示正在执行 {@linkplain ScriptProcessThread#run()} 方法 */
    private volatile boolean running;

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
    public ScriptProcessThread(ScriptProcessEnvironment environment) {
        super();
        this.alreadyRun = false;
        this.running = false;
        this.terminate = false;
        this.environment = environment;
    }

    /**
     * 设置子进程
     *
     * @param process
     */
    public void setObserver(ScriptProcess process) {
        if (process == null) {
            throw new NullPointerException();
        } else {
            this.processInfo = process;
        }
    }

    /**
     * 启动进程
     */
    public synchronized void start() {
        this.alreadyRun = false;
        this.running = false;
        this.terminate = false;
        super.start();
    }

    /**
     * 运行线程
     */
    public void run() {
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
                FileUtils.createFile(logfile); // 创建日志文件

                // 标注信息与错误信息均写入日志文件
                ScriptWriterFactory factory = new ScriptWriterFactory(logfile.getAbsolutePath(), true);
                Writer out = factory.build(session, context);
                cmdout = new ScriptStdout(context.getFactory().getStdoutLog(), out, stdout.getFormatter());
                cmderr = new ScriptStderr(context.getFactory().getStderrLog(), out, stderr.getFormatter());

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
            throw new UniversalScriptException(command == null ? "" : command.getScript(), e);
        } finally {
            this.running = false;
            IO.close(cmdout, cmderr);
        }
    }

    /**
     * true 表示终止后台命令
     *
     * @return 返回 true 表示终止操作执行成功
     * @throws IOException
     * @throws SQLException
     */
    public boolean terminate() throws IOException, SQLException {
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
     * 返回true表示线程正在运行
     *
     * @return
     */
    public boolean alreadyRun() {
        return this.alreadyRun;
    }

    /**
     * 返回 true 表示正在执行 {@linkplain ScriptProcessThread#run()} 方法
     *
     * @return
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * 返回 true 表示进程已被终止
     *
     * @return
     */
    public boolean isTerminate() {
        return this.terminate;
    }

}
