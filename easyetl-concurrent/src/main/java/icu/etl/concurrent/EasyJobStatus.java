package icu.etl.concurrent;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;

/**
 * 任务状态，用于唤醒线程或让线程进入等待
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/1
 */
public class EasyJobStatus {
    private final static Log log = LogFactory.getLog(EasyJobStatus.class);

    private final Object lock = new Object();

    public EasyJobStatus() {
    }

    /**
     * 让当前线程进入等待状态
     *
     * @param timeout 超时时间，单位毫秒
     * @return 返回true表示任务进入等待状态成功，false表示发生异常
     */
    public boolean sleep(long timeout) {
        synchronized (this.lock) { // 取得锁
            try {
                this.lock.wait(timeout);
                return true;
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage(), e);
                }
                return false;
            }
        }
    }

    /**
     * 唤醒所有在此对象上等待的线程
     *
     * @return 返回true表示任务进入等待状态成功，false表示发生异常
     */
    public boolean wakeup() {
        synchronized (this.lock) { //上锁
            try {
                this.lock.notifyAll();
                return true;
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getLocalizedMessage(), e);
                }
                return false;
            }
        }
    }
}
