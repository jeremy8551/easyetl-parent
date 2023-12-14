package icu.etl.log.apd.file;

import java.util.concurrent.TimeUnit;

import icu.etl.util.Ensure;
import icu.etl.util.JUL;

/**
 * 记录日志任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/25
 */
public class FileAppenderJob implements Runnable {

    private FileAppenderWriter out;

    /** 线程是否正在运行 */
    private volatile boolean running;

    /** 是否终止 */
    private volatile boolean notTerminate;

    public FileAppenderJob(FileAppenderWriter out) {
        super();
        this.out = Ensure.notNull(out);
        this.running = false;
        this.notTerminate = true;
    }

    public void run() {
        this.running = true;
        this.notTerminate = true;
        try {
            while (this.notTerminate) {
                this.out.write();
            }
            this.out.flush();
        } catch (Throwable e) {
            if (JUL.isErrorEnabled()) {
                JUL.error(e.getLocalizedMessage(), e);
            }
        } finally {
            this.running = false;
        }
    }

    /**
     * 让调用方法的线程进入等待状态，等待日志记录线程退出
     */
    public void waitFor() {
        while (this.running) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                if (JUL.isDebugEnabled()) {
                    JUL.debug(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * 终止运行
     */
    public void terminate() {
        this.notTerminate = false;
    }

}
