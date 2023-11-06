package icu.etl.database;

import java.io.IOException;

/**
 * 表示数据库表中某列的配置信息
 *
 * @author jeremy8551@qq.com
 */
public interface DatabaseTableColumn extends Cloneable {

    /**
     * 表名称
     *
     * @return
     */
    String getTableName();

    /**
     * 表模式（可为 null）
     *
     * @return
     */
    String getTableSchema();

    /**
     * 表类别（可为 null）
     *
     * @return
     */
    String getTableCatalog();

    /**
     * 返回表的全名
     *
     * @return
     */
    String getTableFullName();

    /**
     * 列名称
     *
     * @return
     */
    String getName();

    /**
     * "NO" 表示明确不允许列使用 NULL 值，"YES" 表示可能允许列使用 NULL 值。空字符串表示没人知道是否允许使用 null 值。
     *
     * @return
     */
    String getNullAble();

    /**
     * 来自 java.sql.Types 的 SQL 类型
     *
     * @return
     */
    int getSqlType();

    /**
     * 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的；如：char varchar int decimal 等
     *
     * @return
     */
    String getFieldType();

    /**
     * 数据源中字段的类型全名，如：char(20), decimal(20,3)
     *
     * @return
     */
    String getFieldName();

    /**
     * 列的长度。对于 char 或 date 类型，列的大小是最大字符数，对于 numeric 和 decimal 类型，列的大小就是精度。
     *
     * @return
     */
    int length();

    /**
     * 描述列的注释（可为 null）
     *
     * @return
     */
    String getRemark();

    /**
     * 返回字段的默认值（可为 null），引号扩起来时表示字符串
     *
     * @return
     */
    String getDefault();

    /**
     * 返回字段默认值的定义表达式, 如: 'text' 10
     *
     * @return
     */
    String getDefaultValue();

    /**
     * CHAR_OCTET_LENGTH 对于 char 类型，该长度是列中的最大字节数
     *
     * @return
     */
    int getMaxLength();

    /**
     * 表中的列的索引（从 1 开始）
     *
     * @return
     */
    int getPosition();

    /**
     * 小数部分的位数
     *
     * @return
     */
    int getDigit();

    /**
     * 基数（通常为 10 或 2）
     *
     * @return
     */
    int getRadix();

    /**
     * 比较
     *
     * @param column 数据库表列信息
     * @return 0表示字段相同 小于零表示字段1小于字段2 大于零表示字段1大于字段2
     */
    int compareTo(DatabaseTableColumn column);

    /**
     * 扩展字段长度 <br>
     * 如果 value 参数超出了 col 字段类型长度, 则扩展字段信息中的字段长度并返回 true <br>
     * 如果 value 参数未超出 col 字段类型长度, 则不对字段信息做任何操作并返回 false <br>
     *
     * @param value       实际值
     * @param charsetName 字符集
     * @return true表示扩展字段
     * @throws IOException
     */
    boolean expandLength(String value, String charsetName) throws IOException;

    /**
     * YES---如果列是自动递增的 <br>
     * NO ---如果列未自动递增 <br>
     * 空字符串---如果无法确定列是否自动递增 <br>
     *
     * @return
     */
    String getIncrement();

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseTableColumn clone();

    /**
     * 返回字段类型信息
     *
     * @return
     */
    DatabaseType getType();

}