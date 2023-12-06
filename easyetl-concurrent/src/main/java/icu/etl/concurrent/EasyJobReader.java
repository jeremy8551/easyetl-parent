package icu.etl.concurrent;

import java.io.Closeable;

/**
 * 并发任务输入流
 *
 * @author jeremy8551@qq.com
 */
public interface EasyJobReader extends Closeable, Terminate {

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
    EasyJob next() throws Exception;

}
