package icu.etl.expression.operation;

import icu.etl.expression.ExpressionException;
import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.expression.parameter.TwoParameter;
import icu.etl.util.ResourcesUtils;

public class TreeOper implements Operator {

    public Parameter execute(Parameter condition, Parameter d2) {
        condition.execute();
        if (condition.getType() != Parameter.BOOLEAN) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(37, condition.getType()));
        }
        TwoParameter run = (TwoParameter) d2;

        ExpressionParameter data = new ExpressionParameter();
        if (condition.booleanValue().booleanValue()) {
            Parameter trueRun = run.getTrueRun();
            trueRun.execute();
            data.setType(trueRun.getType());
            data.setValue(trueRun.value());
        } else {
            Parameter falseRun = run.getFalseRun();
            falseRun.execute();
            data.setType(falseRun.getType());
            data.setValue(falseRun.value());
        }
        return data;
    }

    public int getPriority() {
        return 13;
    }

    public String toString() {
        return ResourcesUtils.getExpressionMessage(21);
    }

}
