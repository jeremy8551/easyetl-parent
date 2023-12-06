package icu.etl.script.session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import icu.etl.concurrent.ThreadSource;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.util.Ensure;

/**
 * 子进程信息
 *
 * @author jeremy8551@qq.com
 */
public class ScriptProcess {

    /** 序号 */
    private static int number = 20;

    /**
     * 返回进程编号
     *
     * @return
     */
    private static synchronized int getId() {
        return ++number;
    }

    /** 进程编号 */
    private String pid;

    /** 进程起始时间 */
    private Date startTime;

    /** 进程终止时间 */
    private Date endTime;

    /** 进程运行环境 */
    private ScriptProcessEnvironment environment;

    /** 进程运行线程 */
    private ScriptProcessJob scriptJob;

    /** 进程的返回值 */
    private Integer exitcode;

    /** 行号 */
    private long lineNumber;

    /**
     * 初始化
     *
     * @param environment
     * @param scriptJob
     */
    public ScriptProcess(ScriptProcessEnvironment environment, ScriptProcessJob scriptJob) {
        this.pid = String.valueOf(ScriptProcess.getId());
        this.environment = Ensure.notNull(environment);
        this.scriptJob = Ensure.notNull(scriptJob);
        this.scriptJob.setObserver(this);
        this.lineNumber = this.environment.getSession().getCompiler().getLineNumber();
    }

    /**
     * 启动进程
     */
    public void start() {
        this.startTime = new Date();
        this.environment.getContext().getContainer().getBean(ThreadSource.class).getExecutorService().submit(this.scriptJob);
    }

    /**
     * 通知进程停止运行并保存返回值
     *
     * @param exitcode
     */
    public void notifyStop(Integer exitcode) {
        this.exitcode = exitcode;
        if (exitcode != null) {
            this.endTime = new Date();
        }
    }

    /**
     * 终止进程
     *
     * @return 返回 true 表示终止操作执行成功
     * @throws IOException
     * @throws SQLException
     */
    public boolean terminate() throws IOException, SQLException {
        return this.scriptJob.terminate();
    }

    /**
     * 返回 true 表示进程已被终止
     *
     * @return
     */
    public boolean isTerminate() {
        return this.scriptJob.isTerminate();
    }

    /**
     * 返回 true 表示进程正在运行
     *
     * @return
     */
    public boolean isAlive() {
        return this.scriptJob.isAlive() || this.scriptJob.isRunning();
    }

    /**
     * 返回 true 表示进程还未开始执行, 返回 false 表示进程已终止或已开始执行
     *
     * @return
     */
    public boolean waitFor() {
        return this.scriptJob.isAlive() && !this.scriptJob.alreadyRun();
    }

    /**
     * 进程编号
     *
     * @return
     */
    public String getPid() {
        return pid;
    }

    /**
     * 返回后台线程运行环境
     *
     * @return 运行环境信息
     */
    public ScriptProcessEnvironment getEnvironment() {
        return environment;
    }

    /**
     * 进程执行的命令
     *
     * @return
     */
    public UniversalScriptCommand getCommand() {
        return this.environment.getCommand();
    }

    /**
     * 进程的返回值
     *
     * @return
     */
    public Integer getExitcode() {
        return exitcode;
    }

    /**
     * 进程起始时间
     *
     * @return
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * 进程结束时间
     *
     * @return
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * 返回行号
     *
     * @return
     */
    public long getLineNumber() {
        return this.lineNumber;
    }

}
