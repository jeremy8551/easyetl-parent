package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;

public class IntegerConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        Integer value = this.resultSet.getInt(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
