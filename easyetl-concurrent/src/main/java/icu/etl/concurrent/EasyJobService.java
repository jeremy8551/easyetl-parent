package icu.etl.concurrent;

/**
 * 并发任务容器
 * <p>
 * 指定同时运行的并行任务个数，运行期会将并行任务提交到线程池中运行
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/28
 */
public interface EasyJobService extends Terminate {

    /**
     * 返回容器编号
     *
     * @return 容器编号
     */
    String getId();

    /**
     * 运行任务，如果任务发生错误，则抛出异常
     *
     * @param in 任务输入流
     */
    void executeForce(EasyJobReader in);

    /**
     * 运行任务，如果任务发生错误，则抛出异常
     *
     * @param in  任务输入流
     * @param out 错误信息输出流
     */
    void executeForce(EasyJobReader in, EasyJobWriter out);

    /**
     * 运行任务
     *
     * @param in 任务输入流
     * @return 发生错误的任务个数
     */
    int execute(EasyJobReader in);

    /**
     * 执行任务
     *
     * @param in  并发任务的输入流
     * @param out 错误信息输出流
     * @return 返回发生错误的任务数, 返回0表示没有错误
     */
    int execute(EasyJobReader in, EasyJobWriter out);

    /**
     * 如果并发任务容器已进入休眠，则会唤醒容器
     */
    void wakeup();

    /**
     * 返回同时运行并行任务的个数
     *
     * @return 并发任务数
     */
    int getConcurrency();

    /**
     * 返回正在运行任务数
     *
     * @return 并发任务数
     */
    int getAliveJob();

    /**
     * 返回已经启动的任务数
     *
     * @return 并发任务数
     */
    int getStartJob();

    /**
     * 发生错误的任务数
     *
     * @return 并发任务数
     */
    int getErrorJob();

}
