package icu.etl.expression.operation;

import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.ResourcesUtils;

/**
 * 与运算 {@literal &&}
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-19 23:00:05
 */
public class AndOper implements Operator {

    public Parameter execute(Parameter d1, Parameter d2) {
        if (d1.getType() != Parameter.BOOLEAN || d2.getType() != Parameter.BOOLEAN) {
            throw new UnsupportedOperationException(d1 + " && " + d2);
        }

        return new ExpressionParameter(Parameter.BOOLEAN, new Boolean(d1.booleanValue() && d2.booleanValue()));
    }

    public int getPriority() {
        return 11;
    }

    public String toString() {
        return ResourcesUtils.getExpressionMessage(14);
    }
}
