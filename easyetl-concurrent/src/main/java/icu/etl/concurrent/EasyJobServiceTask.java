package icu.etl.concurrent;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.TimeWatch;

/**
 * 并发任务的运行环境
 */
public class EasyJobServiceTask implements Runnable, Terminate {
    private final static Log log = LogFactory.getLog(EasyJobServiceTask.class);

    /** 回调接口 */
    private EasyJobServiceImpl service;

    /** 并发任务 */
    private EasyJob job;

    /**
     * 初始化
     *
     * @param service 回调接口
     * @param easyJob 并发任务
     */
    public EasyJobServiceTask(EasyJobServiceImpl service, EasyJob easyJob) {
        super();
        this.service = Ensure.notNull(service);
        this.job = Ensure.notNull(easyJob);
    }

    public void run() {
        try {
            this.execute();
        } finally {
            if (this.service != null) {
                EasyJobServiceImpl service = this.service;
                this.service = null;
                service.executeNextJob();
            }
        }
    }

    /**
     * 执行并发任务
     */
    protected void execute() {
        TimeWatch watch = new TimeWatch();
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("concurrent.job.executor.starter.message", this.job.getName()));
        }

        try {
            this.job.execute();
        } catch (Throwable e) {
            String message = ResourcesUtils.getMessage("concurrent.job.executor.finish.right3", this.job.getName(), watch.useTime());
            if (log.isErrorEnabled()) {
                log.error(message, e);
            }
            this.service.writeError(this.job.getName(), message, e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("concurrent.job.executor.finish.right2", this.job.getName(), watch.useTime()));
            }
        }
    }

    public void terminate() {
        if (this.job != null && !this.job.isTerminate()) {
            this.job.terminate();
        }
    }

    public boolean isTerminate() {
        return this.job != null && this.job.isTerminate();
    }

}
