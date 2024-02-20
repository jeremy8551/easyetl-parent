package icu.etl.database.load.converter;

import java.sql.Types;

public class FloatConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.FLOAT);
        } else {
            this.statement.setFloat(this.position, new Float(value));
        }
    }

}
