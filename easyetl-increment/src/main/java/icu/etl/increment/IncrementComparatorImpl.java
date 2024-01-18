package icu.etl.increment;

import java.io.IOException;
import java.util.Comparator;

import icu.etl.io.TableColumnComparator;
import icu.etl.io.TextTableLine;
import icu.etl.util.ArrayUtils;
import icu.etl.util.StringUtils;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-16 06:54:58
 */
public class IncrementComparatorImpl implements IncrementComparator {

    /** 字符串比较器（用于比较字符串大小，在对数据按主键信息比较的时候用到） */
    private Comparator<String> comp;

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
     * @param comparator 字符串比较规则
     * @param position   位置信息
     */
    public IncrementComparatorImpl(Comparator<String> comparator, IncrementPosition position) {
        this.comp = (comparator == null) ? new TableColumnComparator() : comparator;
        this.newIndexPosition = position.getNewIndexPosition();
        this.oldIndexPosition = position.getOldIndexPosition();
        this.newComparePosition = ArrayUtils.shift(position.getNewComparePosition());
        this.oldComparePosition = ArrayUtils.shift(position.getOldComparePosition());

        if (this.newIndexPosition.length != this.oldIndexPosition.length) {
            throw new IllegalArgumentException(StringUtils.toString(this.newIndexPosition) + " != " + StringUtils.toString(this.oldIndexPosition));
        }
        if (this.newComparePosition.length != this.oldComparePosition.length) {
            throw new IllegalArgumentException(StringUtils.toString(this.newComparePosition) + " != " + StringUtils.toString(this.oldComparePosition));
        }
    }

    public int compareIndex(TextTableLine l1, TextTableLine l2) throws IOException {
        for (int i = 0; i < this.newIndexPosition.length; i++) {
            String value1 = l1.getColumn(this.newIndexPosition[i]);
            String value2 = l2.getColumn(this.oldIndexPosition[i]);
            int v = this.comp.compare(value1, value2);
            if (v == 0) {
                continue;
            } else {
                return v;
            }
        }
        return 0;
    }

    public int compareColumn(TextTableLine l1, TextTableLine l2) throws IOException {
        for (int i = 1; i < this.newComparePosition.length; i++) {
            String value1 = l1.getColumn(this.newComparePosition[i]);
            String value2 = l2.getColumn(this.oldComparePosition[i]);
            if (this.comp.compare(value1, value2) == 0) {
                continue;
            } else {
                return i;
            }
        }
        return 0;
    }

}