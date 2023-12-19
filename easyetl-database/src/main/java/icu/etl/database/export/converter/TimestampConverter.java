package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import icu.etl.util.StringUtils;

public class TimestampConverter extends DateConverter {

    public void init() throws Exception {
        String pattern = StringUtils.defaultString((String) this.getAttribute(PARAM_TIMESTAMPFORMAT), "yyyy-MM-dd hh:mm:ss.SSSSSS");
        this.format = new SimpleDateFormat(pattern);
    }

    public void execute() throws Exception {
        Timestamp value = this.resultSet.getTimestamp(this.column);
        if (value == null) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = this.format.format(value);
        }
    }

}
