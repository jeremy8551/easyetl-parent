package icu.etl.io;

import java.io.IOException;
import java.io.Writer;

/**
 * 表格型数据
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-11
 */
public interface Table extends Cloneable {

    /**
     * 返回列名
     *
     * @param position 列的位置信息，从 1 开始
     * @return
     */
    String getColumnName(int position);

    /**
     * 设置列名
     *
     * @param position 列的位置信息，从 1 开始
     * @param name     列名
     */
    void setColumnName(int position, String name);

    /**
     * 设置文件每行的字段个数 <br>
     * 此方法只负责设置参数,不应对参数进行检查及抛出异常
     *
     * @param column 0表示赋默认值;
     */
    void setColumn(int column);

    /**
     * 文件每行的字段个数 <br>
     * 即使数据文件关闭后, 可返回最后设置值
     *
     * @return
     */
    int getColumn();

    /**
     * 返回一个副本
     *
     * @return
     */
    Table clone();

    /**
     * 返回表格数据的输出流
     *
     * @param writer 输出流
     * @param cache  缓存行数
     * @return
     * @throws IOException
     */
    TableWriter getWriter(Writer writer, int cache) throws IOException;

}
