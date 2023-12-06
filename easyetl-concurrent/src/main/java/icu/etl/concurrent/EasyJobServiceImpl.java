package icu.etl.concurrent;

import java.util.concurrent.ExecutorService;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 接口的实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-12
 */
public class EasyJobServiceImpl implements EasyJobService {
    private final static Log log = LogFactory.getLog(EasyJobServiceImpl.class);

    /** 休眠的超时时间, 单位毫秒 */
    public static int SLEEP_TIMEOUT = 2 * 60 * 1000;

    /** 并发任务运行器的编号 */
    private String id;

    /** 输入流 */
    private EasyJobReader reader;

    /** 任务错误信息输出流 */
    private EasyJobWriter writer;

    /** 同时运行线程的最大数 */
    private int count;

    /** 已启动和正在运行的线程数量之和 */
    private EasyCounter alive;

    /** 已启动线程的数量 */
    private EasyCounter start;

    /** 发生错误线程的数量 */
    private EasyCounter error;

    /** true表示终止任务运行 */
    private volatile boolean terminate;

    /** 线程池 */
    private ExecutorService service;

    /** 状态 */
    private final JobStatus state = new JobStatus();

    public EasyJobServiceImpl(String id, ExecutorService service, int count) {
        this.id = Ensure.notNull(id);
        this.service = Ensure.notNull(service);
        this.count = Ensure.isFromOne(count);
        this.terminate = false;

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("concurrent.job.executor.init.message", this.id));
        }
    }

    public void executeForce(EasyJobReader in) {
        EasyJobWriterImpl out = new EasyJobWriterImpl();
        int error = this.execute(in, out);
        if (error > 0) {
            throw out.toException(ResourcesUtils.getMessage("concurrent.job.executor.finish.error", this.id, this.error.get()));
        }
    }

    public void executeForce(EasyJobReader in, EasyJobWriter out) {
        int error = this.execute(in, out);
        if (error > 0) {
            throw new EasyJobException(ResourcesUtils.getMessage("concurrent.job.executor.finish.error", this.id, this.error.get()));
        }
    }

    public int execute(EasyJobReader in) {
        return this.execute(in, null);
    }

    public synchronized int execute(EasyJobReader in, EasyJobWriter out) {
        this.reader = Ensure.notNull(in);
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("concurrent.job.executor.execute.message", this.id, this.count));
        }

        this.writer = out;
        this.alive = new EasyCounter(0);
        this.start = new EasyCounter(0);
        this.error = new EasyCounter(0);
        this.terminate = false;
        try {
            while (this.reader.hasNext() || this.alive.get() > 0) {
                if (this.terminate) {
                    break;
                }

                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("concurrent.job.executor.execute.condition", this.id, this.alive.get(), this.reader.hasNext()));
                }

                while (this.alive.get() < this.count) {
                    if (this.terminate) {
                        break;
                    }

                    if (!this.executeNext()) {
                        break; // 如果读取不到下一个任务
                    }
                }

                if (this.terminate) {
                    break;
                }

                int timeout = SLEEP_TIMEOUT; // 休眠时间, 单位毫秒
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("concurrent.job.executor.sleep.message", this.id, timeout));
                }

                // 因为是多线程并行计算，所以 alive 变量可能是一直在变化中，所以在休眠前再检查一下
                if (this.alive.get() > 0) {
                    this.state.sleep(timeout);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("concurrent.job.executor.unsleep.message", this.id));
                    }
                }
            }

            if (this.error.get() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("concurrent.job.executor.execute.finish.message", this.id));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("concurrent.job.executor.finish.error", this.id, this.error.get()));
                }
            }

            return this.error.get();
        } catch (Throwable e) {
            throw new EasyJobException(e.getLocalizedMessage(), e);
        } finally {
            this.reader = null;
            this.writer = null;
        }
    }

    /**
     * 启动一个并发任务
     *
     * @return 返回true表示成功启动一个并发任务，false表示没有待运行的任务
     */
    private boolean executeNext() throws Exception {
        if (this.reader.hasNext()) {
            EasyJob next = this.reader.next();
            if (next == null) {
                return this.executeNext();
            }

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("concurrent.job.executor.execute.starter.message", next.getName(), this.service.toString()));
            }

            EasyJobServiceTask task = new EasyJobServiceTask(this, next);
            this.service.submit(task);
            this.alive.incrementAndGet();
            this.start.incrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    public void wakeup() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("concurrent.job.executor.wait.message", this.id));
        }
        this.state.wakeup();
    }

    /**
     * 并发任务运行完毕，运行下一个任务
     */
    public void executeNextJob() {
        this.alive.decrementAndGet();// 运行任务数减一
        this.wakeup();
    }

    /**
     * 记录任务错误信息
     *
     * @param name    任务名
     * @param message 错误信息
     * @param e       异常信息
     */
    public void writeError(String name, String message, Throwable e) {
        this.error.incrementAndGet();
        if (this.writer != null) {
            this.writer.addError(name, message, e);
        }
    }

    public String getId() {
        return this.id;
    }

    public int getConcurrency() {
        return this.count;
    }

    public int getAliveJob() {
        return this.alive.get();
    }

    public int getStartJob() {
        return this.start.get();
    }

    public int getErrorJob() {
        return this.error.get();
    }

    public void terminate() {
        this.terminate = true;
        if (this.reader != null) {
            this.reader.terminate();
        }
    }

    public boolean isTerminate() {
        return this.terminate;
    }

}
