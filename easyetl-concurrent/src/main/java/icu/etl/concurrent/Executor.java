package icu.etl.concurrent;

import icu.etl.util.ResourcesUtils;
import icu.etl.util.Terminate;
import icu.etl.util.TerminateObserver;
import icu.etl.util.TimeWatch;

/**
 * 并发任务的模板
 */
public abstract class Executor implements Runnable, Terminate {

    /** 日志输出接口 */
    protected ExecutorLogger log;

    /** 线程运行的计时器 */
    protected TimeWatch watch;

    /** 任务运行的线程对象 */
    private Thread thread;

    /** 回调接口 */
    private ExecutorContainer handler;

    /** 错误信息 */
    private String errorMsg;

    /** 错误异常信息 */
    private Throwable exception;

    /** 线程已经启动 true */
    private volatile boolean alreadyStart;

    /** 线程执行结束 true */
    private volatile boolean alreadyStop;

    /** true-表示发生错误 false-无错误 */
    private volatile boolean isError;

    /** true 表示终止任务运行 */
    protected volatile boolean terminate;

    /** 任务名 */
    private String name;

    /** 观察者 */
    protected TerminateObserver observers;

    /**
     * 初始化
     */
    public Executor() {
        super();
        this.isError = false;
        this.errorMsg = "";
        this.alreadyStart = false;
        this.watch = new TimeWatch();
        this.log = new ExecutorLogger();
        this.thread = new Thread(this);
        this.observers = new TerminateObserver();
        this.setName(String.valueOf(this.thread.getId()));
    }

    /**
     * 启动任务等待执行
     */
    public synchronized void start() {
        if (this.alreadyStart) {
            return;
        } else {
            this.alreadyStart = true;
            this.alreadyStop = false;
        }

        this.thread.start();
    }

    public void run() {
        this.watch.start();
        try {
            this.execute();
        } catch (Throwable e) {
            this.setError(true);
            this.setErrorMessage(ResourcesUtils.getTaskMessage(4, this.getName(), this.getId(), this.watch.useTime()), e);
            log.error(this.getErrorMessage(), e);
        } finally {
            this.alreadyStop = true;
            this.terminate = false;
            if (this.handler != null) {
                this.handler.executeNextThread(this);
                this.handler = null;
            }
        }
    }

    public void terminate() {
        this.terminate = true;
        this.observers.terminate(false);
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * 执行任务
     */
    public abstract void execute() throws Exception;

    /**
     * 返回优先级
     *
     * @return
     */
    public abstract int getPRI();

    /**
     * 返回线程编号
     *
     * @return
     */
    public long getId() {
        return this.thread.getId();
    }

    /**
     * 返回任务名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置任务名
     *
     * @param taskName
     */
    protected void setName(String taskName) {
        this.name = taskName;
    }

    /**
     * 返回 true 表示线程已经启动
     *
     * @return
     */
    public boolean alreadyStart() {
        return this.alreadyStart;
    }

    /**
     * 返回 true 表示线程退出运行
     *
     * @return
     */
    public boolean alreadyStop() {
        return this.alreadyStop;
    }

    /**
     * 返回 true 表示运行发生错误 <br>
     *
     * @return
     */
    public boolean alreadyError() {
        return this.isError;
    }

    /**
     * 返回运行的错误信息
     *
     * @return
     */
    public String getErrorMessage() {
        return this.errorMsg;
    }

    /**
     * 保存运行的错误信息
     *
     * @param message
     * @param e
     */
    public void setErrorMessage(String message, Throwable e) {
        this.errorMsg = message;
        this.exception = e;
    }

    /**
     * 返回异常信息
     *
     * @return
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 设置运行发生错误 <br>
     * true 表示卸数发生错误 <br>
     * false 没有发生错误
     *
     * @param isError
     */
    public void setError(boolean isError) {
        this.isError = isError;
    }

    /**
     * 设置回调接口
     *
     * @param handler
     */
    public void setHandler(ExecutorContainer handler) {
        this.handler = handler;
    }

    /**
     * 返回日志输出接口
     *
     * @return
     */
    public ExecutorLogger getLogger() {
        return this.log;
    }

}
