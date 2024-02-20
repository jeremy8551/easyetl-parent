package icu.etl.database.export.converter;

public class LongConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        Long value = this.resultSet.getLong(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
