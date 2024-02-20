package icu.etl.database.load.converter;

import java.sql.Types;

public class IntegerConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.INTEGER);
        } else {
            this.statement.setInt(this.position, Integer.parseInt(value));
        }
    }

}