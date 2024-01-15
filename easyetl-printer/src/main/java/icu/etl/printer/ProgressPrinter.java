package icu.etl.printer;

/**
 * 进度信息输出接口
 */
public interface ProgressPrinter {

    /**
     * 输出字符序列信息
     *
     * @param msg 字符序列
     */
    void println(CharSequence msg);

    /**
     * 多任务程序使用的信息输出接口
     *
     * @param id      多任务程序中每个任务的唯一编号
     * @param message 输出信息
     */
    void println(String id, CharSequence message);

}
