package icu.etl.database.export.converter;

import java.math.BigDecimal;

public class BigDecimalConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        BigDecimal value = this.resultSet.getBigDecimal(this.column);
        if (value == null) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
