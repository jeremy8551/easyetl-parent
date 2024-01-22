package icu.etl.database;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface DatabaseProcedureParameter extends Cloneable {

    /**
     * 参数值表达式: 'yyyy-MM-dd' 或 ? 等形式
     *
     * @return 参数表达式
     */
    String getExpression();

    /**
     * 参数值表达式: 'yyyy-MM-dd' 或 ? 等形式
     *
     * @param expression 参数表达式
     */
    void setExpression(String expression);

    /**
     * 参数值
     *
     * @return 参数值
     */
    Object getValue();

    /**
     * 参数值
     *
     * @param value 参数值
     */
    void setValue(Object value);

    /**
     * 占位符参数的位置（从1开始）
     *
     * @return 占位符的序号, 0表示参数没有设置占位符?
     */
    int getPlaceholder();

    /**
     * 当前参数位是占位符?, 则需要设置占位符的序号（从1开始）
     *
     * @param position 占位符的序号（从1开始）0表示参数没有设置占位符?
     */
    void setPlaceholder(int position);

    /**
     * 输出参数的序号（从1开始）, 0表示非输出参数
     *
     * @return 序号
     */
    int getOutIndex();

    /**
     * 存储过程名
     *
     * @return 存储过程名
     */
    String getProcedureName();

    /**
     * 存储过程归属schema
     *
     * @return 模式名
     */
    String getProcedureSchema();

    /**
     * 参数在数据库存储过程中参数的位置（从1开始）
     *
     * @return 位置信息
     */
    int getPosition();

    /**
     * 参数名
     *
     * @return 参数名
     */
    String getName();

    /**
     * 参数类型 CHARACTER INTEGER
     *
     * @return 参数类型
     */
    String getFieldType();

    /**
     * 参数对应的 java.sql.Types 类型
     *
     * @return 参数类型
     */
    int getSqlType();

    /**
     * true表示参数可以为null
     *
     * @return 返回true表示参数可能为null false表示参数不可能为null
     */
    boolean isNullEnable();

    /**
     * 参数长度
     *
     * @return 参数长度
     */
    int length();

    /**
     * 参数精度
     *
     * @return 参数精度
     */
    int getScale();

    /**
     * 参数模式 IN OUT
     *
     * @return 参数模式
     */
    int getMode();

    /**
     * 判断数值是否为数据库存储过程输出型参数 <br>
     * {@linkplain DatabaseProcedure#PARAM_OUT_MODE} <br>
     * {@linkplain DatabaseProcedure#PARAM_INOUT_MODE} <br>
     *
     * @return 返回true表示参数是输出 false表示参数是输入
     */
    boolean isOutMode();

    /**
     * 判断输入参数str是否为合法数据库存储过程参数名, 即首字母以$开始只有字母、数字、下划线组成。
     *
     * @return 返回true表示参数名合法 false表示参数名不合法
     */
    boolean isExpression();

    /**
     * 设置执行存储过程输入参数
     *
     * @param statement 输入执行存储过程的 CallableStatement 对象
     * @throws SQLException 数据库错误
     */
    void setStatement(CallableStatement statement) throws SQLException;

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseProcedureParameter clone();

}