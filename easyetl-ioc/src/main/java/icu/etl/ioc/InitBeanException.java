package icu.etl.ioc;

/**
 * 初始化组件发生错误
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/28
 */
public class InitBeanException extends RuntimeException {

    public InitBeanException() {
    }

    public InitBeanException(String message) {
        super(message);
    }

    public InitBeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitBeanException(Throwable cause) {
        super(cause);
    }

}
