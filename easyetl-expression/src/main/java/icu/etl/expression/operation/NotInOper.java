package icu.etl.expression.operation;

import icu.etl.expression.parameter.ArrayParameter;
import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;

public class NotInOper extends InOper {

    public Parameter execute(Parameter d1, Parameter d2) {
        if (Numbers.inArray(d1.getType(), Parameter.STRING, Parameter.DATE, Parameter.DOUBLE, Parameter.LONG) && d2.getType() == Parameter.ARRAY) {
            ExpressionParameter data = new ExpressionParameter();
            data.setType(Parameter.BOOLEAN);
            data.setValue(!((ArrayParameter) d2).exists(d1));
            return data;
        } else {
            throw new UnsupportedOperationException(d1 + " not in " + d2);
        }
    }

    public int getPriority() {
        return 7;
    }

    public String toString() {
        return ResourcesUtils.getMessage("expression.standard.output.msg011");
    }

}
