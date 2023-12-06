package icu.etl.log;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志表达式分析器
 *
 * @author jeremy8551@qq.com
 */
public class LogAnalysis {

    /** 转义字符 */
    protected char escape;

    /** true表示对字符串变量内容进行转义 */
    protected boolean escapeString;

    /** true表示忽略大小写 */
    protected boolean ignoreCase;

    /**
     * 初始化
     */
    public LogAnalysis() {
        this.ignoreCase = true;
        this.escapeString = true;
        this.escape = '\\';
    }

    public boolean existsEscape() {
        return this.escapeString;
    }

    public char getEscape() {
        return this.escape;
    }

    public void setEscape(char c) {
        this.escape = c;
    }

    public void removeEscape() {
        this.escapeString = false;
    }

    public boolean ignoreCase() {
        return this.ignoreCase;
    }

    public List<String> split(CharSequence script) {
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
            else if (Character.isWhitespace(c)) {
                list.add(script.subSequence(begin, i).toString());
                for (int j = i + 1; j < script.length(); j++) {
                    char nextChar = script.charAt(j);
                    if (Character.isWhitespace(nextChar)) {
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

    public String trim(CharSequence str, int left, int right, char... array) {
        if (str == null) {
            return null;
        }

        char c;
        int sp = 0, len = str.length(), ep = len - 1;

        if (left == 0) { // 删除左侧空白字符
            while (sp < len && Character.isWhitespace((c = str.charAt(sp)))) {
                sp++;
            }
        } else if (left == 1) { // 删除左侧空白字符与语句分隔符
            while (sp < len && (Character.isWhitespace((c = str.charAt(sp))))) {
                sp++;
            }
        } else if (left == 2) { // 删除左侧空白字符与语句分隔符与字符数组中的字符
            while (sp < len && (Character.isWhitespace((c = str.charAt(sp))) || this.inArray(c, array))) {
                sp++;
            }
        }

        if (right == 0) { // 删除右侧空白字符
            while (sp <= ep && ep >= 0 && Character.isWhitespace((c = str.charAt(ep)))) {
                ep--;
            }
        } else if (right == 1) { // 删除右侧空白字符与语句分隔符
            while (sp <= ep && ep >= 0 && (Character.isWhitespace((c = str.charAt(ep))))) {
                ep--;
            }
        } else if (right == 2) { // 删除右侧空白字符与语句分隔符与字符数组中的字符
            while (sp <= ep && ep >= 0 && (Character.isWhitespace((c = str.charAt(ep))) || this.inArray(c, array))) {
                ep--;
            }
        }

        return str.subSequence(sp, ep + 1).toString();
    }

    /**
     * 判断字符数组参数array中是否含有指定字符参数c
     *
     * @param c     字符
     * @param array 字符数组
     * @return
     */
    public static boolean inArray(char c, char... array) {
        if (array == null || array.length == 0) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == c) {
                return true;
            }
        }
        return false;
    }

}
