package icu.etl.expression.operation;

import icu.etl.expression.ExpressionException;
import icu.etl.expression.parameter.DateUnitParameter;
import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.Dates;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 加法运算
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-19 22:58:53
 */
public class PlusOper implements Operator {

    public Parameter execute(Parameter d1, Parameter d2) {
        ExpressionParameter data = new ExpressionParameter();
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.STRING) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(d1.stringValue()).append(d2.stringValue()).toString());
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(d1.stringValue()).append(String.valueOf(d2.doubleValue().doubleValue())).toString());
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(d1.stringValue()).append(String.valueOf(d2.longValue().longValue())).toString());
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.DATE) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(d1.stringValue()).append(StringUtils.toString(d2.dateValue())).toString());
            return data;
        }
        if (d1.getType() == Parameter.STRING && d2.getType() == Parameter.DATEUNIT) {
            java.util.Date date = Dates.testParse(d1.value());
            if (date == null) {
                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg062", d1.stringValue()));
            }
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), p.longValue().intValue()));
            return data;
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.STRING) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(String.valueOf(d1.doubleValue().doubleValue())).append(d2.stringValue()).toString());
            return data;
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.plus(d1.doubleValue(), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.DOUBLE && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.plus(d1.doubleValue(), Double.valueOf(d2.longValue().doubleValue())));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.STRING) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(String.valueOf(d1.longValue().longValue())).append(d2.stringValue()).toString());
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.DOUBLE) {
            data.setType(Parameter.DOUBLE);
            data.setValue(Numbers.plus(Double.valueOf(d1.longValue().doubleValue()), d2.doubleValue()));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.LONG) {
            data.setType(Parameter.LONG);
            data.setValue(Numbers.plus(d1.longValue(), d2.longValue()));
            return data;
        }
        if (d1.getType() == Parameter.LONG && d2.getType() == Parameter.DATEUNIT) {
            java.util.Date date = Dates.testParse(d1.longValue());
            if (date == null) {
                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg062", d1.stringValue()));
            }
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), p.longValue().intValue()));
            return data;
        }
        if (d1.getType() == Parameter.DATE && d2.getType() == Parameter.DATEUNIT) {
            java.util.Date date = d1.dateValue();
            DateUnitParameter p = (DateUnitParameter) d2;
            data.setType(Parameter.DATE);
            data.setValue(Dates.calcDay(date, p.getUnit(), p.longValue().intValue()));
            return data;
        }
        if (d1.getType() == Parameter.DATE && d2.getType() == Parameter.STRING) {
            data.setType(Parameter.STRING);
            data.setValue(new StringBuilder().append(StringUtils.toString(d1.dateValue())).append(d2.stringValue()).toString());
            return data;
        }

        throw new UnsupportedOperationException(d1 + " + " + d2);
    }

    public int getPriority() {
        return 4;
    }

    public String toString() {
        return ResourcesUtils.getMessage("expression.standard.output.msg019");
    }
}
