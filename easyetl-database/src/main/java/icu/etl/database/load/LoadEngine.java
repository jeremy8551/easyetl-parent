package icu.etl.database.load;

import icu.etl.concurrent.AbstractJob;
import icu.etl.database.load.inernal.LoadEngineContextImpl;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;

/**
 * 数据装载引擎
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-03
 */
public class LoadEngine extends AbstractJob implements EasyContextAware {

    /** 数据装载引擎的上下文信息 */
    protected LoadEngineContext context;

    /** 数据装载器 */
    protected Loader loader;

    /** 容器上下文信息 */
    protected EasyContext ioc;

    /**
     * 初始化
     */
    public LoadEngine() {
        super();
        this.context = new LoadEngineContextImpl();
    }

    public void setContext(EasyContext ioc) {
        this.ioc = ioc;
    }

    public int execute() throws Exception {
        // 经测试发现：使用并发分段读取文件的方式，再批量插入的方式速度并不快，反而低，所以默认使用单线程读取数据文件
        this.loader = this.ioc.getBean(Loader.class, "serial");
        this.loader.execute(this.context);
        return 0;
    }

    public void terminate() {
        if (this.loader != null) {
            this.loader.terminate();
        }
    }

    /**
     * 返回数据装载任务的上下文信息
     *
     * @return 数据装载任务的上下文信息
     */
    public LoadEngineContext getContext() {
        return this.context;
    }

}
