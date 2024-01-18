package icu.etl.increment;

import java.io.IOException;

import icu.etl.io.TextTableLine;

/**
 * 增量剥离比较规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2015-04-02
 */
public interface IncrementComparator {

    /**
     * 比较索引字段
     *
     * @param l1 表格型文件中的行
     * @param l2 表格型文件中的行
     * @return 返回0表示相等 小于0表示参数1小于参数2 大于0表示参数1大于参数2
     * @throws IOException 访问文件错误
     */
    int compareIndex(TextTableLine l1, TextTableLine l2) throws IOException;

    /**
     * 比较字段值
     *
     * @param l1 表格型文件中的行
     * @param l2 表格型文件中的行
     * @return 0表示相等 非0表示不等字段的位置（从 1 开始）
     * @throws IOException 访问文件错误
     */
    int compareColumn(TextTableLine l1, TextTableLine l2) throws IOException;

}
