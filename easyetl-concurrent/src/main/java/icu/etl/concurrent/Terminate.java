package icu.etl.concurrent;

/**
 * 并发任务的终止接口
 */
public interface Terminate {

    /**
     * 判断任务是被已被终止，即：已执行 {@linkplain #terminate()} 方法
     *
     * @return 返回 true 表示任务已被终止, false表示还未终止任务
     */
    boolean isTerminate();

    /**
     * 终止并发任务
     * <p>
     * 任务是否立即停止运行，取决于具体的实现
     */
    void terminate();

}
