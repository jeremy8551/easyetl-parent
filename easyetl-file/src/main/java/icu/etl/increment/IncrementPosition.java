package icu.etl.increment;

/**
 * 新旧数据的位置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-14
 */
public interface IncrementPosition {

    /**
     * 返回新数据中的索引字段位置信息
     *
     * @return
     */
    int[] getNewIndexPosition();

    /**
     * 返回新数据中的所有比较字段位置信息
     *
     * @return
     */
    int[] getNewComparePosition();

    /**
     * 返回旧数据中的索引字段位置信息
     *
     * @return
     */
    int[] getOldIndexPosition();

    /**
     * 返回旧数据中的所有比较字段位置信息
     *
     * @return
     */
    int[] getOldComparePosition();

}