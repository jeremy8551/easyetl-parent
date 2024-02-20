package icu.etl.database.db2.expconv;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;

import icu.etl.database.db2.format.DB2DecimalFormat;
import icu.etl.database.export.converter.AbstractConverter;

public class BigDecimalConverter extends AbstractConverter {

    /** decimal字段中数字的位数 */
    private int precision;

    /** decimal字段中小数点后的位数 */
    private int scale;

    /** 格式化工具 */
    private DB2DecimalFormat format;

    public void init() throws Exception {
        ResultSetMetaData rsmd = this.resultSet.getMetaData();
        this.scale = rsmd.getScale(this.column);
        this.precision = rsmd.getPrecision(this.column);
        this.format = new DB2DecimalFormat();
        this.format.applyPattern(this.precision, this.scale);
    }

    public void execute() throws Exception {
        BigDecimal value = this.resultSet.getBigDecimal(this.column);
        if (value == null) {
            this.array[this.column] = "";
        } else {
            this.format.format(value);
            this.array[this.column] = new String(this.format.getChars(), 0, this.format.length());
        }
    }
}
