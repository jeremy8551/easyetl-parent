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
     * 判断字符串参数是否匹配命令参数的规则
     *
     * @param pattern 表达式
     * @return 返回true表示匹配 false表示不匹配
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
            throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg077", this.expr.getCommand(), expr));
        }

        String[] array = StringUtils.split(expr, '|');
        for (String number : array) {
            if (number.indexOf('-') != -1) {
                String[] range = StringUtils.removeBlank(StringUtils.splitProperty(number, '-'));
                if (!StringUtils.isInt(range[0]) || !StringUtils.isInt(range[1])) {
                    throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg077", this.expr.getCommand(), expr));
                }

                int i1 = Integer.parseInt(range[0]);
                int i2 = Integer.parseInt(range[1]);

                for (int start = Math.min(i1, i2), max = Math.max(i1, i2); start <= max; start++) {
                    this.ranges.add(start);
                }
            } else {
                if (!StringUtils.isInt(number)) {
                    throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg077", this.expr.getCommand(), expr));
                }
                this.ranges.add(new Integer(number));
            }
        }
    }

    /**
     * 添加参数
     *
     * @param parameter 参数
     */
    public void add(String parameter) {
        this.values.add(parameter);
    }

    /**
     * 返回参数值
     *
     * @param index 从0开始
     * @return 参数值
     */
    public String get(int index) {
        return index < this.values.size() ? this.values.get(index) : null;
    }

    /**
     * 返回参数个数
     *
     * @return 参数个数
     */
    public int size() {
        return this.values.size();
    }

    /**
     * 判断是否没有参数
     *
     * @return 返回true表示没有任何参数 false表示存在参数
     */
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    /**
     * 返回参数集合
     *
     * @return 参数集合
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * 判断是否只能有一个参数
     *
     * @return 返回true表示最多只能有一个参数 false表示可以有多个参数
     */
    public boolean onlyOne() {
        if (this.ranges.isEmpty()) {
            return false;
        }

        for (Integer value : this.ranges) {
            if (value > 1) {
                return false;
            }
        }
        return true;
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
            if (this.ranges.get(i) == size) {
                return;
            }
        }

        throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg078", this.expr.getCommand(), size, StringUtils.join(this.ranges, ", "), StringUtils.join(this.values, "\n")));
    }

    public String toString() {
        return "CommandParameter[ranges=" + StringUtils.toString(ranges) + ", values=" + StringUtils.toString(values) + "]";
    }

}
