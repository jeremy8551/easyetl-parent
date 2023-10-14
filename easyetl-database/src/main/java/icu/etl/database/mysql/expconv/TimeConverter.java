package icu.etl.database.mysql.expconv;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;

/**
 * 时间格式
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-01
 */
public class TimeConverter extends icu.etl.database.export.converter.TimeConverter {

    public void execute() throws IOException, SQLException {
        Time value = this.resultSet.getTime(this.column);
        if (value != null) {
            String str = this.format.format(value);
            StringBuilder buf = new StringBuilder(str.length() + 2);
            buf.append('"');
            buf.append(str);
            buf.append('"');
            this.array[this.column] = buf.toString();
        }
    }

}
