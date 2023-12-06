package icu.etl.log;

import java.util.concurrent.Executor;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/1
 */
public class ExecutorImpl implements Executor {
    protected Thread thread;

    public ExecutorImpl() {
        this(null);
    }

    public ExecutorImpl(Thread thread) {
        this.thread = thread;
    }

    public void execute(Runnable command) {
        if (this.thread == null) {
            this.thread = new Thread(command);
        }
        this.thread.start();
    }

    public Thread getThread() {
        return thread;
    }
}
