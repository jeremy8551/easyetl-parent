package icu.etl.increment;

import icu.etl.util.ArrayUtils;

/**
 * 接口实现类
 */
public class IncrementPositionImpl implements IncrementPosition {

    /** 新数据的索引字段的位置 */
    private int[] newIndexPosition;

    /** 新数据的比较字段的位置 */
    private int[] newComparePosition;

    /** 索引字段的位置 */
    private int[] oldIndexPosition;

    /** 比较字段的位置 */
    private int[] oldComparePosition;

    /**
     * 初始化
     *
     * @param newIndexPosition   索引字段位置信息
     * @param oldIndexPosition   索引字段位置信息
     * @param newComparePosition 比较字段位置信息
     * @param oldComparePosition 比较字段位置信息
     */
    public IncrementPositionImpl(int[] newIndexPosition, int[] oldIndexPosition, int[] newComparePosition, int[] oldComparePosition) {
        this.newIndexPosition = ArrayUtils.copyOf(newIndexPosition, newIndexPosition.length);
        this.oldIndexPosition = ArrayUtils.copyOf(oldIndexPosition, oldIndexPosition.length);
        this.newComparePosition = ArrayUtils.copyOf(newComparePosition, newComparePosition.length);
        this.oldComparePosition = ArrayUtils.copyOf(oldComparePosition, oldComparePosition.length);
    }

    public int[] getNewIndexPosition() {
        return this.newIndexPosition;
    }

    public int[] getNewComparePosition() {
        return this.newComparePosition;
    }

    public int[] getOldIndexPosition() {
        return this.oldIndexPosition;
    }

    public int[] getOldComparePosition() {
        return this.oldComparePosition;
    }

}
