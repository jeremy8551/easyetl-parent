package icu.etl.concurrent;

import icu.etl.util.Terminate;

/**
 * 任务读取流
 *
 * @author jeremy8551@qq.com
 */
public interface ExecutorReader extends java.io.Closeable, Terminate {

    /**
     * 返回 true 表示 {@linkplain #next()} 方法可以返回一个可用的任务对象
     *
     * @return
     */
    boolean hasNext() throws Exception;

    /**
     * 返回一个新的任务
     *
     * @return
     */
    Executor next() throws Exception;

}
