package icu.etl.expression;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 语句分析器的实现类
 *
 * @author jeremy8551@qq.com
 */
public class XmlAnalysis implements Analysis {

    /** 语句分隔符 */
    protected char token = ';';

    /** 段落的分隔符 */
    protected char segdel = ',';

    /** 映射关系分隔符 */
    protected char mapdel = ':';

    /** 注释符号 */
    protected char comment = '#';

    /** 转义字符 */
    protected char escape = '\\';

    /** true表示对字符串变量内容进行转义 */
    protected boolean escapeString = true;

    /** true表示忽略大小写 */
    protected boolean ignoreCase = true;

    /**
     * 初始化
     */
    public XmlAnalysis() {
    }

    public char getComment() {
        return this.comment;
    }

    public char getSegment() {
        return this.segdel;
    }

    public boolean ignoreCase() {
        return this.ignoreCase;
    }

    public boolean equals(String str1, String str2) {
        boolean b1 = str1 == null;
        boolean b2 = str2 == null;
        if (b1 && b2) {
            return true;
        } else if (b1 || b2) {
            return false;
        } else {
            return this.ignoreCase ? str1.equalsIgnoreCase(str2) : str1.equals(str2);
        }
    }

    public boolean exists(String key, String... array) {
        if (key == null) {
            for (String str : array) {
                if (str == null) {
                    return true;
                }
            }
            return false;
        } else if (this.ignoreCase) {
            return StringUtils.inArrayIgnoreCase(key, array);
        } else {
            return StringUtils.inArray(key, array);
        }
    }

    public List<String> split(CharSequence script, char... array) {
        List<String> list = new ArrayList<String>();
        if (script == null) {
            return list;
        }

        int begin = 0;
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                i = this.indexOfParenthes(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略字符常量中的空白
            else if (c == '\'') {
                i = this.indexOfQuotation(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                i = this.indexOfDoubleQuotation(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略命令替换
            else if (c == '`') {
                i = this.indexOfAccent(script, i);
                if (i == -1) {
                    list.add(script.subSequence(begin, script.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略空白字符和指定参数字符数组中的字符
            else if (Character.isWhitespace(c) || StringUtils.inArray(c, array)) {
                list.add(script.subSequence(begin, i).toString());
                for (int j = i + 1; j < script.length(); j++) {
                    char nextChar = script.charAt(j);
                    if (Character.isWhitespace(nextChar) || StringUtils.inArray(nextChar, array)) {
                        i++;
                    } else {
                        break; // 表示字符串起始位置
                    }
                }
                begin = i + 1;
                continue;
            }
        }

        if (begin < script.length()) {
            list.add(script.subSequence(begin, script.length()).toString());
        } else if (begin == script.length()) {
            list.add("");
        }

        return list;
    }

    public int indexOf(CharSequence script, String str, int from, int left, int right) {
        if (script == null) {
            return -1;
        }

        if (str == null || str.length() == 0 || from < 0) {
            throw new IllegalArgumentException("indexOf(" + script + ", " + str + ", " + from + ", " + left + ")");
        }

        char fc = str.charAt(0); // 搜索字符的第一个字符
        for (int i = from; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略单引号字符串
            if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号字符串
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略反引号中的内容
            else if (c == '`') {
                int end = this.indexOfAccent(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 搜索字符串
            else if ((this.ignoreCase ? (Character.toLowerCase(c) == Character.toLowerCase(fc)) : (c == fc)) && this.startsWith(script, str, i, false) && this.charAt(script, i - 1, left) && this.charAt(script, i + str.length(), right)) {
                return i;
            }
        }

        return -1;
    }

    public boolean charAt(CharSequence script, int index, int mode) {
        if (mode == 0) { // 0-表示只能是空白字符
            return index < 0 || index >= script.length() || Character.isWhitespace(script.charAt(index));
        } else if (mode == 1) { // 1-表示只能是空白字符和控制字符
            if (index < 0 || index >= script.length()) {
                return true;
            } else {
                char c = script.charAt(index);
                return Character.isWhitespace(c) || StringUtils.isSymbol(c);
            }
        } else {
            return true;
        }
    }

    public int[] indexOf(CharSequence script, String[] array, int from) {
        if (script == null) {
            return null;
        }
        if (array == null || array.length <= 1 || from < 0) {
            throw new IllegalArgumentException("indexOf(" + script + ", " + StringUtils.toString(array) + ", " + from + ")");
        }

        int next = 0; // 第几个单词
        String str = array[next]; // 单词

        /**
         * 第一个单词左面可以是空白或控制字符，但是右面必须是空白字符
         */
        int index = this.indexOf(script, str, from, 1, 0);
        if (index == -1) {
            return null;
        }

        int begin = index; // 记录首次出现单词的位置
        from = index; // 设置搜索起始位置
        while (index != -1 && index == from) {
            if (++next >= array.length) { // 已到最后一个单词
                return new int[]{begin, from};
            }

            from = StringUtils.indexOfNotBlank(script, from + str.length(), -1); // 查询下个搜索起始位置
            if (from == -1) { // 找不到下一个单词
                return null;
            } else {
                if (next < array.length) {
                    str = array[next];

                    if (next + 1 == array.length) { // 最后一个单词的右面必须是空白字符或控制字符
                        index = this.indexOf(script, str, from, 0, 1);
                    } else {
                        index = this.indexOf(script, str, from, 0, 0);
                    }
                } else {
                    return new int[]{begin, from};
                }
            }
        }
        return null;
    }

    /**
     * 从字符串参数 str 中指定位置 from 开始到 end 位置为止开始搜索字符串参数 dest，返回字符串参数 dest 在字符串参数 str 中最后一次出现所在的位置
     *
     * @param str   字符串
     * @param dest  搜索字符串
     * @param from  搜索起始位置(包含该点)
     * @param left  0-表示左侧字符必须是空白字符（含-1和最右侧） 1-表示左侧字符必须是空白字符与控制字符 2-表示任意字符
     * @param right 0-表示右侧字符必须是空白字符（含-1和最右侧） 1-表示右侧字符必须是空白字符与控制字符 2-表示任意字符
     * @return -1表示字符串 dest 没有出现
     */
    public int lastIndexOf(String str, String dest, int from, int left, int right) {
        if (str == null) {
            return -1;
        }
        if (dest == null || dest.length() == 0 || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException("lastIndexOf(\"" + str + "\", \"" + dest + "\", " + from + ", " + left + ", " + right + ")");
        }

        int begin = 0; // 搜索起始位置
        int end = from; // 搜索终止位置
        for (; (end - begin + 1) >= dest.length(); end--) {
            int index = end - dest.length() + 1;
            int next = end + 1;
            String substr = str.substring(index, next);

            if (this.ignoreCase) {
                if (substr.equalsIgnoreCase(dest) && (this.charAt(str, index - 1, left) && this.charAt(str, next, right))) {
                    return index;
                }
            } else {
                if (substr.equals(dest) && (this.charAt(str, index - 1, left) && this.charAt(str, next, right))) {
                    return index;
                }
            }
        }
        return -1;
    }

    public int indexOfParenthes(CharSequence script, int from) {
        if (script == null || from < 0 || from >= script.length()) {
            throw new IllegalArgumentException("indexOfParenthes(" + script + ", " + from + ")");
        }

        for (int i = from + 1, count = 1; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略字符常量中的空白
            if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfBracket(CharSequence script, int from) {
        if (script == null || from < 0 || from >= script.length()) {
            throw new IllegalArgumentException("indexOfBracket(" + script + ", " + from + ")");
        }

        for (int i = from + 1, count = 1; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略字符常量中的空白
            if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '[') {
                count++;
            } else if (c == ']') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfBrace(CharSequence script, int from) {
        if (script == null) {
            return -1;
        }
        if (from < 0) {
            throw new IllegalArgumentException("indexOfBrace(" + script + ", " + from + ")");
        }

        for (int i = from + 1, count = 1; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略单引号字符串
            if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号字符串
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfQuotation(CharSequence script, int from) {
        for (int i = from + 1; i < script.length(); i++) {
            char c = script.charAt(i);
            if (this.escapeString && c == this.escape) { // escape
                i++;
            } else if (c == '\'') {
                return i;
            }
        }
        return -1;
    }

    public int indexOfDoubleQuotation(CharSequence script, int from) {
        for (int i = from + 1; i < script.length(); i++) {
            char c = script.charAt(i);
            if (this.escapeString && c == this.escape) { // escape
                i++;
            } else if (c == '\"') {
                return i;
            }
        }
        return -1;
    }

    public int indexOfAccent(CharSequence script, int from) {
        for (int i = from + 1; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                int end = this.indexOfParenthes(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略字符串常量
            else if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略字符串变量
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 命令替换符
            else if (c == '`') {
                return i;
            }
        }
        return -1;
    }

    public int indexOfSemicolon(CharSequence script, int from) {
        int count = 1;
        for (int i = from + 1; i < script.length(); i++) {
            char c = script.charAt(i);

            // 忽略字符串常量
            if (c == '\'') {
                int end = this.indexOfQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略字符串变量
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(script, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略括号中的空白字符与逗号
            else if (c == '(') {
                int end = this.indexOfParenthes(script, i);
                if (end == -1) {
                    throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(112, script));
                }
                i = end;
                continue;
            }

            // 忽略命令替换中的管道符
            else if (c == '`') {
                int end = this.indexOfAccent(script, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            } else if (c == '?') {
                count++;
                continue;
            } else if (c == ':') {
                count--;
                if (count <= 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfFloat(CharSequence script, int from) {
        int length = script.length();
        for (int i = from + 1; i < length; i++) {
            char c = script.charAt(i);

            if (c == 'e' || c == 'E') {
                int next = i + 1;
                if (next >= length) {
                    return length;
                }

                char nc = script.charAt(next);
                if ("0123456789".indexOf(nc) != -1) {
                    i = next;
                    continue;
                }

                if (nc == '+' || nc == '-') {
                    int last = next + 1;
                    if (last < length && "0123456789".indexOf(script.charAt(last)) != -1) {
                        i = last;
                        continue;
                    } else {
                        throw new ExpressionException(ResourcesUtils.getExpressionMessage(38, String.valueOf(script), next), next);
                    }
                }

                continue;
            }

            if ("0123456789".indexOf(c) == -1 && c != '.') {
                return i;
            }
        }
        return length;
    }

    public int indexOfHex(CharSequence str, int from) {
        int length = str.length();
        if (from >= length) {
            return -1;
        }

        for (int i = from; i < length; i++) {
            if ("0123456789abcdefABCDEF".indexOf(str.charAt(i)) == -1) {
                return i;
            }
        }
        return length;
    }

    public int indexOfOctal(CharSequence str, int from) {
        int length = str.length();
        if (from >= length) {
            return -1;
        }

        for (int i = from; i < length; i++) {
            if ("01234567".indexOf(str.charAt(i)) == -1) {
                return i;
            }
        }
        return length;
    }

    public int indexOfWhitespace(CharSequence str, int from) {
        for (int i = from; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                int j = this.indexOfParenthes(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // { .. }
            else if (c == '{') {
                int j = this.indexOfBrace(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // [ .. ]
            else if (c == '[') {
                int j = this.indexOfBracket(str, i);
                if (i != -1) {
                    i = j;
                }
                continue;
            }

            // 忽略字符常量中的空白
            else if (c == '\'') {
                int j = this.indexOfQuotation(str, i);
                if (i != -1) {
                    i = j;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                int j = this.indexOfDoubleQuotation(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // 忽略反引号中的内容
            else if (c == '`') {
                int j = this.indexOfAccent(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // 找到空白字符
            else if (Character.isWhitespace(c)) {
                return i;
            }
        }

        return -1;
    }

    public String unescapeString(CharSequence str) {
        return this.escapeString ? StringUtils.unescape(str) : str.toString();
    }

    public String unQuotation(CharSequence str) {
        if (str == null) {
            return null;
        }

        if (this.containsQuotation(str)) {
            int sp = 0, len = str.length(), ep = len - 1;
            while (sp < len && Character.isWhitespace(str.charAt(sp))) {
                sp++;
            }
            while (sp <= ep && ep >= 0 && Character.isWhitespace(str.charAt(ep))) {
                ep--;
            }
            return str.subSequence(sp + 1, ep).toString();
        } else {
            return str.toString();
        }
    }

    public boolean containsQuotation(CharSequence str) {
        if (str == null || str.length() <= 1) {
            return false;
        }

        char first = ' ', last = ' ';
        int left = 0, len = str.length(), right = len - 1;
        while (left < len && (Character.isWhitespace((first = str.charAt(left))))) {
            left++;
        }

        if (first != '\'' && first != '"') { // 第一个字符不是双引号
            return false;
        }

        while (left <= right && right >= 0 && (Character.isWhitespace((last = str.charAt(right))))) {
            right--;
        }

        if (last != '\'' && last != '"') { // 最后一个字符不是双引号
            return false;
        } else if (first == '\'') {
            return this.indexOfQuotation(str, left) == right;
        } else if (first == '"') {
            return this.indexOfDoubleQuotation(str, left) == right;
        } else {
            return false;
        }
    }

    public boolean startsWith(CharSequence str, CharSequence prefix, int from, boolean ignoreBlank) {
        if (str == null) {
            return false;
        }
        if (prefix == null || prefix.length() == 0 || from < 0) {
            throw new IllegalArgumentException("startsWith(\"" + str + "\", \"" + prefix + "\", " + from + ", " + ignoreBlank + ")");
        }

        for (int i = from; i < str.length(); i++) {
            char c = str.charAt(i);

            if (ignoreBlank && Character.isWhitespace(c)) {
                continue;
            }

            int len = str.length() - i;
            if (len < prefix.length()) {
                return false;
            }

            for (int j = 0; j < prefix.length() && i < str.length(); j++, i++) {
                if (this.ignoreCase) {
                    if (Character.toLowerCase(prefix.charAt(j)) != Character.toLowerCase(str.charAt(i))) {
                        return false;
                    }
                } else {
                    if (prefix.charAt(j) != str.charAt(i)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    public char getMapdel() {
        return this.mapdel;
    }

}
