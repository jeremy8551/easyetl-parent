package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;

public class BooleanConverter extends AbstractConverter {

    public void init() throws IOException, SQLException, ParseException {
    }

    public void execute(String value) throws IOException, SQLException {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.BOOLEAN);
        } else {
            this.statement.setBoolean(this.position, Boolean.parseBoolean(value));
        }
    }

}
