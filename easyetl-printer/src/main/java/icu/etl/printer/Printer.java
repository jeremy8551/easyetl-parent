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
     * 返回当前使用的信息输出接口<br>
     * 如果未设置信息输出接口，则返回null
     *
     * @return
     */
    Writer getWriter();

    /**
     * 设置信息输出接口，更换原有信息输出接口
     *
     * @param writer 设置为 null 表示删除信息输出接口，使用日志输出
     */
    void setWriter(Writer writer);

    /**
     * 设置类型转换器
     *
     * @param f
     */
    void setFormatter(Format f);

    /**
     * 返回类型转换器
     *
     * @return
     */
    Format getFormatter();

    /**
     * 输出字符序列信息（不会追加换行符）
     *
     * @param msg
     */
    void print(CharSequence msg);

    /**
     * 输出字符信息（不会追加换行符）
     *
     * @param c
     */
    void print(char c);

    /**
     * 输出整数信息（不会追加换行符）
     *
     * @param i
     */
    void print(int i);

    /**
     * 输出 float 信息（不会追加换行符）
     *
     * @param f
     */
    void print(float f);

    /**
     * 输出 double 信息（不会追加换行符）
     *
     * @param d
     */
    void print(double d);

    /**
     * 输出 boolean 信息（不会追加换行符）
     *
     * @param b
     */
    void print(boolean b);

    /**
     * 输出 long 信息（不会追加换行符）
     *
     * @param d
     */
    void print(long d);

    /**
     * 输出字符数组信息（不会追加换行符）
     *
     * @param ca
     */
    void print(char[] ca);

    /**
     * 输出一个Object 信息（不会追加换行符）
     *
     * @param obj
     */
    void print(Object obj);

    /**
     * 输出换行符
     */
    void println();

    /**
     * 输出字符序列信息并打印异常信息
     *
     * @param msg
     * @param e
     */
    void println(CharSequence msg, Throwable e);

    /**
     * 输出字符信息
     *
     * @param c
     */
    void println(char c);

    /**
     * 输出整数信息
     *
     * @param i
     */
    void println(int i);

    /**
     * 输出 float 信息
     *
     * @param f
     */
    void println(float f);

    /**
     * 输出 double 信息
     *
     * @param d
     */
    void println(double d);

    /**
     * 输出 boolean 信息
     *
     * @param b
     */
    void println(boolean b);

    /**
     * 输出 long 信息
     *
     * @param d
     */
    void println(long d);

    /**
     * 输出字符数组信息
     *
     * @param ca
     */
    void println(char[] ca);

    /**
     * 输出 Object 信息
     *
     * @param obj
     */
    void println(Object obj);

    /**
     * 关闭输出流
     */
    void close();

}