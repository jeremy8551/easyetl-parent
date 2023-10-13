package icu.etl.expression;

/**
 * 文本表达式异常错误
 *
 * @author jeremy8551@qq.com
 */
public class ExpressionException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    private int errorOffset;

    public ExpressionException() {
        super();
    }

    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(Throwable cause) {
        super(cause);
    }

    public ExpressionException(String message, int errorOffset) {
        this(message);
        this.errorOffset = errorOffset;
    }

    public int getErrorOffset() {
        return errorOffset;
    }

}
