package icu.etl.database.export;

public class ExtractException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public ExtractException() {
        super();
    }

//	public ExtractException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

    public ExtractException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtractException(String message) {
        super(message);
    }

    public ExtractException(Throwable cause) {
        super(cause);
    }

}
