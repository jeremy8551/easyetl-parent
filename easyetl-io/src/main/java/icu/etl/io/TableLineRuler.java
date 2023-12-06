package icu.etl.io;

import java.util.List;

/**
 * 文本字符串分隔与拼接规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-31
 */
public interface TableLineRuler {

    /**
     * 将字符串分割成多个字段，并存储到集合参数 list 中
     *
     * @param str  字符串
     * @param list 字段集合
     */
    void split(String str, List<String> list);

    /**
     * 将表格的行拼接成一个字符串
     *
     * @param line 文件中的一行内容
     * @return 字符串
     */
    String join(TableLine line);

    /**
     * 替换指定位置上的值
     *
     * @param position 字段位置
     * @param value    字段值
     * @return 替换后行的内容
     */
    String replace(TextTableLine line, int position, String value);

}