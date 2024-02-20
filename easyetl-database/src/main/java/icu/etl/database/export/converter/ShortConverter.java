package icu.etl.database.export.converter;

public class ShortConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute() throws Exception {
        Short value = this.resultSet.getShort(this.column);
        if (this.resultSet.wasNull()) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = value.toString();
        }
    }

}
