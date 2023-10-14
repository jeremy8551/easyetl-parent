package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;

import icu.etl.util.StringUtils;

public class TimeConverter extends DateConverter {

    public void init() throws IOException, SQLException {
        String pattern = StringUtils.defaultString((String) this.getAttribute(PARAM_TIMEFORMAT), "hh:mm:ss");
        this.format = new SimpleDateFormat(pattern);
    }

    public void execute() throws IOException, SQLException {
        Time value = this.resultSet.getTime(this.column);
        if (value != null) {
            this.array[this.column] = this.format.format(value);
        }
    }

}
