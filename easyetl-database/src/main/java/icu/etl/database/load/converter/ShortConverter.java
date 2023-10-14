package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

public class ShortConverter extends AbstractConverter {

    public void init() throws IOException, SQLException {
    }

    public void execute(String value) throws IOException, SQLException {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.SMALLINT);
        } else {
            this.statement.setShort(this.position, Short.parseShort(value));
        }
    }

}
