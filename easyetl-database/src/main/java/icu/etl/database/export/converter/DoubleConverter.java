package icu.etl.database.export.converter;

public class DoubleConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        Double value = this.resultSet.getDouble(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
