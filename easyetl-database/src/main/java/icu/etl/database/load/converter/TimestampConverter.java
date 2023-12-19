package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import icu.etl.util.StringUtils;

/**
 * 把字符串转为时间撮
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-11-08
 */
public class TimestampConverter extends DateConverter {

    public void init() throws Exception {
        this.format = new SimpleDateFormat();
        this.format.applyPattern(StringUtils.defaultString((String) this.getAttribute(PARAM_TIMESTAMPFORMAT), "yyyy-MM-dd HH:mm:ss"));
    }

    public void execute(String value) throws Exception {
        value = StringUtils.unquotes(value);
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.TIME);
        } else {
            Date date = this.format.parse(value);
            long val = date.getTime();
            this.statement.setTimestamp(this.position, new java.sql.Timestamp(val));
        }
    }

}
