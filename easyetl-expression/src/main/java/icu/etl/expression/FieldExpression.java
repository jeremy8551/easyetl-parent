package icu.etl.expression;

import icu.etl.util.StringUtils;

/**
 * 字段表达式: int(1) number(12,3)
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-19
 */
public class FieldExpression {

    private String name;

    private String parameter;

    private String precision;

    private String scale;

    private boolean existsParenthes;

    private String src;

    /**
     * 字段表达式
     *
     * @param analysis   语句分析器
     * @param expression 字段表达式, 如: int(1) 或 number(12,3) 或 date
     */
    public FieldExpression(Analysis analysis, String expression) {
        if (StringUtils.isBlank(expression)) {
            throw new IllegalArgumentException(expression);
        } else {
            this.src = expression;
        }

        int begin = analysis.indexOf(expression, "(", 0, 2, 2);
        if (begin != -1) {
            this.existsParenthes = true;
            this.name = expression.substring(0, begin);
            int end = analysis.indexOf(expression, ")", begin, 2, 2);
            if (end != -1) {
                this.parameter = expression.substring(begin + 1, end);
            } else {
                throw new IllegalArgumentException(expression);
            }
        } else {
            this.existsParenthes = false;
            this.name = StringUtils.trimBlank(expression);
            this.parameter = "";
        }

        int start = analysis.indexOf(this.parameter, ",", 0, 2, 2);
        if (start != -1) {
            this.precision = this.parameter.substring(0, start);
            this.scale = this.parameter.substring(start + 1);
        } else {
            this.precision = this.parameter;
            this.scale = "";
        }
    }

    /**
     * 判断表达式中是否存在小括号内容
     *
     * @return
     */
    public boolean containParenthes() {
        return this.existsParenthes;
    }

    /**
     * 返回字段名，如: int 或 number
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 返回字段小括号中的内容
     *
     * @return
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * 返回字段长度
     *
     * @return
     */
    public String getPrecision() {
        return precision;
    }

    /**
     * 返回字段小数位长度
     *
     * @return
     */
    public String getScale() {
        return scale;
    }

    public String toString() {
        return this.src;
    }

}
