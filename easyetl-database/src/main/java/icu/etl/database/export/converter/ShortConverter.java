package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;

public class ShortConverter extends AbstractConverter {

    public void init() throws IOException, SQLException {
    }

    public void execute() throws IOException, SQLException {
        Short value = this.resultSet.getShort(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
