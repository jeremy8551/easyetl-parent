package icu.etl.expression.command;

import java.util.ArrayList;
import java.util.List;

import icu.etl.expression.CommandExpression;
import icu.etl.expression.ExpressionException;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CommandParameter {

    /** 表达式信息 */
    private CommandExpression expr;

    /** 预期的参数个数 */
    private List<Integer> ranges;

    /** 参数集合 */
    private List<String> values;

    /**
     * 初始化
     */
    public CommandParameter(CommandExpression expr) {
        this.expr = expr;
        this.ranges = new ArrayList<Integer>();
        this.values = new ArrayList<String>();
    }

    /**
     * 判断字符串参数是否复合命令参数的规则
     *
     * @param pattern
     * @return
     */
    public boolean match(String pattern) {
        return pattern != null && pattern.startsWith("{") && pattern.endsWith("}");
    }

    /**
     * 解析表达式
     *
     * @param pattern {1-3|5}
     */
    public void parse(String pattern) {
        this.ranges.clear();
        this.values.clear();

        int index = this.expr.getAnalysis().indexOfBrace(pattern, 0);
        if (index == -1) {
            throw new IllegalArgumentException(pattern);
        }

        String expr = pattern.substring(1, index);

        // 不设置参数个数限制
        if (StringUtils.isBlank(expr)) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(77, this.expr.getCommand(), expr));
        }

        String[] array = StringUtils.split(expr, '|');
        for (String number : array) {
            if (number.indexOf('-') != -1) {
                String[] range = StringUtils.removeBlank(StringUtils.splitProperty(number, '-'));
                if (!StringUtils.isInt(range[0]) || !StringUtils.isInt(range[1])) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(77, this.expr.getCommand(), expr));
                }

                int i1 = Integer.parseInt(range[0]);
                int i2 = Integer.parseInt(range[1]);

                for (int start = Math.min(i1, i2), max = Math.max(i1, i2); start <= max; start++) {
                    this.ranges.add(new Integer(start));
                }
            } else {
                if (!StringUtils.isInt(number)) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(77, this.expr.getCommand(), expr));
                }
                this.ranges.add(new Integer(number));
            }
        }
    }

    /**
     * 添加参数
     *
     * @param parameter
     */
    public void add(String parameter) {
        this.values.add(parameter);
    }

    /**
     * 返回参数值
     *
     * @param index 从0开始
     * @return
     */
    public String get(int index) {
        return this.values.get(index);
    }

    /**
     * 返回参数个数
     *
     * @return
     */
    public int size() {
        return this.values.size();
    }

    /**
     * 返回参数集合
     *
     * @return
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * 检查参数值是否复合定义
     */
    public void check() {
        if (this.ranges.isEmpty()) {
            return;
        }

        int size = this.values.size();
        for (int i = 0; i < this.ranges.size(); i++) {
            if (this.ranges.get(i).intValue() == size) {
                return;
            }
        }

        throw new ExpressionException(ResourcesUtils.getExpressionMessage(77, this.expr.getCommand(), StringUtils.join(this.values, " "), StringUtils.join(this.ranges, ", ")));
    }

    public String toString() {
        return "CommandParameter[ranges=" + StringUtils.toString(ranges) + ", values=" + StringUtils.toString(values) + "]";
    }

}
