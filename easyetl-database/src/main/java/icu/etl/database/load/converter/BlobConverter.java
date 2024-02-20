package icu.etl.database.load.converter;

import java.io.ByteArrayInputStream;

import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 解析16进制字符串，转成二进制保存到数据库中
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-11-24
 */

public class BlobConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.notNull && this.isBlank(value)) {
            this.statement.setString(this.position, "");
        } else if (value.length() == 0) { // 空字符串表示空指针
            this.statement.setString(this.position, null);
        } else {
            byte[] array = StringUtils.parseHexString(value);
            ByteArrayInputStream in = new ByteArrayInputStream(array);
            try {
                this.statement.setBinaryStream(this.position, in, array.length);
            } finally {
                IO.closeQuietly(in);
            }
        }
    }

}
