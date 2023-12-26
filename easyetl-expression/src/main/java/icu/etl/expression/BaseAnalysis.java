package icu.etl.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class BaseAnalysis implements Analysis {

    /** 是否忽略字母大小写 */
    protected boolean ignoreCase;

    /** 是否使用转义字符 */
    protected boolean escape;

    /** 转义字符 */
    protected char escapeChar;

    /** 注释符号 */
    protected char comment;

    /** 段落的分隔符 */
    protected char segdel;

    /** 映射关系分隔符 */
    protected char mapdel;

    /**
     * 初始化
     */
    public BaseAnalysis() {
        super();
        this.ignoreCase = true;
        this.escape = true;
        this.escapeChar = '\\';
        this.comment = '#';
        this.segdel = ',';
        this.mapdel = ':';
    }

    public int[] indexOf(CharSequence str, String[] dest, int from) {
        if (str == null) {
            return null;
        }
        if (dest == null || dest.length <= 1 || from < 0) {
            throw new IllegalArgumentException(str + ", " + Arrays.toString(dest) + ", " + from);
        }

        int next = 0; // 第几个单词
        String word = dest[next]; // 单词

        // 第一个单词左面可以是空白或控制字符，但是右面必须是空白字符
        int index = this.indexOf(str, word, from, 1, 0);
        if (index == -1) {
            return null;
        }

        int begin = index; // 记录首次出现单词的位置
        from = index; // 设置搜索起始位置
        while (index != -1 && index == from) {
            if (++next >= dest.length) { // 已到最后一个单词
                return new int[]{begin, from};
            }

            from = StringUtils.indexOfNotBlank(str, from + word.length(), -1); // 查询下个搜索起始位置
            if (from == -1) { // 找不到下一个单词
                return null;
            } else {
                if (next < dest.length) {
                    word = dest[next];

                    if (next + 1 == dest.length) { // 最后一个单词的右面必须是空白字符或控制字符
                        index = this.indexOf(str, word, from, 0, 1);
                    } else {
                        index = this.indexOf(str, word, from, 0, 0);
                    }
                } else {
                    return new int[]{begin, from};
                }
            }
        }
        return null;
    }

    public int indexOf(CharSequence str, String dest, int from, int left, int right) {
        if (str == null) {
            return -1;
        }
        if (dest == null || dest.length() == 0 || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException(str + ", " + dest + ", " + from + ", " + left + ", " + right);
        }

        char fc = dest.charAt(0); // 搜索字符的第一个字符
        for (int i = from; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略单引号字符串
            if (c == '\'') {
                int end = this.indexOfQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号字符串
            if (c == '\"') {
                int end = this.indexOfDoubleQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 搜索字符串
            if ((this.ignoreCase ? (Character.toLowerCase(c) == Character.toLowerCase(fc)) : (c == fc)) && this.startsWith(str, dest, i, false) && this.charAt(str, i - 1, left) && this.charAt(str, i + dest.length(), right)) {
                return i;
            }
        }

        return -1;
    }

    public int lastIndexOf(CharSequence str, String dest, int from, int left, int right) {
        if (str == null) {
            return -1;
        }
        if (dest == null || dest.length() == 0 || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException(str + ", " + dest + ", " + from + ", " + left + ", " + right);
        }

        int begin = 0; // 搜索起始位置
        int end = from; // 搜索终止位置
        for (; (end - begin + 1) >= dest.length(); end--) {
            int index = end - dest.length() + 1;
            int next = end + 1;
            String substr = str.subSequence(index, next).toString();

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

    public boolean charAt(CharSequence str, int index, int mode) {
        // 0-表示只能是空白字符
        if (mode == 0) {
            return index < 0 || index >= str.length() || Character.isWhitespace(str.charAt(index));
        }

        // 1-表示只能是空白字符和控制字符
        if (mode == 1) {
            if (index >= 0 && index < str.length()) {
                char c = str.charAt(index);
                return Character.isWhitespace(c) || StringUtils.isSymbol(c);
            }
        }

        return true;
    }

    public int indexOfAccent(CharSequence str, int from) {
        for (int i = from + 1; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                int end = this.indexOfParenthes(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略字符串常量
            if (c == '\'') {
                int end = this.indexOfQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略字符串变量
            if (c == '\"') {
                int end = this.indexOfDoubleQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 命令替换符
            if (c == '`') {
                return i;
            }
        }
        return -1;
    }

    public int indexOfFloat(CharSequence str, int from) {
        int length = str.length();
        for (int i = from + 1; i < length; i++) {
            char c = str.charAt(i);

            if (c == 'e' || c == 'E') {
                int next = i + 1;
                if (next >= length) {
                    return length;
                }

                char nc = str.charAt(next);
                if ("0123456789".indexOf(nc) != -1) {
                    i = next;
                    continue;
                }

                if (nc == '+' || nc == '-') {
                    int last = next + 1;
                    if (last < length && "0123456789".indexOf(str.charAt(last)) != -1) {
                        i = last;
                        continue;
                    } else {
                        throw new ExpressionException(ResourcesUtils.getExpressionMessage(38, String.valueOf(str), next), next);
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

    public int indexOfSemicolon(CharSequence str, int from) {
        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                int end = StringUtils.indexOfQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '?') {
                count++;
                continue;
            }

            if (c == ':') {
                count--;
                if (count <= 0) {
                    return i;
                } else {
                    continue;
                }
            }
        }
        return -1;
    }

    public int indexOfParenthes(CharSequence str, int from) {
        if (from < 0 || from >= str.length()) {
            throw new IllegalArgumentException(str + ", " + from);
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                int end = StringUtils.indexOfQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '(') {
                count++;
                continue;
            }

            if (c == ')') {
                count--;
                if (count == 0) {
                    return i;
                } else {
                    continue;
                }
            }
        }
        return -1;
    }

    public int indexOfBracket(CharSequence str, int from) {
        if (str == null || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException(str + ", " + from);
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略字符常量中的空白
            if (c == '\'') {
                int end = this.indexOfQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 忽略双引号中的字符串常量
            if (c == '\"') {
                int end = this.indexOfDoubleQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '[') {
                count++;
                continue;
            }

            if (c == ']') {
                count--;
                if (count == 0) {
                    return i;
                } else {
                    continue;
                }
            }
        }
        return -1;
    }

    public int indexOfBrace(CharSequence str, int from) {
        if (from < 0 || from >= str.length()) {
            throw new IllegalArgumentException(str + ", " + from);
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                int end = StringUtils.indexOfQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            if (c == '{') {
                count++;
                continue;
            }

            if (c == '}') {
                count--;
                if (count == 0) {
                    return i;
                } else {
                    continue;
                }
            }
        }
        return -1;
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

    public int indexOfQuotation(CharSequence str, int from) {
        for (int i = from + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (this.escape && c == this.escapeChar) { // escape
                i++;
            } else if (c == '\'') {
                return i;
            }
        }
        return -1;
    }

    public int indexOfDoubleQuotation(CharSequence str, int from) {
        for (int i = from + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (this.escape && c == this.escapeChar) { // escape
                i++;
            } else if (c == '\"') {
                return i;
            }
        }
        return -1;
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
            if (c == '{') {
                int j = this.indexOfBrace(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // [ .. ]
            if (c == '[') {
                int j = this.indexOfBracket(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // 忽略字符常量中的空白
            if (c == '\'') {
                int j = this.indexOfQuotation(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            if (c == '\"') {
                int j = this.indexOfDoubleQuotation(str, i);
                if (j != -1) {
                    i = j;
                }
                continue;
            }

            // 找到空白字符
            if (Character.isWhitespace(c)) {
                return i;
            }
        }

        return -1;
    }

    public boolean startsWith(CharSequence str, CharSequence prefix, int from, boolean ignoreBlank) {
        if (str == null) {
            return false;
        }
        if (prefix == null || prefix.length() == 0 || from < 0) {
            throw new IllegalArgumentException(str + ", " + prefix + ", " + from + ", " + ignoreBlank);
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

    public List<String> split(CharSequence str, char... array) {
        List<String> list = new ArrayList<String>();
        if (str == null) {
            return list;
        }

        int begin = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略括号中的空白字符
            if (c == '(') {
                i = this.indexOfParenthes(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                } else {
                    continue;
                }
            }

            // { .. }
            if (c == '{') {
                i = this.indexOfBrace(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                } else {
                    continue;
                }
            }

            // [ .. ]
            if (c == '[') {
                i = this.indexOfBracket(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                } else {
                    continue;
                }
            }

            // 忽略字符常量中的空白
            if (c == '\'') {
                i = this.indexOfQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                } else {
                    continue;
                }
            }

            // 忽略双引号中的字符串常量
            if (c == '\"') {
                i = this.indexOfDoubleQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                } else {
                    continue;
                }
            }

            // 忽略空白字符和指定参数字符数组中的字符
            if (Character.isWhitespace(c) || StringUtils.inArray(c, array, this.ignoreCase)) {
                list.add(str.subSequence(begin, i).toString());
                for (int j = i + 1; j < str.length(); j++) {
                    char nextChar = str.charAt(j);
                    if (Character.isWhitespace(nextChar) || StringUtils.inArray(nextChar, array, this.ignoreCase)) {
                        i++;
                    } else {
                        break; // 表示字符串起始位置
                    }
                }
                begin = i + 1;
                continue;
            }
        }

        if (begin < str.length()) {
            list.add(str.subSequence(begin, str.length()).toString());
        } else if (begin == str.length()) {
            list.add("");
        }

        return list;
    }

    public String unQuotation(CharSequence str) {
        if (str == null) {
            return null;
        }

        // 如果字符串两端有单引号或双引号
        if (this.containsQuotation(str)) {
            int sp = 0, len = str.length(), ep = len - 1;
            while (sp < len && Character.isWhitespace(str.charAt(sp))) {
                sp++;
            }
            while (sp <= ep && Character.isWhitespace(str.charAt(ep))) {
                ep--;
            }
            return str.subSequence(sp + 1, ep).toString();
        }

        return str.toString();
    }

    public boolean containsQuotation(CharSequence str) {
        if (str == null || str.length() <= 1) {
            return false;
        }

        char first = ' ', last = ' ';
        int sp = 0, len = str.length(), ep = len - 1;
        while (sp < len && (Character.isWhitespace((first = str.charAt(sp))))) {
            sp++;
        }

        if (first != '\'' && first != '"') { // 第一个字符不是双引号
            return false;
        }

        while (sp <= ep && Character.isWhitespace(last = str.charAt(ep))) {
            ep--;
        }

        if (last != '\'' && last != '"') { // 最后一个字符不是双引号
            return false;
        } else if (first == '\'') {
            return this.indexOfQuotation(str, sp) == ep;
        } else if (first == '"') {
            return this.indexOfDoubleQuotation(str, sp) == ep;
        } else {
            return false;
        }
    }

    public boolean existsEscape() {
        return this.escape;
    }

    public char getEscape() {
        return this.escapeChar;
    }

    public void setEscape(char c) {
        this.escapeChar = c;
    }

    public void removeEscape() {
        this.escape = false;
    }

    public char getComment() {
        return this.comment;
    }

    public String unescapeString(CharSequence str) {
        return this.escape ? StringUtils.unescape(str) : str.toString();
    }

    public boolean ignoreCase() {
        return this.ignoreCase;
    }

    public char getSegment() {
        return this.segdel;
    }

    public char getMapdel() {
        return this.mapdel;
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

}
