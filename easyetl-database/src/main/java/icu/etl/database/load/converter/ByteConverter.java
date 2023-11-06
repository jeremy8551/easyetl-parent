package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;

public class ByteConverter extends AbstractConverter {

    public void init() throws IOException, SQLException, ParseException {
    }

    public void execute(String value) throws IOException, SQLException {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.BIT);
        } else {
            this.statement.setByte(this.position, Byte.parseByte(value));
        }
    }

}
