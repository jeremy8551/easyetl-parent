package icu.etl.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * 线程池接口
 */
public interface ThreadSource {

    /**
     * 设置线程工厂
     *
     * @param threadFactory 线程工厂
     */
    void setThreadFactory(ThreadFactory threadFactory);

    /**
     * 返回线程工厂
     *
     * @return 线程工厂
     */
    ThreadFactory getThreadFactory();

    /**
     * 设置线程池工厂
     *
     * @param factory 线程池工厂
     */
    void setExecutorsFactory(ExecutorServiceFactory factory);

    /**
     * 返回线程池工厂
     *
     * @return 线程池工厂
     */
    ExecutorServiceFactory getExecutorsFactory();

    /**
     * 返回线程池
     *
     * @return 线程池
     */
    ExecutorService getExecutorService();

    /**
     * 返回一个并发任务运行器
     *
     * @param n 并发线程数
     * @return 并发任务运行器
     */
    EasyJobService getJobService(int n);

    /**
     * 启动定时任务
     *
     * @param job 定时任务
     */
    void execute(EasyScheduleJob job);

    /**
     * 关闭线程池
     */
    void close();
}
