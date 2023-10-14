package icu.etl.database.load;

public class LoadException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public LoadException() {
        super();
    }

//	public LoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadException(String message) {
        super(message);
    }

    public LoadException(Throwable cause) {
        super(cause);
    }

}
