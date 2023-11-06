package icu.etl.expression.parameter;

/**
 * 三目运算 条件 ? 结果true : 结果false
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-20 14:16:57
 */
public class TwoParameter extends ExpressionParameter {

    private Parameter trueRun;
    private Parameter falseRun;

    public TwoParameter() {
        super();
    }

    public Parameter getTrueRun() {
        return trueRun;
    }

    public void setTrueRun(Parameter trueRun) {
        this.trueRun = trueRun;
    }

    public Parameter getFalseRun() {
        return falseRun;
    }

    public void setFalseRun(Parameter falseRun) {
        this.falseRun = falseRun;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TwoParameter) {
            TwoParameter p = (TwoParameter) obj;
            return this.trueRun.equals(p.trueRun) && this.falseRun.equals(p.falseRun);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append("true: ");
        buf.append(this.trueRun.toString());
        buf.append("; false: ");
        buf.append(this.falseRun.toString());
        buf.append("]");
        return buf.toString();
    }

    public Parameter copy() {
        TwoParameter obj = new TwoParameter();
        obj.setType(getType());
        obj.setValue(value());
        obj.trueRun = this.trueRun == null ? null : this.trueRun.copy();
        obj.falseRun = this.falseRun == null ? null : this.falseRun.copy();
        return obj;
    }

}
