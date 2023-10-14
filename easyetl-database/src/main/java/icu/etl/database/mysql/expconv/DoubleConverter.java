package icu.etl.database.mysql.expconv;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.db2.format.DB2DoubleFormat;
import icu.etl.database.export.converter.AbstractConverter;

public class DoubleConverter extends AbstractConverter {

    protected DB2DoubleFormat format;

    public void init() throws IOException, SQLException {
        this.format = new DB2DoubleFormat();
    }

    public void execute() throws IOException, SQLException {
        Double value = this.resultSet.getDouble(this.column);
        if (!this.resultSet.wasNull()) {
            this.array[this.column] = this.format.format(value).toString();
        }
    }

}
