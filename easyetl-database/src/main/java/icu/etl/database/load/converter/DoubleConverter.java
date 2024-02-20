package icu.etl.database.load.converter;

import java.sql.Types;

public class DoubleConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.DOUBLE);
        } else {
            this.statement.setDouble(this.position, new Double(value));
        }
    }

}
