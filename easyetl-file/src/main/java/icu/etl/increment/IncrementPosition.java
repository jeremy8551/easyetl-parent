package icu.etl.increment;

/**
 * 文件的位置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-14
 */
public interface IncrementPosition {

    /**
     * 返回新数据中的索引字段位置信息
     *
     * @return 位置数组，数组中元素只都是从1开始
     */
    int[] getNewIndexPosition();

    /**
     * 返回新数据中的所有比较字段位置信息
     *
     * @return 位置数组，数组中元素只都是从1开始
     */
    int[] getNewComparePosition();

    /**
     * 返回旧数据中的索引字段位置信息
     *
     * @return 位置数组，数组中元素只都是从1开始
     */
    int[] getOldIndexPosition();

    /**
     * 返回旧数据中的所有比较字段位置信息
     *
     * @return 位置数组，数组中元素只都是从1开始
     */
    int[] getOldComparePosition();

}