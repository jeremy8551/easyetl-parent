package icu.etl.database.load.converter;

public class StringConverter extends AbstractConverter {

    /** true表示保留字符串右端的空白字符 */
    protected boolean keepblanks;

    public void init() throws Exception {
        this.keepblanks = this.contains("keepblanks");
    }

    public void execute(String value) throws Exception {
        if (this.notNull && value.length() == 0) {
            this.statement.setString(this.position, "");
        } else {
            this.statement.setString(this.position, this.keepblanks ? value : this.rtrim(value));
        }
    }

    /**
     * 将参数obj转为字符串并删除字符串右端的空白字符 <br>
     * rtrimBlank(" 1234 ") == " 1234" <br>
     * rtrimBlank(" ") == "" <br>
     * rtrimBlank(null) == null <br>
     *
     * @param str 字符串
     * @return 字符串
     */
    public String rtrim(String str) {
        int index = str.length() - 1;
        while (index >= 0) {
            char c = str.charAt(index);
            if (Character.isWhitespace(c)) {
                index--;
            } else {
                break;
            }
        }
        return str.substring(0, ++index);
    }

}
