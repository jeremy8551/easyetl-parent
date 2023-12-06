package icu.etl.database.load;

/**
 * 数据装载器接口
 */
public interface Loader {

    /**
     * 执行数据装载操作，读取目标中的数据写入到数据库表中
     *
     * @param context 上下文信息
     * @throws Exception 装载数据发生错误
     */
    void execute(LoadEngineContext context) throws Exception;

    /**
     * 终止数据装载操作
     */
    void terminate();

}
