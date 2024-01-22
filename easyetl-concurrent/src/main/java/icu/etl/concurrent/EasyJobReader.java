package icu.etl.concurrent;

import java.io.Closeable;

/**
 * 并发任务输入流
 *
 * @author jeremy8551@qq.com
 */
public interface EasyJobReader extends Closeable, Terminate {

    /**
     * 判断是否可以读取下一个任务
     *
     * @return 返回 true 表示 {@linkplain #next()} 方法可以返回一个可用的任务对象
     */
    boolean hasNext() throws Exception;

    /**
     * 返回一个新的任务
     *
     * @return 任务
     */
    EasyJob next() throws Exception;

}
