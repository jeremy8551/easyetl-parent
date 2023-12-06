package icu.etl.concurrent;

/**
 * 并发任务接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/29
 */
public interface EasyJob extends Terminate {

    /**
     * 返回任务名
     *
     * @return 任务名
     */
    String getName();

    /**
     * 并发任务计算逻辑
     *
     * @return 返回0表示成功 非0表示失败
     * @throws Exception 并发任务发生错误
     */
    int execute() throws Exception;
}
