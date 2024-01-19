package icu.etl.time;

import java.util.Date;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 定时任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-04
 */
public abstract class TimerTask implements Runnable {
    private final static Log log = LogFactory.getLog(TimerTask.class);

    /**
     * 系统默认参数值
     */
    public final static long DEFAULT_PARAM_VALUE = -999;

    /**
     * 系统默认误差毫秒数
     */
    public static long DEFAULT_MISTAKE = 3 * 1000;

    /**
     * 定时任务编号（必须唯一）
     */
    private String taskId;

    /**
     * 定时任务所在的定时器，为null表示没有加入定时器
     */
    private TimerTaskQueue queue;

    /**
     * true表示任务正在运行
     */
    private volatile boolean isRunning;

    /**
     * true表示任务已取消不再执行
     */
    private volatile boolean isCancel;

    /**
     * true表示终止任务
     */
    protected volatile boolean terminate;

    /**
     * 定时执行方式
     */
    private int schedule;

    /**
     * 超时时间（单位毫秒）,任务运行超时后自动调用 terminate() 函数终止任务. 等于0表示没有设置超时时间
     */
    private long timeout;

    /**
     * 定时开始执行时间
     */
    private long startTime;

    /**
     * 任务循环执行的周期（单位毫秒）: 0表示没有间隔时间直接执行第二次任务 -1表示不循环执行任务
     */
    private long period;

    /**
     * 任务延迟执行的时间（单位毫秒）: 0表示没有延迟直接执行
     */
    private long delay;

    /**
     * 误差（已超过下次执行时间时，如果在误差范围内则直接执行任务，如果不在误差范围内则不再执行任务）
     */
    private long mistake;

    /**
     * 本次任务执行时间（毫秒）
     */
    private long nextRunMillis;

    /**
     * 任务执行线程
     */
    private Thread runThread;

    /**
     * 任务执行次数（execute函数执行的次数）
     */
    private int runTimes;

    /**
     * 构造函数
     */
    public TimerTask() {
        super();
        this.isRunning = false;
        this.isCancel = false;
        this.timeout = 0;
        this.mistake = -TimerTask.DEFAULT_MISTAKE;
        this.startTime = DEFAULT_PARAM_VALUE;
        this.period = DEFAULT_PARAM_VALUE;
        this.delay = DEFAULT_PARAM_VALUE;
        this.runTimes = 0;
    }

    /**
     * 启动定时任务 <br>
     * 任务真正的开始执行时间可能与执行 start() 函数不一致，原因如下: <br>
     * 执行 start() 函数后程序会启动任务的线程，但是线程的启动时间依赖于java虚拟机安排 <br>
     */
    public boolean start() {
        if (isRunning()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg048", this.getTaskId()));
            }
            return false;
        }

        if (isCancel()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg049", this.getTaskId()));
            }
            return false;
        }

        if (this.runThread != null) { // 如果线程已经存在
            if (this.runThread instanceof TimerTaskThread) {
                TimerTaskThread thread = (TimerTaskThread) this.runThread;
                if (thread.isRunning()) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("timer.standard.output.msg050", this.getTaskId()));
                    }
                    return false;
                }
            }

            if (this.runThread.isAlive()) { // 如果线程已经执行 start
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("timer.standard.output.msg051", this.getTaskId()));
                }
                return false;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("timer.standard.output.msg013", this.getTaskId()));
        }

        calcNextRunMillis();
        this.runThread = createRunThread();
        this.runThread.start();
        return true;
    }

    /**
     * 创建任务执行线程
     *
     * @return
     */
    protected Thread createRunThread() {
        return new TimerTaskThread(this);
    }

    /**
     * 返回任务执行线程
     *
     * @return
     */
    protected Thread getRunThread() {
        return runThread;
    }

    /**
     * 计算任务下次执行的时间
     */
    void calcNextRunMillis() {
        if (this.schedule == Timer.SCHEDULE_AT_TIME) {
            if (getScheduleTime() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg014", this.getTaskId()));
            }

            setPeriod(-1);
            setDelay(0);
            setNextRunMillis(getScheduleTime());
            return;
        }

        if (this.schedule == Timer.SCHEDULE_AT_TIME_LOOP) {
            if (getPeriod() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg015"));
            }
            if (getScheduleTime() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg016"));
            }

            long period = getPeriod();
            long nextRunMillis = getScheduleTime();
            long c = System.currentTimeMillis();
            while (nextRunMillis < c) {
                nextRunMillis += period;
            }

            setNextRunMillis(nextRunMillis);
            return;
        }

        if (this.schedule == Timer.SCHEDULE_DELAY) {
            if (getDelay() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg017"));
            }

            setPeriod(-1);
            setNextRunMillis(System.currentTimeMillis() + getDelay());
            return;
        }

        if (this.schedule == Timer.SCHEDULE_DELAY_LOOP) {
            if (getPeriod() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg018"));
            }
            if (getDelay() == TimerTask.DEFAULT_PARAM_VALUE) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg019"));
            }

            if (this.runTimes == 0) {
                // 第一次执行
                setNextRunMillis(System.currentTimeMillis() + getDelay());
            } else {
                setNextRunMillis(System.currentTimeMillis() + getPeriod());
            }
            return;
        } else {
            throw new IllegalArgumentException(String.valueOf(this.schedule));
        }
    }

    /**
     * 执行定时任务
     *
     * @throws TimerException 执行任务发生错误
     */
    public abstract void execute() throws TimerException;

    /**
     * 从 execute() 函数中立即退出 <br>
     * 实现代码需要注意： 必须保证 terminate() 函数可重复被执行，用于终止 execute() 函数. <br>
     *
     * @throws TimerException 退出任务发生错误
     */
    public abstract void terminate() throws TimerException;

    /**
     * true 表示正在终止任务
     *
     * @return
     */
    public boolean isTerminate() {
        return terminate;
    }

    public void run() {
        main();
    }

    /**
     * 执行内容
     */
    protected void main() {
        this.isRunning = true;
        String taskId = this.getTaskId();
        TimeoutMonitor monitor = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg020", taskId));
            }

            if (existsQueue()) {
                getQueue().syncQueue(this);
            }

            monitor = startTimeoutMonitor();

            execute();

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg021", taskId));
            }
        } catch (Throwable e) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg022", taskId));
        } finally {
            this.runTimes++;
            this.isRunning = false;
            if (existsQueue()) {
                getQueue().syncQueue(this);
            }

            stopTimeoutMonitor(monitor);
        }
    }

    /**
     * 创建并启动超时监视器
     *
     * @return null表示未设置超时时间
     */
    protected synchronized TimeoutMonitor startTimeoutMonitor() {
        if (existsTimeout()) {
            TimeoutMonitor monitor = new TimeoutMonitor(this);
            monitor.start();
            return monitor;
        } else {
            return null;
        }
    }

    /**
     * 关闭超时监视器
     */
    protected synchronized void stopTimeoutMonitor(TimeoutMonitor monitor) {
        try {
            if (monitor != null) {
                monitor.wakeup();
                monitor.cancel();
                monitor.wakeup();
                monitor.waitRunning();
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getMessage("timer.standard.output.msg023", this.getTaskId()), e);
            }
        }
    }

    /**
     * 取消任务（可多次执行） <br>
     * 如果执行 cancel() 函数前任务未取消则返回true <br>
     * 如果执行 cancel() 函数前任务已取消则返回false <br>
     *
     * @return true第一次执行取消任务 false表示任务已取消
     */
    public synchronized boolean cancel() {
        if (this.isCancel) {
            return false;
        } else {
            this.isCancel = true;
            if (existsQueue()) {
                getQueue().syncQueue(this);
            }

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("timer.standard.output.msg024", this.getTaskId()));
            }
            return true;
        }
    }

    /**
     * 唤醒定时任务上所有等待的线程
     */
    protected synchronized void wakeup() {
        try {
            synchronized (this) {
                notifyAll();
            }
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getMessage("timer.standard.output.msg025", this.getTaskId()), e);
            }
        }
    }

    /**
     * 终止任务线程
     */
    protected synchronized void interruptRunThread() {
        Thread thread = getRunThread();
        if (thread != null) {
            try {
                thread.interrupt();
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(ResourcesUtils.getMessage("timer.standard.output.msg026", this.getTaskId()), e);
                }
            }
        }
    }

    /**
     * 超时时间（单位毫秒）
     *
     * @return 等于0表示没有设置超时时间
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * true表示存在超时设置
     *
     * @return
     */
    public boolean existsTimeout() {
        return timeout != 0;
    }

    /**
     * 定时任务编号（必须唯一,大小写不敏感）
     *
     * @return
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * true表示任务正在运行
     *
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * true表示任务已取消，不再执行
     *
     * @return
     */
    public boolean isCancel() {
        return isCancel;
    }

    /**
     * true表示已添加到队列
     *
     * @return
     */
    public boolean existsQueue() {
        return this.queue != null;
    }

    /**
     * 返回定时器参数 time
     *
     * @return
     */
    public long getScheduleTime() {
        return startTime;
    }

    /**
     * 返回定时器参数 period （单位是毫秒）
     *
     * @return 0表示没有间隔时间直接执行第二次任务 -1表示不循环执行任务
     */
    public long getPeriod() {
        return period;
    }

    /**
     * true表示存在循环周期
     *
     * @return
     */
    public boolean existsPeriod() {
        return this.period != -1;
    }

    /**
     * 返回定时器参数 delay （单位是毫秒）
     *
     * @return
     */
    public long getDelay() {
        return delay;
    }

    /**
     * 任务队列
     *
     * @return
     */
    public TimerTaskQueue getQueue() {
        return queue;
    }

    /**
     * 定时任务执行模式
     *
     * @return 参考<br>
     * <code>{@link Timer#SCHEDULE_AT_TIME}</code> <br>
     * <code>{@link Timer#SCHEDULE_DELAY}</code> <br>
     * <code>{@link Timer#SCHEDULE_AT_TIME_LOOP}</code> <br>
     * <code>{@link Timer#SCHEDULE_DELAY_LOOP}</code> <br>
     */
    public int getSchedule() {
        return schedule;
    }

    /**
     * 设置定时任务执行模式
     *
     * @param schedule 参考<br>
     *                 <code>{@link Timer#SCHEDULE_AT_TIME}</code> <br>
     *                 <code>{@link Timer#SCHEDULE_DELAY}</code> <br>
     *                 <code>{@link Timer#SCHEDULE_AT_TIME_LOOP}</code> <br>
     *                 <code>{@link Timer#SCHEDULE_DELAY_LOOP}</code> <br>
     */
    public void setSchedule(int schedule) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }
        if (!Timer.checkSchedule(schedule)) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg028", schedule));
        }

        this.schedule = schedule;
    }

    /**
     * 设置定时任务循环运行的间隔时间（单位是毫秒）
     *
     * @param period 0表示没有间隔时间直接执行第二次任务 -1表示不循环执行任务
     */
    public void setPeriod(long period) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }
        if (period < -1) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg029", period));
        }

        this.period = period;
    }

    /**
     * 设置定时任务延迟执行时间（单位是毫秒）
     *
     * @param delay 0表示不延迟执行任务
     */
    public void setDelay(long delay) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }
        if (delay < 0) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg030", delay));
        }

        this.delay = delay;
    }

    /**
     * 设置定时任务执行时间
     *
     * @param time
     */
    public void setScheduleTime(long time) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }
        if (time < 0) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg031", time));
        }

        this.startTime = time;
    }

    /**
     * 设置任务运行的超时时间（单位毫秒）<br>
     * 任务运行超时后自动调用 terminate() 函数终止任务
     *
     * @param timeout 超时时间（单位毫秒）等于0表示没有设置超时时间
     */
    public void setTimeout(long timeout) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }
        if (timeout < 0) {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("timer.standard.output.msg032", timeout));
        }

        this.timeout = timeout;
    }

    /**
     * 定时任务编号（必须唯一,大小写不敏感）
     *
     * @param taskId 定时任务ID
     */
    public void setTaskId(String taskId) {
        if (existsQueue()) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg027", this.getTaskId()));
        }

        if (StringUtils.isBlank(taskId)) {
            throw new NullPointerException();
        } else {
            this.taskId = StringUtils.removeBlank(taskId);
        }
    }

    /**
     * 设置定时任务所在的定时器
     *
     * @param queue
     */
    void setQueue(TimerTaskQueue queue) {
        this.queue = queue;
    }

    void uncancel() {
        this.isCancel = false;
    }

    /**
     * 等待定时任务执行完毕
     */
    protected void waitRunning() {
        while (isRunning) {
        }
    }

    /**
     * 等待定时任务线程执行完毕
     */
    protected void waitThreading() {
        this.wakeup();

        int type = 0;
        TimeWatch watch = new TimeWatch();
        TimerTaskThread thread = (TimerTaskThread) this.getRunThread();
        while (thread.isAlive() || thread.isRunning()) {
            if (thread.isRunning()) {
                if (type != 1) {
                    type = 1;
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("timer.standard.output.msg052", this.getTaskId()));
                    }
                }
            } else {
                if (type != 2) {
                    type = 2;
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("timer.standard.output.msg053", this.getTaskId()));
                    }
                }
            }

            if (watch.useSeconds() > 1) {
                try {
                    this.wakeup();
                } catch (Exception e) {
                }
                watch.start();
            }
        }
    }

    /**
     * 返回任务下次执行时间
     *
     * @return
     */
    public long getNextRunMillis() {
        return nextRunMillis;
    }

    /**
     * 设置任务下次执行时间
     *
     * @param nextRunMillis
     */
    void setNextRunMillis(long nextRunMillis) {
        this.nextRunMillis = nextRunMillis;
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("timer.standard.output.msg033", this.getTaskId(), Dates.format19(new Date(this.nextRunMillis))));
        }
    }

    /**
     * 误差（“当前java虚拟机时间戳” 减 “任务的下次执行时间”，如果在误差范围内则直接执行任务，如果不在误差范围内则不再执行任务）
     *
     * @return 0表示必须在制定时间内执行任务
     */
    public long getMistake() {
        return mistake;
    }

    /**
     * 误差（当前执行时间剪掉下次执行时间，如果在误差范围内则直接执行任务，如果不在误差范围内则不再执行任务）
     *
     * @param mistake 0表示必须在指定的时间执行任务
     */
    public void setMistake(long mistake) {
        mistake = mistake < 0 ? -mistake : mistake; // 取绝对值
        if (mistake <= TimerTask.DEFAULT_MISTAKE) {
            throw new TimerException(ResourcesUtils.getMessage("timer.standard.output.msg034", this.getTaskId(), TimerTask.DEFAULT_MISTAKE));
        }

        this.mistake = -mistake;
    }

    /**
     * 任务执行次数（execute函数执行的次数）
     *
     * @return
     */
    public int getRunTimes() {
        return runTimes;
    }

    /**
     * true表示两个定时任务是同一个任务
     */
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof TimerTask)) {
            TimerTask task = (TimerTask) obj;
            if (this.getTaskId().equalsIgnoreCase(task.getTaskId())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

}
