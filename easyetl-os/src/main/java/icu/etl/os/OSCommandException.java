package icu.etl.os;

public class OSCommandException extends OSException {
    private final static long serialVersionUID = 1L;

    public OSCommandException() {
        super();
    }

//	public OSCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

    public OSCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSCommandException(String message) {
        super(message);
    }

    public OSCommandException(Throwable cause) {
        super(cause);
    }

}
