package icu.etl.database.load;

//@Bean(builder = DataLoadEngineBuilder.class)
public interface Loader {

    /**
     * 执行数据装载操作，读取目标中的数据写入到数据库表中
     *
     * @param context 上下文信息
     * @throws Exception
     */
    void execute(LoadEngineContext context) throws Exception;

    /**
     * 终止数据装载操作
     */
    void terminate();

}
