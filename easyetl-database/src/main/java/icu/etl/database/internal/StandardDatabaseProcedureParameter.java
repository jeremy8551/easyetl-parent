package icu.etl.database.internal;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;

import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

/**
 * 数据库存储过程参数
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-05-09
 */
public class StandardDatabaseProcedureParameter implements DatabaseProcedureParameter {
    private final static Log log = LogFactory.getLog(StandardDatabaseProcedureParameter.class);

    /** 存储过程名 */
    private String procedureName;

    /** 存储过程归属schema */
    private String procedureSchema;

    /** 参数在数据库存储过程中参数的位置（从1开始） */
    private int orderid;

    /** 输出参数的序号（从1开始）, 0表示非输出参数 */
    private int outIndex;

    /** 当前参数占位符的序号（从1开始） 0表示参数没有设置占位符? */
    private int placeholder;

    /** 参数名 */
    private String name;

    /** 参数类型 CHARACTER INTEGER */
    private String type;

    /** 参数对应的 java.sql.Types 类型 */
    private int typeId;

    /** true表示参数可以为null */
    private boolean nullEnable;

    /** 参数长度 */
    private int length;

    /** 参数精度 */
    private int scale;

    /** 参数模式 IN OUT */
    private int mode;

    /** 参数值 */
    private Object value;

    /** 参数值表达式: 'yyyy-MM-dd' 或 ? 等形式 */
    private String expression;

    /**
     * 初始化
     */
    public StandardDatabaseProcedureParameter() {
        super();
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(int pos) {
        this.placeholder = pos;
    }

    public int getOutIndex() {
        return outIndex;
    }

    /**
     * 输出参数的序号（从1开始）, 0表示非输出参数
     *
     * @param outIndex 参数的序号（从1开始）
     */
    public void setOutIndex(int outIndex) {
        this.outIndex = outIndex;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public String getProcedureSchema() {
        return procedureSchema;
    }

    public int getPosition() {
        return orderid;
    }

    public String getName() {
        return name;
    }

    public String getFieldType() {
        return type;
    }

    public int getSqlType() {
        return typeId;
    }

    public boolean isNullEnable() {
        return nullEnable;
    }

    public int length() {
        return length;
    }

    public int getScale() {
        return scale;
    }

    public int getMode() {
        return mode;
    }

    public boolean isOutMode() {
        return mode == DatabaseProcedure.PARAM_OUT_MODE || mode == DatabaseProcedure.PARAM_INOUT_MODE;
    }

    /**
     * 存储过程名
     *
     * @param procedureName 存储过程名
     */
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    /**
     * 存储过程归属schema
     *
     * @param procedureSchema 存储过程归属schema
     */
    public void setProcedureSchema(String procedureSchema) {
        this.procedureSchema = procedureSchema;
    }

    /**
     * 参数在数据库存储过程中参数的位置（从1开始）
     *
     * @param orderid 参数的位置（从1开始）
     */
    public void setPosition(int orderid) {
        this.orderid = orderid;
    }

    /**
     * 参数名
     *
     * @param name 参数名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 参数类型 CHARACTER INTEGER
     *
     * @param type 参数类型
     */
    public void setFieldType(String type) {
        this.type = type;
    }

    /**
     * 参数对应的 {@linkplain java.sql.Types} 类型
     *
     * @param typeId {@linkplain java.sql.Types}
     */
    public void setSqlType(int typeId) {
        this.typeId = typeId;
    }

    /**
     * true表示参数可以为null
     *
     * @param isCanNull true表示参数可以为null
     */
    public void setCanNull(boolean isCanNull) {
        this.nullEnable = isCanNull;
    }

    /**
     * 参数长度
     *
     * @param length 参数长度
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * 参数精度
     *
     * @param scale 参数精度
     */
    public void setScale(int scale) {
        this.scale = scale;
    }

    /**
     * 参数模式 IN OUT
     *
     * @param mode 参数模式 IN OUT
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isExpression() {
        String str = this.getExpression();
        if (StringUtils.isBlank(str)) {
            return false;
        }

        str = StringUtils.trimBlank(str);
        if (str.length() <= 1 || str.charAt(0) != '$') {
            return false;
        }

        // 只能是字母数字下划线的组合，不能包含空格
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (StringUtils.isNumber(c) || StringUtils.isLetter(c) || c == '_') { // 变量名只能由罗马字母，数字 下划线组成
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    public void setStatement(CallableStatement statement) throws SQLException {
        Ensure.notNull(statement);

        int type = this.getSqlType();
        String expression = StringUtils.trimBlank(this.getExpression());
        boolean containQuotes = StringUtils.containsQuotation(expression);
        String str = StringUtils.unquote(expression);

        switch (type) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                if (StringUtils.isBlank(str)) {
                    if (containQuotes) {
                        statement.setString(this.getPlaceholder(), "");
                    } else {
                        statement.setNull(this.getPlaceholder(), type);
                    }
                } else {
                    statement.setString(this.getPlaceholder(), str);
                }
                break;

            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
                statement.setShort(this.getPlaceholder(), Short.parseShort(expression));
                break;

            case java.sql.Types.INTEGER:
                statement.setInt(this.getPlaceholder(), Integer.parseInt(expression));
                break;

            case java.sql.Types.BIGINT:
                statement.setLong(this.getPlaceholder(), Long.parseLong(expression));
                break;

            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                statement.setBigDecimal(this.getPlaceholder(), new BigDecimal(expression));
                break;

            case java.sql.Types.FLOAT:
            case java.sql.Types.REAL:
                statement.setFloat(this.getPlaceholder(), new Float(expression));
                break;

            case java.sql.Types.DOUBLE:
                statement.setDouble(this.getPlaceholder(), new Double(expression));
                break;

            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                if (StringUtils.isBlank(str)) {
                    statement.setNull(this.getPlaceholder(), type);
                } else {
                    statement.setDate(this.getPlaceholder(), new java.sql.Date(Dates.parse(str).getTime()));
                }
                break;

            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                byte[] bytes = StringUtils.toBytes(StringUtils.unquotation(expression), Settings.getFileEncoding());
                statement.setBinaryStream(this.getPlaceholder(), new ByteArrayInputStream(bytes), bytes.length);
                break;

            case java.sql.Types.BIT:
            case java.sql.Types.BOOLEAN:
                statement.setBoolean(this.getPlaceholder(), Boolean.valueOf(expression));
                break;

            case java.sql.Types.OTHER:
            case java.sql.Types.JAVA_OBJECT:
            case java.sql.Types.DISTINCT:
            case java.sql.Types.STRUCT:
            case java.sql.Types.ARRAY:
            case java.sql.Types.REF:
            case java.sql.Types.DATALINK:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
                throw new UnsupportedOperationException(String.valueOf(type));

            default:
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getMessage("database.standard.output.msg014", type));
                }

                statement.setString(this.getPlaceholder(), StringUtils.unquotation(expression));
                break;
        }
    }

    public DatabaseProcedureParameter clone() {
        DatabaseProcedureParameter obj = this;
        StandardDatabaseProcedureParameter copy = new StandardDatabaseProcedureParameter();
        copy.procedureName = obj.getProcedureName();
        copy.procedureSchema = obj.getProcedureSchema();
        copy.orderid = obj.getPosition();
        copy.name = obj.getName();
        copy.type = obj.getFieldType();
        copy.typeId = obj.getSqlType();
        copy.outIndex = obj.getOutIndex();
        copy.placeholder = obj.getPlaceholder();
        copy.nullEnable = obj.isNullEnable();
        copy.length = obj.length();
        copy.scale = obj.getScale();
        copy.mode = obj.getMode();
        copy.value = obj.getValue();
        copy.expression = obj.getExpression();
        return copy;
    }

}
