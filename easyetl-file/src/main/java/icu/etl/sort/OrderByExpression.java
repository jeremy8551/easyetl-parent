package icu.etl.sort;

import java.util.Comparator;
import java.util.List;

import icu.etl.expression.Analysis;
import icu.etl.expression.FunctionExpression;
import icu.etl.ioc.EasyContext;
import icu.etl.util.Ensure;
import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

/**
 * 排序字段表达式, 如: 1 或 1 asc 或 int(1) 或 number(2) desc
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-19
 */
public class OrderByExpression {

    /** 容器上下文信息 */
    private EasyContext context;

    /** 表达式 */
    private String expression;

    /** 位置信息，从1开始 */
    private int position;

    /** 排序规则 */
    private Comparator<String> comparator;

    /** 排序方向 */
    private boolean asc;

    /**
     * 排序字段表达式
     *
     * @param context    容器上下文信息
     * @param analysis   语句分析器
     * @param expression 排序表达式 <br>
     *                   1 表示排序第一个字段 <br>
     *                   1 asc 表示第一个字段按正序排序 <br>
     *                   int(1) 表示第一个字段按整数正序排序 <br>
     *                   number(2) desc 表示第二个字段按数值倒序排序 <br>
     */
    @SuppressWarnings("unchecked")
    public OrderByExpression(EasyContext context, Analysis analysis, String expression) {
        this.context = Ensure.notNull(context);
        expression = StringUtils.trimBlank(expression);
        List<String> list = Ensure.notNull(analysis).split(expression);
        if (list.size() != 1 && list.size() != 2) {
            throw new IllegalArgumentException(expression);
        }
        this.expression = expression;

        String first = list.get(0);
        FunctionExpression expr = new FunctionExpression(analysis, first);
        if (expr.containParameter()) {
            Ensure.equals(1, expr.getParameterSize()); // 只能有一个参数
            this.position = Ensure.isFromOne(Integer.parseInt(expr.getParameter(1)));
            String name = Ensure.notBlank(expr.getName());
            this.comparator = Ensure.notNull(this.context.getBean(Comparator.class, name));
        } else {
            this.position = Ensure.isFromOne(Integer.parseInt(first));
            this.comparator = new StringComparator();
        }

        // 解析排序方向
        if (list.size() == 2) {
            String ascOrDesc = list.get(1);
            Ensure.exists(ascOrDesc, "asc", "desc");
            this.asc = "asc".equalsIgnoreCase(ascOrDesc);
        } else {
            this.asc = true;
        }
    }

    public OrderByExpression(int position, Comparator<String> comparator, boolean asc) {
        this.position = Ensure.isFromOne(position);
        this.comparator = Ensure.notNull(comparator);
        this.asc = asc;
    }

    /**
     * 返回位置信息, 从 1 开始
     *
     * @return 位置信息
     */
    public int getPosition() {
        return position;
    }

    /**
     * 返回排序规则
     *
     * @return 排序规则
     */
    public Comparator<String> getComparator() {
        return comparator;
    }

    /**
     * 判断排序方向，正序或倒序
     *
     * @return 返回 true 表示从小到大排序, false 表示从大到小排序
     */
    public boolean isAsc() {
        return asc;
    }

    /**
     * 返回表达式 1 asc 或 2 desc
     *
     * @return 字符串
     */
    public String getExpression() {
        if (this.expression != null) {
            return this.expression;
        } else {
            return this.position + " " + (this.asc ? "asc" : "desc");
        }
    }

    public String toString() {
//        return "OrderByExpression [position=" + this.position + ", asc=" + this.asc + ", comparator=" + this.comparator.getClass().getName() + "]";
        return this.getExpression();
    }

}
