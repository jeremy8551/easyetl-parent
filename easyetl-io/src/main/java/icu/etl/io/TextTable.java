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
     * @param delimiter
     */
    void setDelimiter(String delimiter);

    /**
     * 返回字段分隔符
     *
     * @return
     */
    String getDelimiter();

    /**
     * 设置字符串二端的分隔符, 不能是 null
     *
     * @param coldel
     */
    void setCharDelimiter(String coldel);

    /**
     * 返回字符串二端的分隔符, 不能返回null
     *
     * @return
     */
    String getCharDelimiter();

    /**
     * 判断数据格式（字段分隔符，行间分隔符，字符串限定符，字符集名字，转义自负，列的个数）是否相等
     *
     * @param t2
     * @return
     */
    boolean equalsStyle(TextTable t2);

}
