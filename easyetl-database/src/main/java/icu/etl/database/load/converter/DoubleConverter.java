package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

public class DoubleConverter extends AbstractConverter {

    public void init() throws IOException, SQLException {
    }

    public void execute(String value) throws IOException, SQLException {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.DOUBLE);
        } else {
            this.statement.setDouble(this.position, new Double(value));
        }
    }

}
