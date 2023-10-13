package icu.etl.io;

import java.io.IOException;

/**
 * 表格型文本文件的监听器
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-08
 */
public interface TextTableFileReaderListener {

    /**
     * 如果字段值中存在回车或换行符时的处理接口
     *
     * @param file       文件信息
     * @param line       存在回车或换行符的行信息
     * @param lineNumber 数据文件中的行号
     * @throws IOException
     */
    void processLineSeparator(TextTableFile file, TextTableLine line, long lineNumber) throws IOException;

    /**
     * 成功读取一行记录后处理行内容
     *
     * @param file       文件信息
     * @param line       读取的行内容
     * @param lineNumber 数据文件中的行号
     * @return 返回 true 表示跳过当前行内容直接读取下一行内容，返回 false 表示 {@linkplain TextTableFileReader#readLine()} 方法返回当前行内容
     * @throws IOException
     */
    boolean processLine(TextTableFile file, TextTableLine line, long lineNumber) throws IOException;

    /**
     * 处理文件记录的字段个数与预期不符的错误
     *
     * @param in         发生错误的输入流
     * @param line       字段个数与预期不符的行内容
     * @param lineNumber 数据文件中的行号
     * @return 返回 true 表示未能处理错误需要抛出异常，返回 false 表示错误处理完毕可以通过 {@linkplain TextTableFileReader#readLine()} 方法返回当前行内容
     * @throws IOException
     */
    boolean processColumnException(TextTableFileReader in, String line, long lineNumber) throws IOException;

}
