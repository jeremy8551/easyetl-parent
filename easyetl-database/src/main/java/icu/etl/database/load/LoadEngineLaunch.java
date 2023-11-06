package icu.etl.database.load;

/**
 * 装数引擎启动条件
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public interface LoadEngineLaunch {

    /**
     * 返回 true 表示卸数任务已准备就绪可以执行
     *
     * @param context 装数引擎上下文信息
     * @return
     */
    boolean ready(LoadEngineContext context);

}
