package icu.etl.database.mysql.expconv;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 时间格式
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-01
 */
public class TimestampConverter extends icu.etl.database.export.converter.TimestampConverter {

    public void execute() throws IOException, SQLException {
        Timestamp value = this.resultSet.getTimestamp(this.column);
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
