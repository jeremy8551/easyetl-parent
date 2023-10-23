package icu.etl.database.load;

import icu.etl.concurrent.Executor;
import icu.etl.database.load.inernal.StandardLoadEngineContext;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.log.Log;
import icu.etl.log.STD;

/**
 * 装数引擎
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-03
 */
public class LoadEngine extends Executor implements EasyetlContextAware {

    /** 装数引擎默认日志接口 */
    public static Log out = STD.out;

    /** 加载程序的上下文信息 */
    protected LoadEngineContext context;

    /** 装数引擎 */
    protected Loader loader;

    protected EasyetlContext ioccxt;

    /**
     * 初始化
     */
    public LoadEngine() {
        super();
        this.context = new StandardLoadEngineContext();
    }

    public void set(EasyetlContext context) {
        this.ioccxt = context;
    }

    public void execute() throws Exception {
        String mode = this.context.getAttributes().contains("thread") ? "parallel" : "serial";
        this.loader = this.ioccxt.get(Loader.class, mode);
        this.loader.execute(this.context);
    }

    public void terminate() {
        if (this.loader != null) {
            this.loader.terminate();
        }
    }

    /**
     * 返回数据加载任务的上下文信息
     *
     * @return
     */
    public LoadEngineContext getContext() {
        return this.context;
    }

    public int getPRI() {
        return 0;
    }

}
