package icu.etl.expression;

import java.math.BigDecimal;
import java.util.Date;

import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 表达式运算 <br>
 * 解析字符串表达式，根据运算符号运算 <Br>
 * <br>
 * 支持的运算符号的有： <br>
 *
 * <table border='1'>
 * <caption>运算符优先级</caption>
 * <tr>
 * <th>符号</th>
 * <th>优先级</th>
 * </tr>
 *
 * <tr>
 * <td>()</td>
 * <td>1</td>
 * </tr>
 *
 * <tr>
 * <td>+(正) -(负)</td>
 * <td>2</td>
 * </tr>
 *
 * <tr>
 * <td>* / %</td>
 * <td>3</td>
 * </tr>
 *
 * <tr>
 * <td>+(加) -(减)</td>
 * <td>4</td>
 * </tr>
 *
 * <tr>
 * <td>&nbsp;</td>
 * <td>5</td>
 * </tr>
 *
 * <tr>
 * <td>{@literal < <= > >=}</td>
 * <td>6</td>
 * </tr>
 *
 * <tr>
 * <td>== != in</td>
 * <td>7</td>
 * </tr>
 *
 * <tr>
 * <td>&nbsp;</td>
 * <td>8</td>
 * </tr>
 *
 * <tr>
 * <td>&nbsp;</td>
 * <td>9</td>
 * </tr>
 *
 * <tr>
 * <td>&nbsp;</td>
 * <td>10</td>
 * </tr>
 *
 * <tr>
 * <td>{@literal &&}</td>
 * <td>11</td>
 * </tr>
 *
 * <tr>
 * <td>||</td>
 * <td>12</td>
 * </tr>
 *
 * <tr>
 * <td>?:</td>
 * <td>13</td>
 * </tr>
 *
 * <tr>
 * <td>&nbsp;</td>
 * <td>14</td>
 * </tr>
 *
 * </table>
 * <br>
 * <code>优先级按照从高到低的顺序书写，也就是优先级为1的优先级最高，优先级14的优先级最低 </code><br>
 * <br>
 * <br>
 * <p>
 * 典型使用案例: <br>
 * {@linkplain Expression} obj = new {@link #Expression(String)}; <br>
 * obj.{@link #decimalValue()} <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-14 11:39:37
 */
public class Expression {
    private final static Log log = LogFactory.getLog(Expression.class);

    /** 表达式 */
    protected String expression;

    /** 运算结果 */
    protected Parameter result;

    /** 运算公式 */
    protected Formula formula;

    /**
     * 初始化
     *
     * @param str 表达式语句
     */
    public Expression(String str) {
        this(Parser.getFormat(), str);
    }

    /**
     * 初始化
     *
     * @param format 表达式解析器
     * @param str    表达式
     */
    public Expression(Parser format, String str) {
        if (log.isTraceEnabled()) {
            log.trace(ResourcesUtils.getExpressionMessage(6, str));
        }

        this.expression = str;
        this.formula = format.parse(this.expression);
        this.result = this.formula.execute();
    }

    /**
     * 返回运算公式
     *
     * @return 运算公式
     */
    public Formula getFormula() {
        return this.formula;
    }

    /**
     * 返回运算结果
     *
     * @return null表示不存在运算结果
     */
    public Object value() {
        return this.result == null ? null : this.result.value();
    }

    /**
     * 运算结果的数据类型
     *
     * @return -1表示不存在结果
     */
    public int getType() {
        return this.result == null ? Parameter.UNKNOWN : this.result.getType();
    }

    public String toString() {
        return this.expression;
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 BigDecimal 类型 <br>
     * 如果运算结果是 long 类型，转换为 BigDecimal 类型 <br>
     * 如果运算结果是 String 类型，转换为 BigDecimal 类型 <br>
     *
     * @return 运算结果
     */
    public BigDecimal decimalValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return BigDecimal.valueOf(this.result.doubleValue());
            case Parameter.LONG:
                return new BigDecimal(this.result.longValue());
            case Parameter.STRING:
                return new BigDecimal(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), BigDecimal.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 Double 类型 <br>
     * 如果运算结果是 long 类型，转换为 Double 类型 <br>
     * 如果运算结果是 String 类型，转换为 Double 类型 <br>
     *
     * @return 运算结果
     */
    public Double doubleValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return this.result.doubleValue();
            case Parameter.LONG:
                return this.result.longValue().doubleValue();
            case Parameter.STRING:
                return new Double(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Double.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 Float 类型 <br>
     * 如果运算结果是 long 类型，转换为 Float 类型 <br>
     * 如果运算结果是 String 类型，转换为 Float 类型 <br>
     *
     * @return 运算结果
     */
    public Float floatValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return this.result.doubleValue().floatValue();
            case Parameter.LONG:
                return this.result.longValue().floatValue();
            case Parameter.STRING:
                return new Float(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Float.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 Integer 类型 <br>
     * 如果运算结果是 long 类型，转换为 Integer 类型 <br>
     * 如果运算结果是 String 类型，转换为 Integer 类型 <br>
     *
     * @return 运算结果
     */
    public Integer intValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return this.result.doubleValue().intValue();
            case Parameter.LONG:
                return this.result.longValue().intValue();
            case Parameter.STRING:
                return new Integer(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Integer.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 Long 类型 <br>
     * 如果运算结果是 long 类型，转换为 Long 类型 <br>
     * 如果运算结果是 String 类型，转换为 Long 类型 <br>
     * 如果运算结果是 Date 类型，返回日期的时间戳 <br>
     *
     * @return 运算结果
     */
    public Long longValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return this.result.doubleValue().longValue();
            case Parameter.LONG:
                return this.result.longValue();
            case Parameter.STRING:
                return new Long(this.result.stringValue());
            case Parameter.DATE:
                return this.result.dateValue().getTime();
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Long.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，返回执行结果<br>
     * 如果运算结果是 double 类型，会抛出异常 <br>
     * 如果运算结果是 long 类型，会抛出异常 <br>
     * 如果运算结果是 String 类型，转换为 Boolean 类型 <br>
     *
     * @return 运算结果
     */
    public Boolean booleanValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.BOOLEAN:
                return this.result.booleanValue();
            case Parameter.STRING:
                return Boolean.valueOf(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Boolean.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，会抛出异常 <br>
     * 如果运算结果是 double 类型，转换为 Short 类型 <br>
     * 如果运算结果是 long 类型，转换为 Short 类型 <br>
     * 如果运算结果是 String 类型，转换为 Short 类型 <br>
     *
     * @return 运算结果
     */
    public Short shortValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.DOUBLE:
                return this.result.doubleValue().shortValue();
            case Parameter.LONG:
                return this.result.longValue().shortValue();
            case Parameter.STRING:
                return new Short(this.result.stringValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Short.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 boolean 类型，转换为 String 类型 <br>
     * 如果运算结果是 double 类型，转换为 String 类型 <br>
     * 如果运算结果是 long 类型，转换为 String 类型 <br>
     * 如果运算结果是 String 类型，转换为 String 类型 <br>
     * 如果运算结果是 Date 类型，转换为 String 类型 <br>
     *
     * @return 运算结果
     */
    public String stringValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.BOOLEAN:
                return String.valueOf(this.result.booleanValue());
            case Parameter.DOUBLE:
                return String.valueOf(this.result.doubleValue());
            case Parameter.LONG:
                return String.valueOf(this.result.longValue());
            case Parameter.STRING:
                return this.result.stringValue();
            case Parameter.DATE:
                return StringUtils.toString(this.result.dateValue());
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), String.class.getSimpleName()));
        }
    }

    /**
     * 返回运算结果 <br>
     * 如果运算结果是 long 类型，作为时间戳初始化一个日期对象 <br>
     * 如果运算结果是 String 类型，转换为 Date 类型 <br>
     * 如果运算结果是 Date 类型，直接返回 <br>
     *
     * @return 运算结果
     */
    public Date dateValue() {
        if (this.result == null) {
            return null;
        }

        int type = this.result.getType();
        switch (type) {
            case Parameter.LONG:
                return new Date(this.result.longValue());
            case Parameter.STRING:
                return Dates.parse(this.result.stringValue());
            case Parameter.DATE:
                return this.result.dateValue();
            default:
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(1, this.expression, ExpressionParameter.getTypeName(type), Date.class.getSimpleName()));
        }
    }

}
