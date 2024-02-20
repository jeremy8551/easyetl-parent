package icu.etl.database.export.converter;

public class FloatConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        Float value = this.resultSet.getFloat(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
