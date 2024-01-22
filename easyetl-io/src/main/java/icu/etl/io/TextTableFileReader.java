package icu.etl.io;

import java.io.IOException;

/**
 * 表格型数据的输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-11
 */
public interface TextTableFileReader extends TableReader, java.io.Closeable, LineNumber, LineSeparator {

    /**
     * 当前输入流跨过指定字符个数
     *
     * @param chars 越过的字符总数，可以是零
     * @param rows  越过的总行数（用于跨过行数的检查）
     * @return 返回 true 表示输入流已成功跳转到指定字符处 <br>
     * 返回 false 表示输入流实际越过的总行数与参数 rows 不符 <br>
     * @throws IOException 输入流发生错误
     */
    boolean skip(long chars, long rows) throws IOException;

    /**
     * 返回输入流起始位置信息
     *
     * @return 位置信息，从0开始
     */
    long getStartPointer();

    /**
     * 返回监听器
     *
     * @return 监听器
     */
    TextTableFileReaderListener getListener();

    /**
     * 设置监听器
     *
     * @param listener 监听器
     */
    void setListener(TextTableFileReaderListener listener);

    TextTableLine readLine() throws IOException;

    TextTableLine readLine(int row) throws IOException;

    TextTableLine getLine();

    TextTableFile getTable();

}
