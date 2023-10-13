package icu.etl.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * 表格数据输入流接口
 *
 * @author jeremy8551@qq.com
 */
public interface TableReader extends LineNumber, Closeable {

    /**
     * 返回输入流打开的表格文件
     *
     * @return
     */
    Table getTable();

    /**
     * 输入流读取一行内容
     *
     * @return 返回 null 表示已读取到文件结尾
     * @throws IOException
     */
    TableLine readLine() throws IOException;

    /**
     * 判断是否可以读取指定行内容
     *
     * @param row 文件行号，从 1 开始，0 表示尝试将输入流恢复到初始未读状态
     * @return 返回 null 表示已读取到文件结尾
     * @throws IOException
     */
    TableLine readLine(int row) throws IOException;

    /**
     * 返回输入流当前行内容
     *
     * @return
     */
    TableLine getLine();

}