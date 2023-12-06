package icu.etl.expression;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class StandardAnalysis implements Analysis {

    /** true表示忽略字母大小写 */
    protected boolean ignoreCase;

    /** true表示使用转义字符 */
    protected boolean escape;

    /** 转义字符 */
    protected char escapeChar;

    /** true表示对字符串变量内容进行转义 */
    protected boolean escapeString;

    /**
     * 初始化
     */
    public StandardAnalysis() {
        super();
        this.escapeString = true;
        this.escapeChar = '\\';
        this.escape = true;
        this.ignoreCase = true;
    }

    public int indexOf(CharSequence str, String dest, int from, int left, int right) {
        if (str == null) {
            return -1;
        }

        Ensure.notNull(dest);
        Ensure.isFromOne(dest.length());
        Ensure.isFromZero(from);

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
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(str, i);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            }

            // 搜索字符串
            else if ((this.ignoreCase ? (Character.toLowerCase(c) == Character.toLowerCase(fc)) : (c == fc)) && this.startsWith(str, dest, i, false) && this.charAt(str, i - 1, left) && this.charAt(str, i + dest.length(), right)) {
                return i;
            }
        }

        return -1;
    }

    public boolean charAt(CharSequence str, int index, int mode) {
        if (mode == 0) { // 0-表示只能是空白字符
            return index < 0 || index >= str.length() || Character.isWhitespace(str.charAt(index));
        } else if (mode == 1) { // 1-表示只能是空白字符和控制字符
            if (index < 0 || index >= str.length()) {
                return true;
            } else {
                char c = str.charAt(index);
                return Character.isWhitespace(c) || StringUtils.isSymbol(c);
            }
        } else {
            return true;
        }
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
            } else if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
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
            } else if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
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

    public int indexOfBracket(CharSequence str, int from) {
        if (from < 0 || from >= str.length()) {
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
            else if (c == '\"') {
                int end = this.indexOfDoubleQuotation(str, i);
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
            } else if (c == '\"') {
                int end = StringUtils.indexOfDoubleQuotation(str, i, this.escape);
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
            if (this.escapeString && c == this.escapeChar) { // escape
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
            if (this.escapeString && c == this.escapeChar) { // escape
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

            // 找到空白字符
            else if (Character.isWhitespace(c)) {
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
                }
                continue;
            }

            // { .. }
            else if (c == '{') {
                i = this.indexOfBrace(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // [ .. ]
            else if (c == '[') {
                i = this.indexOfBracket(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略字符常量中的空白
            else if (c == '\'') {
                i = this.indexOfQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            else if (c == '\"') {
                i = this.indexOfDoubleQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略空白字符和指定参数字符数组中的字符
            else if (Character.isWhitespace(c) || StringUtils.inArray(c, array, this.ignoreCase)) {
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

    public char getComment() {
        return '#';
    }

    public String unescapeString(CharSequence str) {
        return this.escapeString ? StringUtils.unescape(str) : str.toString();
    }

    public boolean ignoreCase() {
        return this.ignoreCase;
    }

    public char getSegment() {
        return ',';
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

    public char getMapdel() {
        return ':';
    }

}
