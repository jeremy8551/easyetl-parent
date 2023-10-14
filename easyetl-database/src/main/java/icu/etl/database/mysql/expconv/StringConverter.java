package icu.etl.database.mysql.expconv;

import java.io.IOException;
import java.sql.SQLException;

/**
 * MYSQL 数据库对字符类型字段的处理 <br>
 * 如果字段中存在双引号则替换为二个双引号 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-03-21
 */
public class StringConverter extends icu.etl.database.export.converter.StringConverter {

    public void execute() throws IOException, SQLException {
        String value = this.resultSet.getString(this.column);
        if (value != null) {
            StringBuilder buf = new StringBuilder(value.length() + 2);
            buf.append('"');
            buf.append(this.process.execute(value));
            buf.append('"');
            this.array[this.column] = buf.toString();
        }
    }

}