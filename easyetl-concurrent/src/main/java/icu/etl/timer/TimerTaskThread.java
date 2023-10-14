package icu.etl.timer;

import java.lang.Thread.UncaughtExceptionHandler;

import icu.etl.log.STD;
import icu.etl.util.ResourcesUtils;

/**
 * 运行任务的线程
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-07
 */
public class TimerTaskThread extends Thread implements UncaughtExceptionHandler {

    /**
     * 定时任务
     */
    private TimerTask task;

    /**
     * true表示线程已经进入运行阶段 run() 函数
     */
    private volatile boolean isRunning;

    /**
     * 初始化
     *
     * @param task 定时任务
     */
    public TimerTaskThread(TimerTask task) {
        super();
        this.isRunning = false;
        this.task = task;
        this.setDaemon(false);
        this.setName(this.getTaskThreadName(this.task.getTaskId(), TimerTaskThread.class));
        this.setUncaughtExceptionHandler(this);
    }

    /**
     * 生成任务运行时线程名
     *
     * @param taskId
     * @return
     */
    protected String getTaskThreadName(String taskId, Class<?> clazz) {
        return taskId + "@" + clazz.getSimpleName().toUpperCase();
    }

    public void run() {
        this.isRunning = true;
        try {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getTimerMessage(42, this.getName()));
            }

            loop();

            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getTimerMessage(43, this.getName()));
            }
        } catch (Exception e) {
            if (STD.out.isErrorEnabled()) {
                STD.out.error(ResourcesUtils.getTimerMessage(44, this.getName()), e);
            }

            try {
                this.task.notifyAll();
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        } finally {
            this.isRunning = false;
        }
    }

    /**
     * 循环执行任务
     */
    private void loop() {
        while (!task.isCancel()) {
            long delay = task.getNextRunMillis() - System.currentTimeMillis();
            if (delay < task.getMistake()) { // 已经错过执行时间在不在执行等待下次执行
                task.calcNextRunMillis();
                continue;
            } else if (delay >= task.getMistake() && delay <= 0) { // 在误差范围内直接运行任务
                task.run(); // 运行任务
                if (task.existsPeriod()) { // 循环执行某个定时任务
                    task.calcNextRunMillis();
                    continue;
                } else {
                    task.wakeup();
                    task.cancel(); // 取消任务
                    task.wakeup();
                    continue;
                }
            } else { // 还未到执行时间
                try {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug(ResourcesUtils.getTimerMessage(45, this.getName(), delay));
                    }
                    synchronized (task) {
                        task.wait(delay); // 任务线程进入等待状态
                    }
                } catch (Throwable e) {
                    if (STD.out.isErrorEnabled()) {
                        STD.out.error(ResourcesUtils.getTimerMessage(46, this.getName()), e);
                    }
                }
                continue;
            }
        }
    }

    /**
     * 定时任务
     *
     * @return
     */
    public TimerTask getTask() {
        return task;
    }

    /**
     * true表示正在执行 run 函数
     *
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 取消任务并唤醒定时任务线程
     */
    protected void cancelTask() {
        if (this.task != null) {
            this.task.wakeup();
            this.task.cancel();
            this.task.wakeup();
        }
    }

    /**
     * 线程发生严重错误退出时执行的函数
     */
    public void uncaughtException(Thread t, Throwable e) {
        if (STD.out.isErrorEnabled()) {
            STD.out.error(ResourcesUtils.getTimerMessage(47, t.getName()), e);
        }

        cancelTask(); // 取消任务
    }
}
