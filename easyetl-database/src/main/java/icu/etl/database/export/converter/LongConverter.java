package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;

public class LongConverter extends AbstractConverter {

    public void init() throws IOException, SQLException {
    }

    public void execute() throws IOException, SQLException {
        Long value = this.resultSet.getLong(this.column);
        if (!this.resultSet.wasNull()) {
            this.array[this.column] = value.toString();
        }
    }

}
