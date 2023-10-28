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
     * 根据字符串参数str内容转为对应的类型参数
     *
     * @param str
     */
    public ExpressionParameter(String str) {
        // 解析整数
        if (StringUtils.isLong(str)) {
            this.type = LONG;
            this.value = new Long(StringUtils.trimBlank(str));
        }

        // 解析小数
        else if (StringUtils.isDouble(str)) {
            this.type = DOUBLE;
            this.value = new Double(str);
        }

        // 解析布尔值
        else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
            this.type = BOOLEAN;
            this.value = Boolean.parseBoolean(str);
        }

        // 默认作为字符串
        else {
            this.type = STRING;
            this.value = str;
        }
    }

    /**
     * 整数参数
     *
     * @param value
     */
    public ExpressionParameter(int value) {
        this.type = LONG;
        this.value = new Long(value);
    }

    /**
     * 初始化
     *
     * @param value
     */
    public ExpressionParameter(Object value) {
        if (value instanceof String) {
            this.type = STRING;
            this.value = value;
        } else if (value instanceof Number) {
            String str = value.toString();
            if (str.indexOf('.') == -1) {
                this.type = LONG;
                this.value = new Long(str);
            } else {
                this.type = DOUBLE;
                this.value = new Double(str);
            }
        } else if (value instanceof Boolean) {
            this.type = BOOLEAN;
            this.value = value;
        } else if (value instanceof Date) {
            this.type = DATE;
            this.value = value;
        } else if (value != null && value.getClass().isArray()) {
            this.type = ARRAY;
            this.value = value;
        } else {
            this.type = UNKNOWN;
            this.value = value;
        }
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
