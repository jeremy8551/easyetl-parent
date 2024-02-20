package icu.etl.database.mysql.expconv;

import java.sql.Time;

/**
 * 时间格式
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-01
 */
public class TimeConverter extends icu.etl.database.export.converter.TimeConverter {

    public void execute() throws Exception {
        Time value = this.resultSet.getTime(this.column);
        if (value == null) {
            this.array[this.column] = "";
        } else {
            String str = this.format.format(value);
            StringBuilder buf = new StringBuilder(str.length() + 2);
            buf.append('"');
            buf.append(str);
            buf.append('"');
            this.array[this.column] = buf.toString();
        }
    }

}
