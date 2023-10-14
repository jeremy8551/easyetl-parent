package icu.etl.database.mysql.expconv;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.db2.format.DB2FloatFormat;
import icu.etl.database.export.converter.AbstractConverter;

public class RealConverter extends AbstractConverter {

    /** 格式化工具 */
    private DB2FloatFormat format;

    public void init() throws IOException, SQLException {
        this.format = new DB2FloatFormat();
    }

    public void execute() throws IOException, SQLException {
        Float value = this.resultSet.getFloat(this.column);
        if (!this.resultSet.wasNull()) {
            this.array[this.column] = this.format.format(value).toString();
        }
    }

}
