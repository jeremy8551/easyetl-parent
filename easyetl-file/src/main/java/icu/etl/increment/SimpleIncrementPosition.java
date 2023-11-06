package icu.etl.increment;

import java.util.Arrays;

public class SimpleIncrementPosition implements IncrementPosition {

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
    public SimpleIncrementPosition(int[] newIndexPosition, int[] oldIndexPosition, int[] newComparePosition, int[] oldComparePosition) {
        this.newIndexPosition = Arrays.copyOf(newIndexPosition, newIndexPosition.length);
        this.oldIndexPosition = Arrays.copyOf(oldIndexPosition, oldIndexPosition.length);
        this.newComparePosition = Arrays.copyOf(newComparePosition, newComparePosition.length);
        this.oldComparePosition = Arrays.copyOf(oldComparePosition, oldComparePosition.length);
    }

    /**
     * 新旧数据的索引字段和比较字段相同
     *
     * @param indexPosition   索引字段位置信息
     * @param comparePosition 比较字段位置信息
     */
    public SimpleIncrementPosition(int[] indexPosition, int[] comparePosition) {
        this(indexPosition, indexPosition, comparePosition, comparePosition);
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
