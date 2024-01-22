package icu.etl.database.internal;

import java.io.IOException;
import java.math.BigDecimal;

import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseType;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库表字段信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-06
 */
public class StandardDatabaseTableColumn implements Cloneable, Comparable<DatabaseTableColumn>, DatabaseTableColumn {
    private final static Log log = LogFactory.getLog(StandardDatabaseTableColumn.class);

    private String name;
    private int sqlType;
    private String typeName;
    private int length;
    private String remarks;
    private String defaultValue;
    private int maxLength;
    private int position;
    private String isNullAble;
    private int digits;
    private int numPrecRadix;
    private String tableName;
    private String tableSchema;
    private String tableCatalog;
    private String tableFullName;
    private String autoIncrement;
    private DatabaseType type;
    private String fullTypeName;
    private boolean isChar;
    private boolean isFloat;
    private boolean isNchar;

    /**
     * 返回字段类型信息
     *
     * @return 字段类型信息
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * 设置字段类型信息
     *
     * @param type 字段类型信息
     */
    public void setType(DatabaseType type) {
        this.type = Ensure.notNull(type);

        // 生成字段类型全名
        String expression = this.type.getExpression();
        StringBuilder buf = new StringBuilder(15);
        buf.append(this.getFieldType());
        if (StringUtils.isBlank(expression)) {
        } else if (expression.equalsIgnoreCase("LENGTH")) {
            buf.append("(").append(this.length()).append(")");
        } else if (expression.equalsIgnoreCase("PRECISION,SCALE")) {
            buf.append("(").append(this.length()).append(", ").append(this.getDigit()).append(")");
        } else if (expression.equalsIgnoreCase("PRECISION")) {
            buf.append("(").append(this.length()).append(")");
        } else {
            throw new UnsupportedOperationException(this.getName() + " " + this.getFieldType() + " " + expression);
        }
        this.fullTypeName = buf.toString();

        // true-表示字符型字段（按字节存储）
        this.isChar = "LENGTH".equalsIgnoreCase(expression) && StringUtils.isNotBlank(type.getTextPrefix()) && type.getRadix() == null;

        // true-表示双字节型字符字段（按字符存储）
        this.isNchar = this.isChar && Numbers.inArray(type.getSqlType(), -15, 2011, -9); // 对应 Types.NCHAR Types.NCLOB Types.NVARCHAR

        // true-表示精度型字段
        this.isFloat = "PRECISION,SCALE".equalsIgnoreCase(expression) || "PRECISION".equalsIgnoreCase(expression);
    }

    public String getTableFullName() {
        return tableFullName;
    }

    public void setTableFullName(String tableName) {
        this.tableFullName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * 表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    /**
     * 表模式（可为 null）
     *
     * @param tableSchema 表模式
     */
    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableCatalog() {
        return tableCatalog;
    }

    /**
     * 编目（可为 null）
     *
     * @param tableCatalog 编目
     */
    public void setTableCatalog(String tableCatalog) {
        this.tableCatalog = tableCatalog;
    }

    /**
     * 列名称
     *
     * @param name 列名称
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNullAble() {
        return isNullAble;
    }

    /**
     * "NO" 表示明确不允许列使用 NULL 值，"YES" 表示可能允许列使用 NULL 值。空字符串表示没人知道是否允许使用 null 值。
     *
     * @param str 字符串
     */
    public void setNullAble(String str) {
        this.isNullAble = str == null ? "" : str.toUpperCase();
    }

    public int getSqlType() {
        return sqlType;
    }

    /**
     * 来自 {@linkplain java.sql.Types} 的 SQL 类型
     *
     * @param sqlType {@linkplain java.sql.Types}
     */
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public String getFieldType() {
        return typeName;
    }

    public String getFieldName() {
        return this.fullTypeName;
    }

    /**
     * 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的
     *
     * @param typeName 类型名称
     */
    public void setFieldType(String typeName) {
        this.typeName = typeName;
    }

    public int length() {
        return length;
    }

    /**
     * 列的大小。对于 char 或 date 类型，列的大小是最大字符数，对于 numeric 和 decimal 类型，列的大小就是精度。
     *
     * @param length 列的大小
     */
    public void setLength(int length) {
        this.length = length;
    }

    public boolean expandLength(String value, String charsetName) throws IOException {
        StandardDatabaseTableColumn column = this;

        // 只有一个参数，存在文本限定符，不存在进制
        if (this.isChar) {
            int size = this.isNchar ? value.length() : value.getBytes(charsetName).length;
            if (size > column.length()) {
                column.setLength(size);
                return true;
            } else {
                return false;
            }
        } else if (this.isFloat) {
            boolean modify = false;
            BigDecimal decimal = new BigDecimal(value);

            int precision = decimal.precision();
            if (precision > column.length()) {
                column.setLength(precision);
                modify = true;
            }

            int scale = decimal.scale();
            if (scale > column.getDigit()) {
                column.setDigit(scale);
                modify = true;
            }
            return modify;
        } else {
            return false;
        }
    }

//	public int getNullEnable() {
//		return isNullEnable;
//	}

    public String getRemark() {
        return remarks;
    }

    /**
     * 描述列的注释（可为 null）
     *
     * @param remarks 注释
     */
    public void setRemark(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 设置字段的默认值（可为 null）
     *
     * @param defaultValue 字段的默认值
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }

    public String getDefaultValue() {
        return this.type.toText(StringUtils.defaultString(this.getDefault(), ""));
    }

    public int getMaxLength() {
        return maxLength;
    }

    /**
     * CHAR_OCTET_LENGTH 对于 char 类型，该长度是列中的最大字节数
     *
     * @param length 最大字节数
     */
    public void setMaxLength(int length) {
        this.maxLength = length;
    }

    public int getPosition() {
        return position;
    }

    /**
     * 表中的列的索引（从 1 开始）
     *
     * @param position 索引信息
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public int getDigit() {
        return digits;
    }

    /**
     * 小数部分的位数
     *
     * @param n
     */
    public void setDigit(int n) {
        this.digits = n;
    }

    public int getRadix() {
        return numPrecRadix;
    }

    /**
     * 基数（通常为 10 或 2）
     *
     * @param n 基数
     */
    public void setRadix(int n) {
        this.numPrecRadix = n;
    }

    public String getIncrement() {
        return autoIncrement;
    }

    public void setIncrement(String incr) {
        this.autoIncrement = incr;
    }

    public String toString() {
        return this.name + "[" + this.position + "]";
    }

    public int compareTo(DatabaseTableColumn column) {
        return this.compareTo(this, column);
    }

    /**
     * 比较字段信息是否一致
     *
     * @param column1 字段1
     * @param column2 字段2
     * @return 0表示字段相同 小于零表示字段1小于字段2 大于零表示字段1大于字段2
     */
    private int compareTo(DatabaseTableColumn column1, DatabaseTableColumn column2) {
        if (column1 == null) {
            throw new NullPointerException();
        }
        if (column2 == null) {
            throw new NullPointerException();
        }
        String name1 = column1.getTableFullName();
        String name2 = column2.getTableFullName();

        int nameCompare = column1.getName().compareTo(column2.getName());
        if (nameCompare != 0) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName()));
            }
            return nameCompare;
        }

        if (column1.getSqlType() != column2.getSqlType()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getSqlType(), column2.getSqlType()));
            }
            return column1.getSqlType() - column2.getSqlType();
        }

        int typeNameCompare = column1.getFieldType().compareTo(column2.getFieldType());
        if (typeNameCompare != 0) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getFieldType(), column2.getFieldType()));
            }
            return typeNameCompare;
        }

        if (column1.length() != column2.length()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.length(), column2.length()));
            }
            return column1.length() - column2.length();
        }

        if (column1.getMaxLength() != column2.getMaxLength()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getMaxLength(), column2.getMaxLength()));
            }
            return column1.getMaxLength() - column2.getMaxLength();
        }

        if (column1.getPosition() != column2.getPosition()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getPosition(), column2.getPosition()));
            }
            return column1.getPosition() - column2.getPosition();
        }

        if (column1.getDigit() != column2.getDigit()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getDigit(), column2.getDigit()));
            }
            return column1.getDigit() - column2.getDigit();
        }

        if (column1.getRadix() != column2.getRadix()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg043", name1, column1.getPosition(), column1.getName(), name2, column2.getPosition(), column2.getName(), column1.getRadix(), column2.getRadix()));
            }
            return column1.getRadix() - column2.getRadix();
        }

        return 0;
    }

    public boolean equals(Object obj) {
        if (obj instanceof DatabaseTableColumn) {
            DatabaseTableColumn c = (DatabaseTableColumn) obj;
            return this.name.equals(c.getName()) //
                    && this.sqlType == c.getSqlType() //
                    && this.typeName.equalsIgnoreCase(c.getFieldType()) //
                    && this.length == c.length() //
//					&& this.isNullEnable == c.getNullEnable() //
                    && this.remarks.equalsIgnoreCase(c.getRemark()) //
                    && this.defaultValue.equals(c.getDefault()) //
                    && this.maxLength == c.getMaxLength() //
                    && this.position == c.getPosition() //
                    && this.isNullAble.equalsIgnoreCase(c.getNullAble()) //
                    && this.digits == c.getDigit() //
                    && this.numPrecRadix == c.getRadix() //
                    && this.tableName.equalsIgnoreCase(c.getTableName()) //
                    && this.tableSchema.equalsIgnoreCase(c.getTableSchema()) //
                    && this.tableCatalog.equalsIgnoreCase(c.getTableCatalog()) //
                    && this.tableFullName.equalsIgnoreCase(c.getTableFullName()) //
                    && this.autoIncrement.equalsIgnoreCase(c.getIncrement()) //
                    ;
        } else {
            return false;
        }
    }

    public StandardDatabaseTableColumn clone() {
        StandardDatabaseTableColumn col = new StandardDatabaseTableColumn();
        col.name = this.name;
        col.sqlType = this.sqlType;
        col.typeName = this.typeName;
        col.length = this.length;
        col.remarks = this.remarks;
        col.defaultValue = this.defaultValue;
        col.maxLength = this.maxLength;
        col.position = this.position;
        col.isNullAble = this.isNullAble;
        col.digits = this.digits;
        col.numPrecRadix = this.numPrecRadix;
        col.tableName = this.tableName;
        col.tableSchema = this.tableSchema;
        col.tableCatalog = this.tableCatalog;
        col.tableFullName = this.tableFullName;
        col.autoIncrement = this.autoIncrement;
        col.isChar = this.isChar;
        col.isNchar = this.isNchar;
        col.isFloat = this.isFloat;
        col.fullTypeName = this.fullTypeName;
        col.type = this.type;
        return col;
    }

}