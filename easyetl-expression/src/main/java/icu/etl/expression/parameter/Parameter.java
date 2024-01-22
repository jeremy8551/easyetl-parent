package icu.etl.expression.parameter;

import java.util.Date;

/**
 * 运算参数
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-21 15:02:32
 */
public interface Parameter {

    /**
     * 未设置参数
     */
    int UNKNOWN = -1;

    /**
     * 运算参数类型： 布尔类型数值
     */
    int BOOLEAN = 1;

    /**
     * 运算参数类型： long 类型数值
     */
    int LONG = 2;

    /**
     * 运算参数类型： double 类型数值
     */
    int DOUBLE = 3;

    /**
     * 运算参数类型： String 类型数值
     */
    int STRING = 4;

    /**
     * 运算参数类型： Date 类型数值
     */
    int DATE = 5;

    /**
     * 运算参数类型： 日期单位类型
     */
    int DATEUNIT = 6;

    /**
     * 运算参数类型： 数组类型
     */
    int ARRAY = 7;

    /**
     * 运算参数类型： 表达式类型
     */
    int EXPRESS = 9;

    /**
     * 运算参数类型
     *
     * @return 参考: <br>
     * <code>{@linkplain Parameter#BOOLEAN} </code> <br>
     * <code>{@linkplain Parameter#LONG} </code> <br>
     * <code>{@linkplain Parameter#DOUBLE} </code> <br>
     * <code>{@linkplain Parameter#STRING} </code> <br>
     * <code>{@linkplain Parameter#EXPRESS} </code> <br>
     * <code>{@linkplain Parameter#DATE} </code> <br>
     * <code>{@linkplain Parameter#DATEUNIT} </code> <br>
     * <code>{@linkplain Parameter#UNKNOWN} </code>
     */
    int getType();

    /**
     * 运算参数类型
     *
     * @param type 参考: <br>
     *             <code>{@linkplain Parameter#BOOLEAN} </code> <br>
     *             <code>{@linkplain Parameter#LONG} </code> <br>
     *             <code>{@linkplain Parameter#DOUBLE} </code> <br>
     *             <code>{@linkplain Parameter#STRING} </code> <br>
     *             <code>{@linkplain Parameter#EXPRESS} </code> <br>
     *             <code>{@linkplain Parameter#DATE} </code> <br>
     *             <code>{@linkplain Parameter#DATEUNIT} </code> <br>
     *             <code>{@linkplain Parameter#UNKNOWN} </code>
     */
    void setType(int type);

    /**
     * 设置参数值
     *
     * @param obj 参数值
     */
    void setValue(Object obj);

    /**
     * 返回参数值
     *
     * @return 参数值
     */
    Object value();

    /**
     * 执行运算操作
     */
    void execute();

    /**
     * 把执行结果转换为 Double 对象
     *
     * @return 执行结果
     */
    Double doubleValue();

    /**
     * 把执行结果转换为 Long 对象
     *
     * @return 执行结果
     */
    Long longValue();

    /**
     * 把执行结果转换为 String 对象
     *
     * @return 执行结果
     */
    String stringValue();

    /**
     * 把执行结果转换为 Boolean 对象
     *
     * @return 执行结果
     */
    Boolean booleanValue();

    /**
     * 把执行结果转换为 Date 对象
     *
     * @return 执行结果
     */
    Date dateValue();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    Parameter copy();

}