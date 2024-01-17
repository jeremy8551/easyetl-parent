package icu.etl.concurrent;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/28
 */
public class ThreadSourceImpl implements ThreadSource, Closeable {
    private final static Log log = LogFactory.getLog(ThreadSourceImpl.class);

    /** 线程池 */
    private ExecutorService service;

    /** 是否需要关闭线程池 */
    private boolean autoClose;

    /** 外部线程池工厂 */
    private ExecutorsFactory externalFactory;

    /** 默认线程池工厂 */
    private ExecutorsFactory factory;

    /** 线程工厂 */
    private ThreadFactory threadFactory;

    /** 线程池拒绝策略 */
    private RejectedExecutionHandler executionHandler;

    /** 并发任务运行容器的序号 */
    private int serial;

    /** 核心线程数 */
    private int coreSize;

    /** 最大值 */
    private int maxSize;

    /** 闲置线程的空闲时间 */
    private long aliveTime;

    /** 队列容量 */
    private int queueSize;

    public ThreadSourceImpl() {
        this.coreSize = 12;
        this.maxSize = 40;
        this.aliveTime = 10 * 1000; // 10 second
        this.queueSize = 40;
        this.serial = 0;
        this.autoClose = true;

        // 注册挂钩线程
        Runtime.getRuntime().addShutdownHook(new Thread(new ThreadSourceHook(this)));
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public void setExecutorsFactory(ExecutorsFactory factory) {
        this.externalFactory = factory;
    }

    public ExecutorsFactory getExecutorsFactory() {
        if (this.externalFactory != null) { // 优先使用外部线程池
            return this.externalFactory;
        }

        if (this.factory == null) {
            synchronized (this) {
                if (this.factory == null) {
                    this.factory = new ExecutorsFactoryImpl();
                }
            }
        }
        return factory;
    }

    public ExecutorService getExecutorService() {
        if (this.service == null) {
            synchronized (this) {
                if (this.service == null) {
                    this.service = this.getExecutorsFactory().create( //
                            this.coreSize //
                            , this.maxSize //
                            , this.aliveTime //
                            , TimeUnit.MILLISECONDS //
                            , new LinkedBlockingQueue<Runnable>(this.queueSize) //
                            , this.threadFactory == null ? Executors.defaultThreadFactory() : this.threadFactory //
                            , this.executionHandler == null ? new ThreadPoolExecutor.AbortPolicy() : this.executionHandler);

                    // 如果使用内置工厂创建线程池，则线程池需要关闭
                    this.autoClose = this.externalFactory == null;
                }
            }
        }
        return this.service;
    }

    public EasyJobService getJobService(int n) {
        Ensure.isFromOne(n);
        String id = "JobService" + (++this.serial);
        ExecutorService service = this.getExecutorService();
        return new EasyJobServiceImpl(id, service, n);
    }

    public void execute(EasyScheduleJob job) {
        throw new UnsupportedOperationException();
    }

    public void close() {
        this.externalFactory = null;
        this.factory = null;
        this.threadFactory = null;

        // 关闭线程池
        if (this.autoClose && this.service != null) {
            this.service.shutdown();
        }
        this.service = null;
    }

    /**
     * 挂钩线程任务：关闭线程池
     */
    private static class ThreadSourceHook implements Runnable {

        private ThreadSourceImpl source;

        public ThreadSourceHook(ThreadSourceImpl source) {
            this.source = Ensure.notNull(source);
        }

        public void run() {
            this.source.close();
        }
    }
}
