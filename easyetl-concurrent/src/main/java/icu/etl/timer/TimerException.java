package icu.etl.timer;

/**
 * 定时器组件异常
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-04
 */
public class TimerException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public TimerException() {
        super();
    }

    public TimerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimerException(String message) {
        super(message);
    }

    public TimerException(Throwable cause) {
        super(cause);
    }

}
