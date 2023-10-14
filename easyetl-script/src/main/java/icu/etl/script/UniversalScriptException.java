package icu.etl.script;

/**
 * 脚本引擎异常信息
 *
 * @author jeremy8551@qq.com
 */
public class UniversalScriptException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    /** 脚本命令所在行号 */
    private long lineNumber;

    /** 发生错误的脚本命令 */
    private String scrip;

    /**
     * 创建一个脚本异常信息
     */
    public UniversalScriptException() {
        super();
    }

    /**
     * 创建一个脚本异常信息
     *
     * @param message
     * @param cause
     */
    public UniversalScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个脚本异常信息
     *
     * @param message
     */
    public UniversalScriptException(String message) {
        super(message);
    }

    /**
     * 创建一个脚本异常信息
     *
     * @param cause
     */
    public UniversalScriptException(Throwable cause) {
        super(cause);
    }

    /**
     * 创建一个脚本异常信息
     *
     * @param e          异常信息
     * @param lineNumber 脚本命令发生异常时，脚本语句所在行号
     * @param script     发生异常错误的脚本语句
     */
    public UniversalScriptException(Throwable e, long lineNumber, String script) {
        super(e);
        this.lineNumber = lineNumber;
        this.scrip = script;
    }

    /**
     * 返回脚本命令发生异常时，脚本语句所在行号（从 1 开始）
     *
     * @return
     */
    public long getLineNumber() {
        return this.lineNumber;
    }

    /**
     * 返回发生异常的脚本语句
     *
     * @return
     */
    public String getScript() {
        return this.scrip;
    }

}
