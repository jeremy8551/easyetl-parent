package icu.etl.database.db2.expconv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.SQLException;

import icu.etl.collection.CharBuffer;
import icu.etl.util.StringUtils;

/**
 * DB2 数据库对字符类型字段的处理 <br>
 * 如果字段中存在双引号则替换为二个双引号 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-03-21
 */
public class StringConverter extends icu.etl.database.export.converter.StringConverter {

    public void init() throws IOException, SQLException {
        super.init();
        String charsetName = (String) this.getAttribute(PARAM_CHARSET);
        this.process = StringUtils.isNotBlank(charsetName) && this.contains(PARAM_MESSY) ? new Messy(charsetName) : new Nomal();
    }

    public void execute() throws IOException, SQLException {
        String str = this.resultSet.getString(this.column);
        if (str == null) {
            this.array[this.column] = "";
        } else {
            this.array[this.column] = this.process.execute(str);
        }
    }

    /**
     * 处理乱码
     *
     * @author jeremy8551@qq.com
     */
    static class Messy implements Process {

        private CharsetEncoder encoder;

        private CharBuffer buffer;

        public Messy(String charsetName) {
            this.buffer = new CharBuffer(100, 30);
            Charset charset = java.nio.charset.Charset.forName(charsetName);
            this.encoder = charset.newEncoder();
        }

        public String execute(String str) {
            this.buffer.setLength(0);
            this.buffer.append('\"');
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (this.encoder.canEncode(c)) { // 检查字符是否正确
                    if (c == '\"' || c == ',') { // 对字符串中的半角逗号与双引号进行转义
                        this.buffer.append('\"');
                    }
                    this.buffer.append(c);
                }
            }
            this.buffer.append('\"');
            return this.buffer.toString();
        }
    }

    /**
     * 乱码处理接口
     *
     * @author jeremy8551@qq.com
     */
    static class Nomal implements Process {

        private CharBuffer buffer;

        public Nomal() {
            this.buffer = new CharBuffer(100, 30);
        }

        public String execute(String str) {
            this.buffer.setLength(0);
            this.buffer.expandCapacity(str.length() + 2);
            this.buffer.append('\"');
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '\"' || c == ',') { // 对字符串中的半角逗号与双引号进行转义
                    this.buffer.append('\"');
                }
                this.buffer.append(c);
            }
            this.buffer.append('\"');
            return this.buffer.toString();
        }
    }

}