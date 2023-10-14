package icu.etl.database;

import java.sql.DatabaseMetaData;

/**
 * 数据库支持的数据类型 <br>
 * <br>
 * precision意为“精密度、精确”，表示该字段的有效数字位数了。 <br>
 * scale意为“刻度、数值范围”，表示该字段的小数位数。 <br>
 * radix：可选参数，数字基数，可以理解为进制，范围为2~36 <br>
 * <br>
 * 举个简单的例子 <br>
 * 123.45：precision = 5 ，scale = 2 <br>
 * precision 数据长度 <br>
 * scale 小数长度 <br>
 * <br>
 * <br>
 * sql boolean 0/1表示true或False <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-23
 */
public interface DatabaseType {

    /**
     * 返回字段类型名, 如: char 或 varchar 或 date 或 time 或 timestamp
     *
     * @return
     */
    String getName();

    /**
     * 返回字段类型对应的 {@link java.sql.Types} 值
     *
     * @return
     */
    Integer getSqlType();

    /**
     * 返回整型数的支持的最大数字位数<br>
     * PRECISION <br>
     *
     * @return
     */
    Integer getPrecision();

    /**
     * 返回引用文字的前缀，如: G' 或 null <br>
     * LITERAL_PREFIX <br>
     *
     * @return
     */
    String getTextPrefix();

    /**
     * 返回引用文字的后缀, 如: ' 或 null <br>
     * LITERAL_SUFFIX <br>
     *
     * @return
     */
    String getTextSuffix();

    /**
     * 返回引用文字信息
     *
     * @param value 文字信息
     * @return
     */
    String toText(CharSequence value);

    /**
     * 返回字段参数类型: LENGTH 或 PRECISION,SCALE 或 PRECISION 或 null <br>
     * CREATE_PARAMS <br>
     *
     * @return
     */
    String getExpression();

    /**
     * 返回 true 表示字段可以是null <br>
     * NULLABLE <br>
     * {@linkplain DatabaseMetaData#typeNoNulls} 不允许为空 <br>
     * {@linkplain DatabaseMetaData#typeNullable} 可以为空 <br>
     * {@linkplain DatabaseMetaData#typeNullableUnknown} 不知为是否可以为空 <br>
     *
     * @return
     */
    Integer getNullAble();

    /**
     * 返回 1 表示字段类型区分大小写，返回 0 或 null 表示字段类型不区分大小写 <br>
     * CASE_SENSITIVE <br>
     *
     * @return
     */
    Integer getCaseSesitive();

    /**
     * 返回字段类型支持支持的最大小数位 <br>
     * MAXIMUM_SCALE <br>
     *
     * @return
     */
    Integer getMaxScale();

    /**
     * 返回字段类型支持支持的最小小数位 <br>
     * MINIMUM_SCALE <br>
     *
     * @return
     */
    Integer getMinScale();

    /**
     * 返回数字精度基数 <br>
     * NUM_PREC_RADIX <br>
     *
     * @return 通常返回2进制或10进制
     */
    Integer getRadix();

    /**
     * 返回 1 表示有固定小数位，返回 0 或 null 表示小数位不是固定的 <br>
     * FIXED_PREC_SCALE <br>
     *
     * @return
     */
    Integer getScale();

    /**
     * 支出是否可以在where条件句中使用这种类型，有以下的可能值： <br>
     * SEARCHABLE <Br>
     * 返回值: <br>
     * {@linkplain DatabaseMetaData#typeSearchable} 所有where语句都可以使用这种数据类型<br>
     * {@linkplain DatabaseMetaData#typePredChar} 只可以用于 where … like 语句中 <br>
     * {@linkplain DatabaseMetaData#typePredBasic} 除了 where … like 语句外所有where语句都可以使用这种数据类型 <br>
     * {@linkplain DatabaseMetaData#typePredNone} 不可以用在where中<br>
     *
     * @return
     */
    Integer getSearchable();

    /**
     * 返回 1 表示数据类型可以自动增加值，返回 0 或 null 表示数据类型不可以自动增加值 <br>
     * AUTO_INCREMENT
     *
     * @return
     */
    Integer getAutoIncrement();

    /**
     * 返回 0 表示数据类型是有符号的，返回 1 或 null 表示数据类型是无符号的 <br>
     * UNSIGNED_ATTRIBUTE
     *
     * @return
     */
    Integer getUnsigned();

    /**
     * 被本地化了的数据类型名，可能返回 null 值 <br>
     * LOCAL_TYPE_NAME
     *
     * @return
     */
    String getLocalName();

//	/**
//	 * 未使用 <br>
//	 * SQL_DATETIME_SUB
//	 */
//	Integer getSqlDateTimeSub();

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseType clone();

}
