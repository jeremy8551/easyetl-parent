package icu.etl.concurrent;

import icu.etl.log.STD;
import icu.etl.printer.Printer;

public class ExecutorLogger {

    /** 标准信息输出接口 */
    private Printer stdout;

    /** 错误信息输出接口 */
    private Printer stderr;

    /**
     * 初始化
     */
    public ExecutorLogger() {
    }

    /**
     * 设置标准信息输出接口
     *
     * @param out 标准信息输出接口
     */
    public void setStdout(Printer out) {
        this.stdout = out;
    }

    /**
     * 设置错误信息输出接口
     *
     * @param err 错误信息输出接口
     */
    public void setStderr(Printer err) {
        this.stderr = err;
    }

    /**
     * 判断是否可以输出调试信息
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return STD.out.isDebugEnabled();
    }

    /**
     * 输出调试信息
     *
     * @param message
     */
    public void debug(String message) {
        STD.out.debug(message);
    }

    /**
     * 判断是否可以输出警告信息
     *
     * @return
     */
    public boolean isWarnEnabled() {
        return STD.out.isWarnEnabled();
    }

    /**
     * 输出警告信息
     *
     * @param message
     */
    public void warn(String message) {
        STD.out.warn(message);
    }

    /**
     * 打印标准信息
     *
     * @param str
     */
    public void info(String str) {
        if (this.stdout != null) {
            this.stdout.println(str);
            return;
        }

        if (STD.out.isInfoEnabled()) {
            STD.out.info(str);
        }
    }

    /**
     * 打印错误信息
     *
     * @param str
     * @param e
     */
    public void error(String str, Throwable e) {
        if (this.stderr != null) {
            this.stderr.println(str, e);
            return;
        }

        if (STD.out.isErrorEnabled()) {
            STD.out.error(str, e);
        }
    }

}
