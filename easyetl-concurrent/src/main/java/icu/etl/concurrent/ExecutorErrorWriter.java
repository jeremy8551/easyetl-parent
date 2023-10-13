package icu.etl.concurrent;

/**
 * 错误信息记录器
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-12
 */
public interface ExecutorErrorWriter extends java.io.Closeable {

    /**
     * 保存异常错误信息
     *
     * @param name    任务名
     * @param message 错误提示信息
     * @param e       异常信息
     */
    void addError(String name, String message, Throwable e);

    /**
     * 判断是否有错误信息
     *
     * @return
     */
    boolean hasError();

}
