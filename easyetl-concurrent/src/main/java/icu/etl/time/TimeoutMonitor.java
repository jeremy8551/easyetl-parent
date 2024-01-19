package icu.etl.time;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ResourcesUtils;

/**
 * 用于监视任务是否超时,如果任务A超时自动调用任务A的 termiante函数终止任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-05
 */
public class TimeoutMonitor extends TimerTask {
    private final static Log log = LogFactory.getLog(TimeoutMonitor.class);

    /**
     * 被监视任务
     */
    private TimerTask target;

    /**
     * 初始化
     *
     * @param task 被监视任务
     */
    public TimeoutMonitor(TimerTask task) {
        super();
        this.target = task;
        this.setTimeout(0);
        this.setQueue(null);
        this.setTaskId(TimeoutMonitor.toMonitorTaskId(task.getTaskId()));
        this.setSchedule(Timer.SCHEDULE_DELAY);
        this.setDelay(task.getTimeout());
    }

    public void execute() throws TimerException {
        if (target != null && target.isRunning()) {
            long second = getDelay() / 1000;
            if (log.isWarnEnabled()) {
                log.warn(ResourcesUtils.getMessage("timer.standard.output.msg041", target.getTaskId(), second));
            }
            target.terminate();
        }
    }

    public void terminate() throws TimerException {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成超时监控任务id
     *
     * @param taskId 被监控任务id
     * @return
     */
    public static String toMonitorTaskId(String taskId) {
        return taskId + "@MONITOR@TIMEOUT";
    }

}
