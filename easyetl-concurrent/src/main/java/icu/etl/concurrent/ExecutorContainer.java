package icu.etl.concurrent;

import icu.etl.util.StringUtils;
import icu.etl.util.Terminate;

/**
 * 启动多个线程执行任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-12
 */
public class ExecutorContainer implements Terminate {

    /** 输入流 */
    private ExecutorReader reader;

    /** 任务错误信息输出流 */
    private ExecutorErrorWriter writer;

    /** true表示等待执行 false-初始化完毕/还没有初始化 */
    private volatile boolean waitFor;

    /** 同时运行线程的最大数 */
    private volatile int threads;

    /** 已启动和正在运行的线程数量之和 */
    private volatile int alive;

    /** 已启动线程的数量 */
    private volatile int start;

    /** 发生错误线程的数量 */
    private volatile int error;

    /** true表示终止任务运行 */
    private volatile boolean terminate;

    /**
     * 初始化
     *
     * @param in 任务输入流
     */
    public ExecutorContainer(ExecutorReader in) {
        this.reader = in;
    }

    /**
     * 初始化
     *
     * @param in  任务输入流
     * @param out 错误信息输出流
     */
    public ExecutorContainer(ExecutorReader in, ExecutorErrorWriter out) {
        this.reader = in;
        this.writer = out;
    }

    /**
     * 执行任务
     *
     * @param number 并发的线程数量
     * @return 返回发生错误的任务数 返回0表示没有错误
     */
    public int execute(int number) {
        if (this.reader == null) {
            throw new NullPointerException();
        }
        if (number < 1) {
            throw new IllegalArgumentException(String.valueOf(number));
        }

        this.waitFor = true;
        this.threads = number;
        this.alive = 0;
        this.start = 0;
        this.error = 0;

        // 逐个启动任务
        for (int i = 1; !this.terminate && i <= this.threads; i++) {
            this.executeNextThread();
        }
        this.waitFor = false;

        // 等待所有任务执行完毕
        while (this.alive > 0) {
        }

        this.reader = null;
        this.writer = null;
        return this.error == 0 ? 0 : this.error;
    }

    /**
     * 用于通知线程池启动下一个任务 <br>
     * <br>
     * 任务中的 {@linkplain Executor#execute()} 方法执行完毕后执行的回调函数
     *
     * @param executor 已执行完毕的任务
     */
    protected synchronized void executeNextThread(Executor executor) {
        if (this.terminate) {
            return;
        }

        // 等待初始化完毕再向下执行任务
        while (this.waitFor) {
            if (this.terminate) {
                return;
            }
        }

        // 上一个任务发生错误
        if (executor.alreadyError()) {
            this.error++;
            if (this.writer != null) {
                this.writer.addError(executor.getName(), executor.getErrorMessage(), executor.getException());
            }
        }

        // 执行下一个任务
        try {
            this.executeNextThread();
        } catch (Throwable e) {
            this.error++;
            if (this.writer != null) {
                this.writer.addError(executor.getName(), executor.getErrorMessage(), e);
            }
            throw new RuntimeException(StringUtils.toString(executor), e);
        } finally {
            this.alive--;
        }
    }

    /**
     * 启动下一个任务
     */
    private void executeNextThread() {
        try {
            if (!this.terminate && this.reader.hasNext()) {
                Executor executor = this.reader.next();
                if (executor == null) {
                    this.executeNextThread();
                    return;
                }

                executor.setHandler(this);
                this.start++;
                this.alive++;
                executor.start();
            }
        } catch (Throwable e) {
            throw new RuntimeException("executeNextThread()", e);
        }
    }

    /**
     * 设置任务输入流
     *
     * @param reader
     */
    public void setReader(ExecutorReader reader) {
        this.reader = reader;
    }

    /**
     * 设置错误信息输出流
     *
     * @param writer
     */
    public void setWriter(ExecutorErrorWriter writer) {
        this.writer = writer;
    }

    /**
     * 返回同时执行线程数
     *
     * @return
     */
    public int getThreads() {
        return this.threads;
    }

    /**
     * 返回正在运行（包含运行 {@linkplain Thread#start()} 与 {@linkplain Thread#run()} 方法）线程的数量
     *
     * @return
     */
    public int getAliveNumber() {
        return this.alive;
    }

    /**
     * 返回已经启动线程 {@linkplain Thread#start()} 方法的数量
     *
     * @return
     */
    public int getStartNumber() {
        return this.start;
    }

    /**
     * 发生错误的任务数量
     *
     * @return
     */
    public int getErrorNumber() {
        return this.error;
    }

    /**
     * 是否有错误
     *
     * @return
     */
    public boolean hasError() {
        return this.error > 0;
    }

    /**
     * 终止容器运行
     */
    public void terminate() {
        this.terminate = true;
        if (this.reader != null) {
            this.reader.terminate();
        }
    }

    public boolean isTerminate() {
        return this.terminate;
    }

}
