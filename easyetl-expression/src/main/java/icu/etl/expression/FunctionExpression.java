package icu.etl.expression;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

/**
 * 字段类型表达式: int(1) number(12,3)
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-19
 */
public class FunctionExpression {

    /** 表达式 */
    private String src;

    /** 名字 */
    private String name;

    /** 是否存在小括号 */
    private boolean exists;

    /** 小括号中的参数内容 */
    private String parameter;

    /** 小括号中第一个参数 */
    private List<String> list;

    /**
     * 字段表达式
     *
     * @param analysis   语句分析器
     * @param expression 字段表达式, 如: int(1) 或 number(12,3) 或 date
     */
    public FunctionExpression(Analysis analysis, String expression) {
        this.src = Ensure.notBlank(expression);
        int begin = analysis.indexOf(expression, "(", 0, 2, 2);
        if (begin != -1) {
            this.exists = true;
            this.name = expression.substring(0, begin);
            int end = Ensure.isFromZero(analysis.indexOf(expression, ")", begin, 2, 2));
            this.parameter = expression.substring(begin + 1, end);
            this.list = analysis.split(this.parameter, ',');
        } else {
            this.exists = false;
            this.name = StringUtils.trimBlank(expression);
            this.parameter = "";
            this.list = new ArrayList<String>();
        }
    }

    /**
     * 返回名字
     *
     * @return 字符串
     */
    public String getName() {
        return this.name;
    }

    /**
     * 判断表达式中是否存在小括号内容
     *
     * @return 返回true表示存在小括号，false表示没有小括号
     */
    public boolean containParameter() {
        return this.exists;
    }

    /**
     * 返回字段小括号中的内容
     *
     * @return 小括号中的内容
     */
    public String getParameter() {
        return this.parameter;
    }

    /**
     * 返回参数个数
     *
     * @return 参数个数
     */
    public int getParameterSize() {
        return this.list.size();
    }

    /**
     * 返回小括号中的第几个参数
     *
     * @param position 位置信息，从1开始
     * @return 字符串
     */
    public String getParameter(int position) {
        int p = Ensure.isFromOne(position);
        return this.list.get(p - 1);
    }

    /**
     * 返回表达式
     *
     * @return 字符串
     */
    public String getExpression() {
        return src;
    }

    public String toString() {
        return this.src;
    }

}
