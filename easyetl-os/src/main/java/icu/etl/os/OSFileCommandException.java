package icu.etl.os;

/**
 * 操作系统文件功能异常
 */
public class OSFileCommandException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public OSFileCommandException() {
        super();
    }

//	public OSFileCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

    public OSFileCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSFileCommandException(String message) {
        super(message);
    }

    public OSFileCommandException(Throwable cause) {
        super(cause);
    }

}
