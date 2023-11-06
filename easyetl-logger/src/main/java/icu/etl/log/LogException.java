package icu.etl.log;

/**
 * 日志模块发生错误
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/29
 */
public class LogException extends RuntimeException {

    public LogException() {
        super();
    }

    public LogException(String message) {
        super(message);
    }

    public LogException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogException(Throwable cause) {
        super(cause);
    }
}
