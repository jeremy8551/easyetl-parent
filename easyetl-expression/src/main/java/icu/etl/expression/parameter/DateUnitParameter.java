package icu.etl.expression.parameter;

import java.util.Calendar;

import icu.etl.expression.ExpressionException;
import icu.etl.util.StringUtils;

/**
 * 日期类型的操作数，如： 1day 2month 3year 4hour 5minute 6second 7millis
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-21 14:40:58
 */
public class DateUnitParameter extends ExpressionParameter {

    protected int unit;

    private DateUnitParameter() {
    }

    /**
     * 初始化
     *
     * @param parameter
     * @param unit      {@linkplain Calendar#HOUR} <br>
     *                  {@linkplain Calendar#MINUTE} <br>
     *                  {@linkplain Calendar#SECOND} <br>
     *                  {@linkplain Calendar#MILLISECOND} <br>
     *                  {@linkplain Calendar#DAY_OF_MONTH} <br>
     *                  {@linkplain Calendar#MONTH} <br>
     *                  {@linkplain Calendar#YEAR} <br>
     */
    public DateUnitParameter(Parameter parameter, int unit) {
        this.type = DATEUNIT;
        this.unit = unit;

        switch (parameter.getType()) {
            case LONG:
                this.value = parameter.longValue();
                break;
            case STRING:
                if (StringUtils.isLong(parameter.stringValue())) {
                    this.value = Long.parseLong(parameter.stringValue());
                    break;
                } else {
                    throw new ExpressionException();
                }
            case DATEUNIT:
                throw new ExpressionException();
            case DOUBLE:
                throw new ExpressionException();
            default:
                throw new ExpressionException();
        }
    }

    /**
     * {@linkplain Calendar#HOUR} <br>
     * {@linkplain Calendar#MINUTE} <br>
     * {@linkplain Calendar#SECOND} <br>
     * {@linkplain Calendar#MILLISECOND} <br>
     * {@linkplain Calendar#DAY_OF_MONTH} <br>
     * {@linkplain Calendar#MONTH} <br>
     * {@linkplain Calendar#YEAR} <br>
     *
     * @return
     */
    public int getUnit() {
        return unit;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DateUnitParameter) {
            DateUnitParameter p = (DateUnitParameter) obj;
            return this.type == p.getType() && this.unit == p.unit && this.value.equals(p.value());
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(ExpressionParameter.getTypeName(this.type)).append(": ");
        str.append(this.value).append(' ');
        switch (this.unit) {
            case Calendar.DAY_OF_MONTH:
                str.append("day");
                break;
            case Calendar.MONTH:
                str.append("month");
                break;
            case Calendar.YEAR:
                str.append("year");
                break;
            case Calendar.HOUR:
                str.append("hour");
                break;
            case Calendar.MINUTE:
                str.append("minute");
                break;
            case Calendar.SECOND:
                str.append("second");
                break;
            case Calendar.MILLISECOND:
                str.append("millisecond");
                break;
            default:
                str.append("unknown" + this.unit);
                break;
        }
        str.append("]");
        return str.toString();
    }

    public Parameter copy() {
        DateUnitParameter obj = new DateUnitParameter();
        obj.value = this.value;
        obj.type = this.type;
        obj.unit = this.unit;
        return obj;
    }

}
