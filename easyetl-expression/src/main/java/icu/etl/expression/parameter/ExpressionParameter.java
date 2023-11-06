package icu.etl.expression.parameter;

import java.util.Date;

import icu.etl.util.StringUtils;

/**
 * 表达式数值对象
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-19 15:16:10
 */
public class ExpressionParameter implements Parameter {

    /**
     * 将字符串参数转为表达式参数
     *
     * @param str 字符串
     * @return 表达式参数
     */
    public static ExpressionParameter parseStr(String str) {
        // 解析整数
        if (StringUtils.isLong(str)) {
            return new ExpressionParameter(Parameter.LONG, new Long(StringUtils.trimBlank(str)));
        }

        // 解析小数
        if (StringUtils.isDouble(str)) {
            return new ExpressionParameter(Parameter.DOUBLE, new Double(str));
        }

        // 解析布尔值
        if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
            return new ExpressionParameter(Parameter.BOOLEAN, Boolean.parseBoolean(str));
        }

        // 默认作为字符串
        return new ExpressionParameter(Parameter.UNKNOWN, str);
    }

    /**
     * 将参数转为表达式参数
     *
     * @param value 参数值
     * @return 表达式参数
     */
    public static ExpressionParameter parse(Object value) {
        if (value instanceof String) {
            return new ExpressionParameter(Parameter.STRING, value);
        }

        if (value instanceof Number) {
            String str = value.toString();
            if (str.indexOf('.') == -1) {
                return new ExpressionParameter(Parameter.LONG, new Long(str));
            } else {
                return new ExpressionParameter(Parameter.DOUBLE, new Double(str));
            }
        }

        if (value instanceof Boolean) {
            return new ExpressionParameter(Parameter.BOOLEAN, value);
        }

        if (value instanceof Date) {
            return new ExpressionParameter(Parameter.DATE, value);
        }

        if (value != null && value.getClass().isArray()) {
            return new ExpressionParameter(Parameter.ARRAY, value);
        }

        return new ExpressionParameter(Parameter.UNKNOWN, value);
    }

    /** 表示obj对象的类型 */
    protected int type;

    /** 结果对象 */
    protected Object value;

    /**
     * 初始化
     */
    public ExpressionParameter() {
        this.type = UNKNOWN;
    }

    /**
     * 初始化
     *
     * @param type  参数类型
     * @param value 参数值
     */
    public ExpressionParameter(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(Object obj) {
        this.value = obj;
    }

    public void execute() {
    }

    public Double doubleValue() {
        return (Double) this.value;
    }

    public Long longValue() {
        return (Long) this.value;
    }

    public String stringValue() {
        return (String) this.value;
    }

    public Boolean booleanValue() {
        return (Boolean) this.value;
    }

    public Date dateValue() {
        return (Date) this.value;
    }

    public Object value() {
        return this.value;
    }

    public Parameter copy() {
        return new ExpressionParameter(this.type, this.value);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter p = (Parameter) obj;
            return this.type == p.getType() && this.value.equals(p.value());
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append(ExpressionParameter.getTypeName(this.type)).append(": ");
        buf.append(StringUtils.toString(this.value));
        buf.append("]");
        return buf.toString();
    }

    /**
     * 把数值类型转换为字符串
     *
     * @param type
     * @return
     */
    public static String getTypeName(int type) {
        switch (type) {
            case BOOLEAN:
                return "Boolean";
            case DOUBLE:
                return "Double";
            case EXPRESS:
                return "Express";
            case LONG:
                return "Long";
            case STRING:
                return "String";
            case DATE:
                return "Date";
            case DATEUNIT:
                return "DateUnit";
            default:
                return "Unknown" + type;
        }
    }

}
