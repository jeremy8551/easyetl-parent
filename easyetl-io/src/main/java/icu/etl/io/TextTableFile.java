package icu.etl.io;

import java.io.IOException;
import java.io.Writer;

import icu.etl.ioc.EasyContext;

/**
 * 表格型数据文件接口 <br>
 * <br>
 * 从容器上下文 {@linkplain EasyContext} 中返回一个 {@linkplain TextTableFile} 表格型文件对象
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-07-18
 */
public interface TextTableFile extends TextTable, TextFile {

    /**
     * 返回记录分隔规则
     *
     * @return 分隔规则
     */
    TableLineRuler getRuler();

    /**
     * 返回数据文件的输入流
     *
     * @param cache 缓冲区大小，单位：字符
     * @return 输入流
     * @throws IOException 打开输入流错误
     */
    TextTableFileReader getReader(int cache) throws IOException;

    /**
     * 返回数据文件的输入流
     *
     * @param start  输入流的起始位置, 从 0 开始 <br>
     *               如果起始位置不是一行内容的开始位置，则自动从下一行的起始位置开始读取 <br>
     * @param length 输入流读取的最大字节总数
     * @param cache  缓冲区大小，单位：字符
     * @return 输入流
     * @throws IOException 打开输入流错误1
     */
    TextTableFileReader getReader(long start, long length, int cache) throws IOException;

    /**
     * 返回数据文件的输出流
     *
     * @param append true表示追加写入记录 <br>
     *               false表示覆盖原有记录
     * @param cache  缓存行数
     * @return 输出流
     * @throws IOException 打开输出流错误
     */
    TextTableFileWriter getWriter(boolean append, int cache) throws IOException;

    TextTableFileWriter getWriter(Writer writer, int cache) throws IOException;

    /**
     * 统计文本表格型文件字段个数
     *
     * @return 文件中字段个数
     * @throws IOException 访问数据发生错误
     */
    int countColumn() throws IOException;

    TextTableFile clone();

}