package icu.etl.time;

import java.util.List;

/**
 * 定时任务队列，用于管理定时任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-05
 */
public class TimerTaskQueue {

    /**
     * 队列所属的定时器
     */
    private Timer timer;

    /**
     * 所有定时任务
     */
    private TaskList all;

    /**
     * 已取消的定时任务队列
     */
    private TaskList cancel;

    /**
     * 正在运行的定时任务队列
     */
    private TaskList running;

    /**
     * 未取消的定时任务队列
     */
    private TaskList idle;

    /**
     * 初始化
     */
    public TimerTaskQueue(Timer timer, int init) {
        this.timer = timer;

        all = new TaskList(init);
        idle = new TaskList(init);
        running = new TaskList(init);
        cancel = new TaskList(init);
    }

    /**
     * 返回定时器
     *
     * @return null表示不存在定时器
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * true表示队列中存在定时器
     *
     * @return
     */
    public boolean existsTimer() {
        return timer != null;
    }

    /**
     * 添加定时任务
     *
     * @param task 定时任务
     */
    public synchronized void addTask(TimerTask task) {
        all.add(task);
        task.setQueue(this);
        this.syncQueue(task);
    }

    /**
     * 搜索定时任务
     *
     * @param taskId 定时任务id
     * @return null表示任务不存在
     */
    public synchronized TimerTask getTask(String taskId) {
        return all.get(taskId);
    }

    /**
     * 所有任务到数组
     */
    public synchronized List<TimerTask> getTask() {
        return all.toList();
    }

    /**
     * 判断定时任务是否已经存在
     *
     * @param taskId 定时任务id
     * @return true表示定时任务已经存在
     */
    public boolean existsTask(String taskId) {
        return all.existsTask(taskId);
    }

    /**
     * 从队列中移除定时任务
     *
     * @param taskId 定时任务
     */
    public synchronized void removeTask(String taskId) {
        if (this.all.existsTask(taskId)) {
            this.all.remove(taskId);
        }

        if (this.idle.existsTask(taskId)) {
            this.idle.remove(taskId);
        }

        if (this.running.existsTask(taskId)) {
            this.running.remove(taskId);
        }

        if (this.cancel.existsTask(taskId)) {
            this.cancel.remove(taskId);
        }
    }

    /**
     * 移除队列中的所有任务
     */
    public synchronized void removeTask() {
        this.all.clear();
        this.idle.clear();
        this.cancel.clear();
        this.running.clear();
    }

    /**
     * 取消定时任务
     *
     * @param taskId 定时任务id
     * @return 被取消的定时任务 null表示任务不存在
     */
    protected synchronized TimerTask cancelTask(String taskId) {
        TimerTask task = this.all.get(taskId);
        if (task != null) {
            task.wakeup();
            task.cancel();
            task.wakeup();
        }
        return task;
    }

    /**
     * 对于正在运行的任务执行 terminate() 函数立即终止继续执行
     */
    public synchronized void killRunningTask() {
        this.running.kill();
    }

    /**
     * 终止任务
     *
     * @param taskId
     */
    public synchronized void killTask(String taskId) {
        TimerTask task = this.all.get(taskId);
        this.all.kill(task);
    }

    /**
     * 移除队列中所有已取消任务
     *
     * @return 所以已取消任务
     */
    public synchronized List<TimerTask> clearCancel() {
        List<TimerTask> list = this.cancel.toList();
        for (TimerTask task : list) {
            this.all.remove(task.getTaskId());
        }
        cancel.clear();
        return list;
    }

    /**
     * 正在运行任务数
     *
     * @return
     */
    public synchronized int getRunningTaskSize() {
        return this.running.size();
    }

    /**
     * 已取消运行任务数
     *
     * @return
     */
    public synchronized int getCancelTaskSize() {
        return this.cancel.size();
    }

    /**
     * 未取消运行任务数
     *
     * @return
     */
    public synchronized int getIdleTaskSize() {
        return this.idle.size();
    }

    /**
     * 返回任务数
     *
     * @return
     */
    public synchronized int getTaskSize() {
        return this.idle.size();
    }

    /**
     * 取消所欲运行的定时任务
     */
    public synchronized void cancelAllTasks() {
        while (this.idle.size() > 0) {
            TimerTask task = this.idle.get(0);
            task.wakeup();
            task.cancel();
            task.wakeup();
        }
    }

    /**
     * 同步队列中的状态保持与定时任务状态一致 <br>
     * 根据定时任务的已取消状态，把定时任务添加到 cancel 队列 <br>
     * 根据定时任务的未取消状态，把定时任务添加到 idle 队列 <br>
     * 根据定时任务的运行状态，把定时任务添加到 running 队列 <br>
     *
     * @param taskId 定时任务
     */
    public synchronized void syncQueue(String taskId) {
        if (taskId == null) {
            return;
        }

        TimerTask task = this.all.get(taskId);
        this.syncQueue(task);
    }

    /**
     * 同步队列中的状态保持与定时任务状态一致
     *
     * @param task 定时任务
     */
    protected synchronized void syncQueue(TimerTask task) {
        if (task != null) {
            if (task.isCancel()) {
                // 已取消任务
                this.cancel.add(task);
                this.idle.remove(task);
            } else {
                // 未取消任务
                this.cancel.remove(task);
                this.idle.add(task);
            }

            if (task.isRunning()) {
                // 正在运行任务
                this.running.add(task);
            } else {
                // 未运行任务
                this.running.remove(task);
            }
        }
    }

}

