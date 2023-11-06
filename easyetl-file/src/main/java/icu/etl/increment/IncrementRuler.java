package icu.etl.increment;

import java.io.IOException;

import icu.etl.io.TextTableLine;

/**
 * 增量剥离规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2015-04-02
 */
public interface IncrementRuler {

    /**
     * 比较索引字段
     *
     * @param l1 表格型文件中的行
     * @param l2 表格型文件中的行
     * @return
     * @throws IOException
     */
    int compareIndex(TextTableLine l1, TextTableLine l2) throws IOException;

    /**
     * 比较字段值
     *
     * @param l1 表格型文件中的行
     * @param l2 表格型文件中的行
     * @return 0表示相等 非0表示不等字段的位置（从 1 开始）
     * @throws IOException
     */
    int compareColumn(TextTableLine l1, TextTableLine l2) throws IOException;

}
