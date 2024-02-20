package icu.etl.database.load.converter;

import java.sql.Types;

public class ByteConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.BIT);
        } else {
            this.statement.setByte(this.position, Byte.parseByte(value));
        }
    }

}
