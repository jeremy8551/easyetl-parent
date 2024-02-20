package icu.etl.database.load.converter;

import java.sql.Types;
import java.text.SimpleDateFormat;

import icu.etl.util.StringUtils;

/**
 * 把时间格式化为：java.sql.Time
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-11-08
 */
public class TimeConverter extends DateConverter {

    public void init() throws Exception {
        this.format = new SimpleDateFormat();
        this.format.applyPattern(StringUtils.defaultString((String) this.getAttribute(AbstractConverter.PARAM_TIMEFORMAT), "HH:mm:ss"));
    }

    public void execute(String value) throws Exception {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.TIME);
        } else {
            this.statement.setTime(this.position, new java.sql.Time(this.format.parse(value).getTime()));
        }
    }

}
