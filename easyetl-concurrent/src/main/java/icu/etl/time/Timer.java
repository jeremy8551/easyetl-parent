package icu.etl.time;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 定时器 <br>
 * 在指定时间执行任务或循环执行任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-03
 */
public class Timer {
    private final static Log log = LogFactory.getLog(Timer.class);

    /**
     * 在指定的时间执行某个任务（非循环任务），如果已经错过了开始执行时间则立即执行 schedule(task,time)
     */
    public final static int SCHEDULE_AT_TIME = 1;

    /**
     * 在指定的时间开始间隔循环执行某个任务，如果已经错过了开始执行时间则立即执行 schedule(task,time,period)
     */
    public final static int SCHEDULE_AT_TIME_LOOP = 2;

    /**
     * 在指定延迟时间后马上执行某个任务（非循环任务） schedule(task,delay)
     */
    public final static int SCHEDULE_DELAY = 3;

    /**
     * 在指定延迟时间后开始间隔循环执行某个任务 schedule(task,delay,period)
     */
    public final static int SCHEDULE_DELAY_LOOP = 4;

    /**
     * 添加定时任务成功
     */
    public final static int START_SUCCESS = 0;

    /**
     * 定时任务未通过校验: 定时任务为null
     */
    public final static int START_UNCHECK_NULL = 10;

    /**
     * 定时任务未通过校验: 定时任务执行模式不合法
     */
    public final static int START_UNCHECK_SCHEDULE = 11;

    /**
     * 定时任务未通过校验: 未设置定时任务id
     */
    public final static int START_UNCHECK_TASKID = 12;

    /**
     * 定时任务未通过校验: 定时任务正在执行
     */
    public final static int START_UNCHECK_RUNNING = 13;

    /**
     * 定时任务未通过校验: 定时任务已取消
     */
    public final static int START_UNCHECK_CANCEL = 14;

    /**
     * 定时任务在定时器中已存在且未取消
     */
    public final static int START_NOT_CANCEL = 2;

    /**
     * 定时任务在定时器中已经存在且正在运行
     */
    public final static int START_IS_RUNNING = 3;

    /**
     * 添加定时任务抛出异常错误
     */
    public final static int START_EXCEPTION = 4;

    /**
     * true表示计时器已启动 false表示计时器已终止
     */
    protected volatile boolean start;

    /**
     * 定时任务队列
     */
    protected TimerTaskQueue queue;

    /**
     * 初始化定时器
     */
    public Timer() {
        this(10);
    }

    /**
     * 初始化定时器
     *
     * @param init 初始任务数
     */
    public Timer(int init) {
        this.setStartOrStop(false);
        this.queue = new TimerTaskQueue(this, init);
    }

    /**
     * 启动定时器
     */
    public synchronized void start() {
        if (this.isStart()) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("timer.standard.output.msg001"));
        }

        this.setStartOrStop(true);
    }

    /**
     * 终止定时器并取消所有任务 <br>
     * 如定时时器已经终止则会自动退出 <br>
     * 定时器会取消所有任务，如果任务正在运行则等运行完毕后推出 <br>
     *
     * @param isForce 是否强制终止任务 <br>
     *                true:立即取消并终止所有任务 false:取消所有任务（等待运行任务执行完毕）
     */
    public synchronized void stop(boolean isForce) {
        if (this.isStop()) {
            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg002"));
            }
            this.queue.cancelAllTasks(); // 取消所有任务

            if (isForce) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("timer.standard.output.msg003"));
                }
                this.queue.killRunningTask();
            }

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg004"));
            }
            while (this.queue.getRunningTaskSize() > 0) {
            }

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg005"));
            }
            this.queue.removeTask();

            this.setStartOrStop(false);
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg006"));
            }
        }
    }

    /**
     * 添加定时任务
     *
     * @param list 定时任务集合
     * @return 参考：<br>
     * <code>{@link Timer#START_SUCCESS }</code><br>
     * <code>{@link Timer#START_UNCHECK_NULL }</code><br>
     * <code>{@link Timer#START_UNCHECK_SCHEDULE} </code><br>
     * <code>{@link Timer#START_UNCHECK_TASKID }</code><br>
     * <code>{@link Timer#START_UNCHECK_RUNNING }</code><br>
     * <code>{@link Timer#START_UNCHECK_CANCEL }</code><br>
     * <code>{@link Timer#START_EXCEPTION }</code><br>
     */
    public synchronized int[] addTask(Collection<TimerTask> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        }

        int size = list.size();
        int[] array = new int[size];
        int index = 0;
        Iterator<TimerTask> it = list.iterator();
        while (it.hasNext()) {
            TimerTask task = it.next();
            if (index >= 0 && index < array.length) {
                array[index] = this.addTask(task);
            }
            index++;
        }
        return array;
    }

    /**
     * 添加定时任务
     *
     * @param task 定时任务
     * @return 参考：<br>
     * <code>{@link Timer#START_SUCCESS }</code><br>
     * <code>{@link Timer#START_UNCHECK_NULL }</code><br>
     * <code>{@link Timer#START_UNCHECK_SCHEDULE }</code><br>
     * <code>{@link Timer#START_UNCHECK_TASKID }</code><br>
     * <code>{@link Timer#START_UNCHECK_RUNNING }</code><br>
     * <code>{@link Timer#START_UNCHECK_CANCEL }</code><br>
     * <code>{@link Timer#START_EXCEPTION }</code><br>
     */
    public synchronized int addTask(TimerTask task) {
        if (isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        }
        if (task == null) {
            return Timer.START_UNCHECK_NULL;
        }
        if (StringUtils.isBlank(task.getTaskId())) {
            return Timer.START_UNCHECK_TASKID;
        }
        if (!Timer.checkSchedule(task.getSchedule())) {
            return Timer.START_UNCHECK_SCHEDULE;
        }
        if (task.isRunning()) {
            return Timer.START_UNCHECK_RUNNING;
        }
        if (task.isCancel()) {
            return Timer.START_UNCHECK_CANCEL;
        }

        String taskId = task.getTaskId();
        TimerTask old = this.queue.getTask(taskId);
        if (old != null) {
            if (!old.isCancel()) {
                return Timer.START_NOT_CANCEL;
            } else if (old.isRunning()) {
                return Timer.START_IS_RUNNING;
            } else {
                this.queue.removeTask(taskId); // 如果任务已取消且当前没有运行，则移除这个任务
            }
        }

        if (task.start()) {
            this.queue.addTask(task);
            return Timer.START_SUCCESS;
        } else {
            return Timer.START_EXCEPTION;
        }
    }

    /**
     * 检查定时任务模式是否正确
     *
     * @param schedule 定时任务模式
     * @return true表示正确 false表示错误
     */
    protected static boolean checkSchedule(int schedule) {
        switch (schedule) {
            case Timer.SCHEDULE_AT_TIME:
            case Timer.SCHEDULE_DELAY:
            case Timer.SCHEDULE_AT_TIME_LOOP:
            case Timer.SCHEDULE_DELAY_LOOP:
                // case Timer.SCHEDULE_AT_TIME_LOOP_FIX:
                // case Timer.SCHEDULE_DELAY_LOOP_FIX:
                return true;

            default:
                return false;
        }
    }

    /**
     * 取消定时任务 <br>
     * 如果定时器没有执行定时任务,则以后不再执行 <br>
     * 如果定时任务正在执行,则待任务执行完毕后以后不再执行 <br>
     * 可以反复取消任务,第一次取消任务将调用cancel()函数,以后再次调用时不再做任何操作直接返回取消任务成功 <br>
     *
     * @param taskId 定时任务id
     */
    public synchronized TimerTask cancelTask(String taskId) {
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        } else {
            return this.queue.cancelTask(taskId);
        }
    }

    /**
     * 取消并从定时器中移除任务 <br>
     * 如果当前任务正在执行，则会一直等待定时任务结束
     *
     * @param taskId 定时任务id
     * @return 0表示取消任务成功 -1表示任务不存在
     */
    public synchronized TimerTask removeTask(String taskId) {
        if (isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        }

        TimerTask task = this.queue.cancelTask(taskId);
        if (task != null) {
            this.queue.removeTask(taskId);

            task.waitThreading();
            task.waitRunning();

            task.setQueue(null);
            task.uncancel();
        }
        return task;
    }

    /**
     * 搜索任务
     *
     * @param taskId 定时任务id
     * @return null表示定时任务不存在
     */
    public synchronized TimerTask findTask(String taskId) {
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        } else {
            return queue.getTask(taskId);
        }
    }

    /**
     * 返回所有任务
     *
     * @return 任务集合
     */
    public synchronized List<TimerTask> getTasks() {
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        } else {
            return this.queue.getTask();
        }
    }

    /**
     * 终止任务
     *
     * @param taskId 任务id
     */
    public synchronized void killTask(String taskId) {
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        } else {
            this.queue.killTask(taskId);
        }
    }

    /**
     * 清空所有已取消的任务
     */
    public synchronized void clearCancelTask() {
        if (this.isStop()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg007"));
        } else {
            this.queue.clearCancel();
        }
    }

    /**
     * 判断定时任务是否已启动
     *
     * @return 返回true表示定时器已启动
     */
    public boolean isStart() {
        return this.start;
    }

    /**
     * 判断定时任务是否已停止
     *
     * @return 返回true表示定时器已停止
     */
    public boolean isStop() {
        return !this.start;
    }

    /**
     * true表示定时器已启动 false表示定时器已关闭
     *
     * @param start true表示启动
     */
    protected void setStartOrStop(boolean start) {
        this.start = start;
    }

}
