package icu.etl.io;

import icu.etl.util.CharsetName;

/**
 * 文本型表格数据
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-11
 */
public interface TextTable extends Table, CharsetName, Escape, LineSeparator {

    /**
     * 设置字段分隔符
     *
     * @param delimiter 分隔符
     */
    void setDelimiter(String delimiter);

    /**
     * 返回字段分隔符
     *
     * @return 分隔符
     */
    String getDelimiter();

    /**
     * 设置字符串二端的分隔符, 不能是 null
     *
     * @param coldel 字符串
     */
    void setCharDelimiter(String coldel);

    /**
     * 返回字符串二端的分隔符, 不能返回null
     *
     * @return 字符串
     */
    String getCharDelimiter();

    /**
     * 判断数据格式（字段分隔符，行间分隔符，字符串限定符，字符集名字，转义自负，列的个数）是否相等
     *
     * @param table 格式信息
     * @return 返回true表示格式相等 false表示格式不等
     */
    boolean equalsStyle(TextTable table);

}
