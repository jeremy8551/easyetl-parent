package icu.etl.expression.operation;

import icu.etl.expression.ExpressionException;
import icu.etl.expression.parameter.DateUnitParameter;
import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.Dates;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;

/**
 * 减法运算
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-16 20:30:30
 */
public class SubOper implements Operator {

    public Parameter execute(Parameter d1, Parameter d2) {
        ExpressionParameter data = new ExpressionParameter();
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.subtract(d1.doubleValue(), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.subtract(d1.doubleValue(), Double.valueOf(d2.longValue().doubleValue())));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.subtract(Double.valueOf(d1.longValue().doubleValue()), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.LONG);
            data.setValue(Numbers.subtract(d1.longValue(), d2.longValue()));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.DATEUNIT) { // 19000101 - 1 month
            java.util.Date date = Dates.testParse(d1.longValue());
            if (date == null) {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(62, d1.stringValue()));
            }
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), -p.longValue().intValue()));
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.DATEUNIT) {
            java.util.Date date = Dates.testParse(d1.value());
            if (date == null) {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(62, d1.stringValue()));
            }
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), -p.longValue().intValue()));
            return data;
        }
        if (d1.getType() == Parameter.DATE && d2.getType() == Parameter.DATEUNIT) {
            java.util.Date date = d1.dateValue();
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), -p.longValue().intValue()));
            return data;
        }

        throw new UnsupportedOperationException(d1 + " - " + d2);
    }

    public int getPriority() {
        return 4;
    }

    public String toString() {
        return ResourcesUtils.getExpressionMessage(20);
    }
}
