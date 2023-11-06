package icu.etl.database.internal;

import java.sql.ResultSet;
import java.sql.SQLException;

import icu.etl.database.DatabaseType;
import icu.etl.database.Jdbc;

public class StandardDatabaseType implements DatabaseType {

    private String name;
    private Integer sqlType;
    private Integer sqlDataType;
    private Integer precision;
    private String literalPrefix;
    private String literalSuffix;
    private String createParams;
    private Integer nullAble;
    private Integer caseSesitive;
    private Integer maximumScale;
    private Integer minimumScale;
    private Integer numberPrecRadix;
    private Integer fixedPrecScale;
    private Integer searchable;
    private Integer autoIncrement;
    private Integer unsignedAttribute;
    private Integer sqlDateTimeSub;
    private String localTypeName;

    /**
     * 初始化
     *
     * @param obj
     */
    public StandardDatabaseType(StandardDatabaseType obj) {
        this.name = obj.name;
        this.sqlType = obj.sqlType;
        this.sqlDataType = obj.sqlDataType;
        this.precision = obj.precision;
        this.literalPrefix = obj.literalPrefix;
        this.literalSuffix = obj.literalSuffix;
        this.createParams = obj.createParams;
        this.nullAble = obj.nullAble;
        this.caseSesitive = obj.caseSesitive;
        this.maximumScale = obj.maximumScale;
        this.minimumScale = obj.minimumScale;
        this.numberPrecRadix = obj.numberPrecRadix;
        this.fixedPrecScale = obj.fixedPrecScale;
        this.searchable = obj.searchable;
        this.autoIncrement = obj.autoIncrement;
        this.unsignedAttribute = obj.unsignedAttribute;
        this.sqlDateTimeSub = obj.sqlDateTimeSub;
        this.localTypeName = obj.localTypeName;
    }

    /**
     * 初始化
     *
     * @param resultSet
     * @throws SQLException
     */
    public StandardDatabaseType(ResultSet resultSet) throws SQLException {
        this.name = resultSet.getString("TYPE_NAME");
        this.sqlType = Jdbc.getInt(resultSet, "DATA_TYPE");
        this.precision = Jdbc.getInt(resultSet, "PRECISION");
        this.literalPrefix = Jdbc.getString(resultSet, "LITERAL_PREFIX");
        this.literalSuffix = Jdbc.getString(resultSet, "LITERAL_SUFFIX");
        this.createParams = Jdbc.getString(resultSet, "CREATE_PARAMS");
        this.nullAble = Jdbc.getInt(resultSet, "NULLABLE");
        this.caseSesitive = Jdbc.getInt(resultSet, "CASE_SENSITIVE");
        this.searchable = Jdbc.getInt(resultSet, "SEARCHABLE");
        this.unsignedAttribute = Jdbc.getInt(resultSet, "UNSIGNED_ATTRIBUTE");
        this.fixedPrecScale = Jdbc.getInt(resultSet, "FIXED_PREC_SCALE");
        this.autoIncrement = Jdbc.getInt(resultSet, "AUTO_INCREMENT");
        this.localTypeName = Jdbc.getString(resultSet, "LOCAL_TYPE_NAME");
        this.minimumScale = Jdbc.getInt(resultSet, "MINIMUM_SCALE");
        this.maximumScale = Jdbc.getInt(resultSet, "MAXIMUM_SCALE");
        this.numberPrecRadix = Jdbc.getInt(resultSet, "NUM_PREC_RADIX");
        this.sqlDataType = Jdbc.getInt(resultSet, "SQL_DATA_TYPE");
        this.sqlDateTimeSub = Jdbc.getInt(resultSet, "SQL_DATETIME_SUB");
    }

    public StandardDatabaseType clone() {
        return new StandardDatabaseType(this);
    }

    public String getName() {
        return name;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public Integer getPrecision() {
        return precision;
    }

    public String getTextPrefix() {
        return literalPrefix;
    }

    public String getTextSuffix() {
        return literalSuffix;
    }

    public String toText(CharSequence value) {
        StringBuilder buf = new StringBuilder();
        buf.append(this.literalPrefix);
        buf.append(value);
        buf.append(this.literalSuffix);
        return buf.toString();
    }

    public String getExpression() {
        return createParams;
    }

    public Integer getNullAble() {
        return nullAble;
    }

    public Integer getCaseSesitive() {
        return caseSesitive;
    }

    public Integer getMaxScale() {
        return maximumScale;
    }

    public Integer getMinScale() {
        return minimumScale;
    }

    public Integer getScale() {
        return fixedPrecScale;
    }

    public Integer getRadix() {
        return numberPrecRadix;
    }

    public Integer getSearchable() {
        return searchable;
    }

    public Integer getAutoIncrement() {
        return autoIncrement;
    }

    public Integer getUnsigned() {
        return unsignedAttribute;
    }

    public String getLocalName() {
        return localTypeName;
    }

//	public Integer getSqlDataType() {
//		return sqlDataType;
//	}
//	
//	public Integer getSqlDateTimeSub() {
//		return sqlDateTimeSub;
//	}

    public String toString() {
        return "StandardDatabaseType [name=" + name + ", sqlType=" + sqlType + ", sqlDataType=" + sqlDataType + ", precision=" + precision + ", literalPrefix=" + literalPrefix + ", literalSuffix=" + literalSuffix + ", createParams=" + createParams + ", nullAble=" + nullAble + ", caseSesitive=" + caseSesitive + ", maximumScale=" + maximumScale + ", minimumScale=" + minimumScale + ", numberPrecRadix=" + numberPrecRadix + ", fixedPrecScale=" + fixedPrecScale + ", searchable=" + searchable + ", autoIncrement=" + autoIncrement + ", unsignedAttribute=" + unsignedAttribute + ", sqlDateTimeSub=" + sqlDateTimeSub + ", localTypeName=" + localTypeName + "]";
    }

}
