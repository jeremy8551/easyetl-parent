package icu.etl.expression.parameter;

import icu.etl.expression.Expression;
import icu.etl.util.StringUtils;

/**
 * 复合参数
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-21 14:40:58
 */
public class ComplexParameter extends ExpressionParameter {

    private String expression;

    private ComplexParameter() {
    }

    /**
     * 初始化
     *
     * @param str
     */
    public ComplexParameter(String str) {
        this.type = Parameter.EXPRESS;
        this.expression = str;

        Expression obj = new Expression(str);
        this.type = obj.getType();
        this.value = obj.value();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String toString() {
        StringBuilder cb = new StringBuilder();
        cb.append("[");
        cb.append(ExpressionParameter.getTypeName(this.type)).append(": ");
        cb.append(this.expression).append(": ");
        cb.append(StringUtils.toString(this.value));
        cb.append("]");
        return cb.toString();
    }

    public Parameter copy() {
        ComplexParameter obj = new ComplexParameter();
        obj.value = this.value;
        obj.type = this.type;
        obj.expression = this.expression;
        return obj;
    }

}
