package icu.etl.database.export;

/**
 * 用户自定义的卸数监听器
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public interface UserListener {

    /**
     * 返回 true 表示卸数任务已准备就绪可以执行
     *
     * @param context
     * @return
     */
    boolean ready(ExtracterContext context);

    /**
     * 卸数任务运行前执行的逻辑
     *
     * @param context
     */
    void before(ExtracterContext context);

    /**
     * 卸数任务运行发生错误时执行的逻辑
     *
     * @param context
     * @param e
     */
    void catchException(ExtracterContext context, Throwable e);

    /**
     * 卸数任务运行完毕后执行的逻辑
     *
     * @param context
     */
    void after(ExtracterContext context);

    /**
     * 退出卸数任务前执行的逻辑
     *
     * @param context
     */
    void quit(ExtracterContext context);

}
