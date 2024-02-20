package icu.etl.database.mysql.expconv;

import icu.etl.database.db2.format.DB2DoubleFormat;
import icu.etl.database.export.converter.AbstractConverter;

public class DoubleConverter extends AbstractConverter {

    protected DB2DoubleFormat format;

    public void init() throws Exception {
        this.format = new DB2DoubleFormat();
    }

    public void execute() throws Exception {
        Double value = this.resultSet.getDouble(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = this.format.format(value).toString();
        }
    }

}
