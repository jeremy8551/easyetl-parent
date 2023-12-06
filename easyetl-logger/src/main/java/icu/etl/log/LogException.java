package icu.etl.log;

/**
 * 日志错误
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/29
 */
public class LogException extends RuntimeException {

    public LogException(String message, Throwable cause) {
        super(message, cause);
    }
}
