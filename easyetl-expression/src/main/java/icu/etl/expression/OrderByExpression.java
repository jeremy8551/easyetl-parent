package icu.etl.expression;

import java.util.Comparator;
import java.util.List;

import icu.etl.ioc.EasyetlContext;
import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

/**
 * 排序字段表达式, 如: 1 或 1 asc 或 int(1) 或 number(2) desc
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-19
 */
public class OrderByExpression {

    private int position;
    private Comparator<String> comparator;
    private boolean asc;
    protected EasyetlContext context;

    /**
     * 排序字段表达式
     *
     * @param context
     * @param analysis   语句分析器
     * @param expression 排序表达式
     *                   1 表示排序第一个字段
     *                   1 asc 表示第一个字段按正序排序
     *                   int(1) 表示第一个字段按整数正序排序
     *                   number(2) desc 表示第二个字段按数值倒序排序
     * @param asc        true表示正序
     *                   false表示倒序
     */
    @SuppressWarnings("unchecked")
    public OrderByExpression(EasyetlContext context, Analysis analysis, String expression, boolean asc) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (analysis == null) {
            throw new NullPointerException();
        }

        this.context = context;
        expression = StringUtils.trimBlank(expression);
        List<String> list = analysis.split(expression);
        if (list.size() != 1 && list.size() != 2) {
            throw new IllegalArgumentException(expression);
        }

        FieldExpression field = new FieldExpression(analysis, list.get(0));
        if (field.containParenthes()) {
            this.position = Integer.parseInt(field.getParameter());
            this.comparator = this.context.get(Comparator.class, StringUtils.defaultString(field.getName(), "default"));
            if (this.comparator == null) {
                throw new UnsupportedOperationException(field.getName());
            }
        } else {
            this.position = Integer.parseInt(field.toString());
            this.comparator = new StringComparator();
        }

        if (this.position < 0) {
            throw new IllegalArgumentException(expression);
        }

        if (list.size() == 2) {
            this.asc = "asc".equalsIgnoreCase(list.get(1));
        } else {
            this.asc = asc;
        }
    }

    public OrderByExpression(int position, Comparator<String> comparator, boolean asc) {
        this.position = position;
        this.comparator = comparator;
        this.asc = asc;
    }

    /**
     * 返回位置信息, 从 1 开始
     *
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * 返回排序规则
     *
     * @return
     */
    public Comparator<String> getComparator() {
        return comparator;
    }

    /**
     * 返回 true 表示从小到大排序, false 表示从大到小排序
     *
     * @return
     */
    public boolean isAsc() {
        return asc;
    }

    public String toString() {
        return "OrderByExpression [position=" + this.position + ", asc=" + this.asc + ", comparator=" + this.comparator.getClass().getName() + "]";
    }

}
