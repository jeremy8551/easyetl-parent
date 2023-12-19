package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

public class LongConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.FLOAT);
        } else {
            this.statement.setLong(this.position, Long.parseLong(value));
        }
    }

}
