package icu.etl.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/28
 */
public interface ExecutorServiceFactory {

    /**
     * 创建一个线程池
     *
     * @return 线程池
     */
    ExecutorService create(int corePoolSize, //
                           int maximumPoolSize, //
                           long keepAliveTime, //
                           TimeUnit unit, //
                           BlockingQueue<Runnable> workQueue, //
                           ThreadFactory threadFactory, //
                           RejectedExecutionHandler handler);
}
