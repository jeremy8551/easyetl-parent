package icu.etl.database.db2.recconv;

import icu.etl.database.load.converter.AbstractConverter;

/**
 * DB2 数据库字符串型字段值左右会存在双引号
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-15
 */
public class StringConverter extends AbstractConverter {

    /** true表示保留字符串右端的空白字符 */
    protected boolean keepblanks;

    public void init() throws Exception {
        this.keepblanks = this.contains("keepblanks");
    }

    public void execute(String value) throws Exception {
        int end = value.length() - 1;
        if (end >= 1 && value.charAt(0) == '"' && value.charAt(end) == '"') {
            value = value.substring(1, end);
        }
        this.statement.setString(this.position, this.keepblanks ? value : this.rtrim(value, end));
    }

    /**
     * 从字符串参数 str 右端向左端删除空白字符
     *
     * @param str   字符串
     * @param start 删除空白字符的起始位置
     * @return
     */
    public String rtrim(String str, int start) {
        while (start >= 0) {
            char c = str.charAt(start);
            if (Character.isWhitespace(c)) {
                start--;
            } else {
                break;
            }
        }
        return ++start == str.length() ? str : str.substring(0, start);
    }

}
