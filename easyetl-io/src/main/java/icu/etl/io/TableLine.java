package icu.etl.io;

/**
 * 表格数据中的行信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-28
 */
public interface TableLine {

    /**
     * 判断指定位置上的字符串是否为空字符串
     *
     * @param position 字段位置, 从1开始
     * @return
     */
    boolean isColumnBlank(int position);

    /**
     * 取得指定位置上的数值 <br>
     * 数据文件关闭后, 返回null
     *
     * @param position 字段位置, 从1开始
     * @return 不可能返回null
     */
    String getColumn(int position);

    /**
     * 设置当前行某个字段的值 <br>
     * 会影响到 {@linkplain #getColumn(int)} 方法的返回值
     *
     * @param position 字段位置, 从1开始
     * @param value    替换后的字符串
     */
    void setColumn(int position, String value);

    /**
     * 返回字段的个数
     *
     * @return
     */
    int getColumn();

}
