package icu.etl.database.export.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

import icu.etl.util.StringUtils;

public class DateConverter extends AbstractConverter {

    /** 日期格式化 */
    protected SimpleDateFormat format;

    public void init() throws Exception {
        String pattern = StringUtils.defaultString((String) this.getAttribute(PARAM_DATEFORMAT), "yyyy-MM-dd");
        this.format = new SimpleDateFormat(pattern);
    }

    public void execute() throws Exception {
        Date value = this.resultSet.getDate(this.column);
        if (value == null) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = this.format.format(value);
        }
    }

}
