package icu.etl.expression.operation;

import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;

/**
 * 不等于运算
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-19 23:03:18
 */
public class NotEqualsOper implements Operator {

    public Parameter execute(Parameter d1, Parameter d2) {
        ExpressionParameter data = new ExpressionParameter();
        if (d1.getType() == Parameter.BOOLEAN && d2.getType() == Parameter.BOOLEAN) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(new Boolean(d1.booleanValue().booleanValue() != d2.booleanValue().booleanValue()));
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.STRING) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(new Boolean(!d1.stringValue().equals(d2.stringValue())));
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.DOUBLE) {
            throw new UnsupportedOperationException(d1 + " != " + d2);
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.LONG) {
            throw new UnsupportedOperationException(d1 + " != " + d2);
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.STRING) {
            throw new UnsupportedOperationException(d1 + " != " + d2);
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(!Numbers.equals(d1.doubleValue(), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(!Numbers.equals(d1.doubleValue(), Double.valueOf(d2.longValue().doubleValue())));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.STRING) {
            throw new UnsupportedOperationException(d1 + " != " + d2);
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(!Numbers.equals(Double.valueOf(d1.longValue().doubleValue()), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.BOOLEAN);
            data.setValue(!Numbers.equals(d1.longValue(), d2.longValue()));
            return data;
        }

        throw new UnsupportedOperationException(d1 + " != " + d2);
    }

    public int getPriority() {
        return 7;
    }

    public String toString() {
        return ResourcesUtils.getExpressionMessage(17);
    }
}
