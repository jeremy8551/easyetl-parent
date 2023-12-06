package icu.etl.concurrent;

/**
 * 并发任务发生异常
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/28
 */
public class EasyJobException extends RuntimeException {

    public EasyJobException(String message) {
        super(message);
    }

    public EasyJobException(String message, Throwable cause) {
        super(message, cause);
    }

}
