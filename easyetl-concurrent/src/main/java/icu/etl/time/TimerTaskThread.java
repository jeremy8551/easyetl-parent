package icu.etl.time;

import java.lang.Thread.UncaughtExceptionHandler;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ResourcesUtils;

/**
 * 运行任务的线程
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-07
 */
public class TimerTaskThread extends Thread implements UncaughtExceptionHandler {
    private final static Log log = LogFactory.getLog(TimerTaskThread.class);

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
     * @param taskId 任务编号
     * @param clazz  类信息
     * @return 线程名
     */
    protected String getTaskThreadName(String taskId, Class<?> clazz) {
        return taskId + "@" + clazz.getSimpleName().toUpperCase();
    }

    public void run() {
        this.isRunning = true;
        try {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg042", this.getName()));
            }

            loop();

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg043", this.getName()));
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getMessage("timer.standard.output.msg044", this.getName()), e);
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
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("timer.standard.output.msg045", this.getName(), delay));
                    }
                    synchronized (task) {
                        task.wait(delay); // 任务线程进入等待状态
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error(ResourcesUtils.getMessage("timer.standard.output.msg046", this.getName()), e);
                    }
                }
                continue;
            }
        }
    }

    /**
     * 定时任务
     *
     * @return 定时任务
     */
    public TimerTask getTask() {
        return task;
    }

    /**
     * 判断任务是否正在运行
     *
     * @return 返回true表示正在执行 run 函数
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
        if (log.isErrorEnabled()) {
            log.error(ResourcesUtils.getMessage("timer.standard.output.msg047", t.getName()), e);
        }

        cancelTask(); // 取消任务
    }
}
