package icu.etl.os;

/**
 * 操作系统异常
 */
public class OSException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public OSException() {
        super();
    }

//	public OSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

    public OSException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSException(String message) {
        super(message);
    }

    public OSException(Throwable cause) {
        super(cause);
    }

}
