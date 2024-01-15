package icu.etl.printer;

import java.io.Closeable;
import java.io.Writer;
import java.text.Format;

/**
 * 信息输出接口
 *
 * @author jeremy8551@qq.com
 */
public interface Printer extends ProgressPrinter, Closeable {

    /**
     * 返回字符串输出流
     *
     * @return 输出流，如果未设置，则返回null
     */
    Writer getWriter();

    /**
     * 设置字符串输出流
     *
     * @param writer 输出流
     */
    void setWriter(Writer writer);

    /**
     * 设置类型转换器
     *
     * @param f 类型转换器
     */
    void setFormatter(Format f);

    /**
     * 返回类型转换器
     *
     * @return 类型转换器
     */
    Format getFormatter();

    /**
     * 输出字符序列信息（不会追加换行符）
     *
     * @param msg 字符序列
     */
    void print(CharSequence msg);

    /**
     * 输出一个Object 信息（不会追加换行符）
     *
     * @param obj 对象
     */
    void print(Object obj);

    /**
     * 输出换行符
     */
    void println();

    /**
     * 输出字符序列信息并打印异常信息
     *
     * @param msg 字符序列
     * @param e   异常信息
     */
    void println(CharSequence msg, Throwable e);

    /**
     * 输出 Object 信息
     *
     * @param obj 对象
     */
    void println(Object obj);

    /**
     * 关闭输出流
     */
    void close();

}