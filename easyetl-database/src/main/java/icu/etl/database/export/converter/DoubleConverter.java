package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;

public class DoubleConverter extends AbstractConverter {

    public void init() throws IOException, SQLException {
    }

    public void execute() throws IOException, SQLException {
        Double value = this.resultSet.getDouble(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
