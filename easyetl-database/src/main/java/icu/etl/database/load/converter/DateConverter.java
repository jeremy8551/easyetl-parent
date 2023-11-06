package icu.etl.database.load.converter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import icu.etl.util.StringUtils;

/**
 * 把字符串格式化为java.sql.Date <br>
 * 默认的日期格式为yyyy-MM-dd
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-11-08
 */
public class DateConverter extends AbstractConverter {

    /** 日期时间转换器 */
    protected SimpleDateFormat format;

    public void init() throws IOException, SQLException, ParseException {
        this.format = new SimpleDateFormat();
        this.format.applyPattern(StringUtils.defaultString((String) this.getAttribute(PARAM_DATEFORMAT), "yyyy-MM-dd"));
    }

    public void execute(String value) throws IOException, SQLException, ParseException {
        if (this.isBlank(value)) {
            this.statement.setNull(this.position, Types.DATE);
        } else {
            this.statement.setDate(this.position, new java.sql.Date(this.format.parse(value).getTime()));
        }
    }

}
