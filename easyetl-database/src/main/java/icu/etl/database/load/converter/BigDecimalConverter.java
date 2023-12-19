package icu.etl.database.load.converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

public class BigDecimalConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.DECIMAL);
        } else {
            this.statement.setBigDecimal(this.position, new BigDecimal(value));
        }
    }

}
