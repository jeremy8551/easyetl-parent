package icu.etl.expression;

import java.util.List;

/**
 * 单词查询规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-18
 */
public interface WordQuery {

    /**
     * 搜索并返回结束位置
     *
     * @param src   原字符串
     * @param list  单词集合
     * @param index 搜索起始位置
     * @param last  搜索结束位置
     * @return 返回 -1 表示未搜索到并抛出异常
     */
    int indexOf(CharSequence src, List<WordIterator.Word> list, int index, int last);

}
