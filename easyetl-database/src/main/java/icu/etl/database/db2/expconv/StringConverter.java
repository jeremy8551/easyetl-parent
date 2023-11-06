package icu.etl.database.db2.expconv;

import java.io.IOException;
import java.sql.SQLException;

/**
 * DB2 数据库对字符类型字段的处理 <br>
 * 如果字段中存在双引号则替换为二个双引号 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-03-21
 */
public class StringConverter extends icu.etl.database.export.converter.StringConverter {

    @Override
    public void init() throws IOException, SQLException {
        super.init();
    }

    public void execute() throws IOException, SQLException {
        String str = this.resultSet.getString(this.column);
        if (str == null) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = this.process.execute(str);
        }
    }
}