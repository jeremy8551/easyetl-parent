package icu.etl.script.compiler;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.etl.expression.ExpressionException;
import icu.etl.expression.WordIterator;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptFormatter;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.io.ScriptStdbuf;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 语句分析器的实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptAnalysis implements UniversalScriptAnalysis {

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
    public ScriptAnalysis() {
    }

    public char getComment() {
        return this.comment;
    }

    public char getSegment() {
        return this.segdel;
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

    public char getToken() {
        return this.token;
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

    public String getPrefix(String script) {
        int length = script.length();
        StringBuilder cb = new StringBuilder(length);
        int index = script.charAt(0) == '!' ? 1 : 0; // 忽略取反符号 !
        for (; index < length; index++) {
            char c = script.charAt(index);

            if (Character.isWhitespace(c) // 空白字符
                    || c == this.token // 语句分隔符
                    || c == this.comment // 注释符
                    || c == this.segdel // 段落分隔符
                    || c == '|' // 管道符
                    || c == '`' // 命令替换符
                    || c == '[' // 变量方法分隔符
                    || (c == '.' && index != 0) // 变量方法分隔符， 第一个字符不能是 .
            ) {
                break;
            } else {
                cb.append(c);
            }
        }

        return cb.toString(); // 命令前缀 或 自定义方法名 或 变量名
    }

    public int indexOfVariableName(String str, int from) {
        if (str == null) {
            return -1;
        }

        // 在脚本语句中搜索变量名结束位置 <br>
        // 变量名中只能有英文字母, 数字, 下划线, 首字母不能是数字
        if (StringUtils.isNumber(str.charAt(from))) {
            throw new IllegalArgumentException(ResourcesUtils.getExpressionMessage(60, str, from + 1));
        }

        for (int i = from + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!StringUtils.isLetter(c) && !StringUtils.isNumber(c) && c != '_') {
                return i;
            }
        }
        return str.length();
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

    public int indexOf(CharSequence script, char[] array, int from) {
        if (script == null) {
            return -1;
        }
        if (array == null || array.length == 0 || from < 0) {
            throw new IllegalArgumentException("indexOf(\"" + script + "\", " + StringUtils.toString(array) + ", " + from + ")");
        }

        for (int i = from; i < script.length(); i++) {
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
            } else {
                for (char ac : array) {
                    if (c == ac) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

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

    public int indexOfVariableMethod(String str, int from) {
        if (str == null) {
            return -1;
        }
        if (from < 0) {
            throw new IllegalArgumentException("indexOfVariableMethod(" + str + ", " + from + ")");
        }

        for (int i = from + 1; i < str.length(); i++) {
            char c = str.charAt(i);

            // 忽略括号中的空白字符与逗号
            if (c == '(') {
                int end = this.indexOfParenthes(str, i);
                if (end == -1) {
                    throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(112, str));
                }
                i = end;
                continue;
            }

            // 忽略中括号中的字符串信息
            else if (c == '[') {
                int end = this.indexOfBracket(str, i);
                if (end == -1) {
                    throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(115, str));
                }
                i = end;
                continue;
            } else if (!StringUtils.isLetter(c) && !StringUtils.isNumber(c) && c != '_' && c != '.') {
                return i;
            }
        }
        return str.length();
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

    public int indexOfInteger(CharSequence script, int from) {
        if (script == null || from < 0 || from >= script.length()) {
            return -1;
        }

        char c = script.charAt(from);
        if (c == '0') {
            return from + 1;
        }

        for (int i = from + 1; i < script.length(); i++) {
            if (!StringUtils.isNumber(script.charAt(i))) {
                return i;
            }
        }
        return script.length();
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

    public String unescapeSQL(String str) {
        return StringUtils.replaceAll(str, "&ads;", "$");
    }

    public String removeSide(CharSequence str, char left, char right) {
        if (str == null) {
            return null;
        } else if (this.containsSide(str, left, right)) {
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

    public boolean containsSide(CharSequence str, char lc, char rc) {
        if (Character.isWhitespace(lc) || Character.isWhitespace(rc)) {
            throw new IllegalArgumentException();
        }
        if (str == null || str.length() <= 1) {
            return false;
        }

        char first = ' ', last = ' ';
        int left = 0, len = str.length(), right = len - 1;
        while (left < len && (Character.isWhitespace((first = str.charAt(left))))) {
            left++;
        }

        if (first != lc) { // 第一个非空白字符不是字符参数lc
            return false;
        }

        while (left <= right && right >= 0 && (Character.isWhitespace((last = str.charAt(right))))) {
            right--;
        }

        if (last != rc) { // 最后一个字符不是双引号
            return false;
        } else if (first == '\'') { // 引号需要成对出现
            return this.indexOfQuotation(str, left) == right;
        } else if (first == '"') { // 引号需要成对出现
            return this.indexOfDoubleQuotation(str, left) == right;
        } else {
            return true;
        }
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
            while (sp < len && (Character.isWhitespace((c = str.charAt(sp))) || c == this.token)) {
                sp++;
            }
        } else if (left == 2) { // 删除左侧空白字符与语句分隔符与字符数组中的字符
            while (sp < len && (Character.isWhitespace((c = str.charAt(sp))) || c == this.token || StringUtils.inArray(c, array))) {
                sp++;
            }
        }

        if (right == 0) { // 删除右侧空白字符
            while (sp <= ep && ep >= 0 && Character.isWhitespace((c = str.charAt(ep)))) {
                ep--;
            }
        } else if (right == 1) { // 删除右侧空白字符与语句分隔符
            while (sp <= ep && ep >= 0 && (Character.isWhitespace((c = str.charAt(ep))) || c == this.token)) {
                ep--;
            }
        } else if (right == 2) { // 删除右侧空白字符与语句分隔符与字符数组中的字符
            while (sp <= ep && ep >= 0 && (Character.isWhitespace((c = str.charAt(ep))) || c == this.token || StringUtils.inArray(c, array))) {
                ep--;
            }
        }

        return str.subSequence(sp, ep + 1).toString();
    }

    public boolean isBlankline(CharSequence str) {
        if (str != null && str.length() > 0) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                if (!Character.isWhitespace(c) && c != this.token && c != this.comment) {
                    return false;
                }
            }
        }
        return true;
    }

    public char getMapdel() {
        return this.mapdel;
    }

    public WordIterator parse(String script) {
        return new WordIterator(this, script);
    }

    /**
     * 对字符串参数 str 执行命令替换和变量替换 <br>
     * 命令替换: 替换 `` 中的命令 <br>
     * 变量替换: 替换（会替换单引号中的变量占位符） $name ${name} $? $# $0 $1 格式的变量占位符 <br>
     *
     * @param session 用户会话信息
     * @param str     字符串
     * @param escape  true表示对字符串参数 str 进行转义（转义规则详见 {@link #unescapeSQL(String)}}）, false表示不执行转义
     * @return
     */
    public String replaceVariable(UniversalScriptSession session, UniversalScriptContext context, String str, boolean escape) {
        if (str == null) {
            return str;
        }

        UniversalScriptStdout stdout = context.getStdout();
        UniversalScriptStderr stderr = context.getStderr();
        UniversalScriptVariable localVariable = context.getLocalVariable();
        UniversalScriptVariable globalVariable = context.getGlobalVariable();
        UniversalScriptFormatter format = context.getFormatter();
        Map<String, Object> variables = session.getVariables();

        str = this.replaceSubCommand(session, context, stdout, stderr, str, false);
        str = this.replaceShellSpecialVariable(session, str, false);
        str = this.replaceShellVariable(str, localVariable, format, false, true);
        str = this.replaceShellVariable(str, globalVariable, format, false, true);
        str = this.replaceShellVariable(str, variables, format, false, true);
        return escape ? this.unescapeSQL(str) : str;
    }

    /**
     * 对字符串参数 str 执行命令替换和变量替换 <br>
     * 命令替换: 替换 `` 中的命令 <br>
     * 变量替换: 替换（会保留单引号中的变量占位符） $name ${name} $? $# $0 $1 格式的变量占位符 <br>
     *
     * @param session      用户会话信息
     * @param str          字符串
     * @param removeQuote  true表示删除字符串参数 str 二端的单引号或双引号, false表示不作处理
     * @param keepVariable true表示保留变量值是null的变量占位符 false表示删除变量值是null的变量占位符
     * @param evalInnerCmd true表示执行命令替换 false表示不执行命令替换
     * @param escape       true表示对字符串参数 str 进行转义（转义规则详见 {@link #unescapeSQL(String)}}）, false表示不执行转义
     * @return
     */
    public String replaceShellVariable(UniversalScriptSession session, UniversalScriptContext context, String str, boolean removeQuote, boolean keepVariable, boolean evalInnerCmd, boolean escape) {
        if (str == null) {
            return str;
        }

        UniversalScriptStdout stdout = context.getStdout();
        UniversalScriptStderr stderr = context.getStderr();
        UniversalScriptVariable localVariable = context.getLocalVariable();
        UniversalScriptVariable globalVariable = context.getGlobalVariable();
        UniversalScriptFormatter format = context.getFormatter();
        Map<String, Object> variables = session.getVariables();

        if (evalInnerCmd) {
            str = this.replaceSubCommand(session, context, stdout, stderr, str, true);
        }

        str = this.replaceShellSpecialVariable(session, str, true);
        str = this.replaceShellVariable(str, localVariable, format, true, true);
        str = this.replaceShellVariable(str, globalVariable, format, true, true);
        str = this.replaceShellVariable(str, variables, format, true, keepVariable);
        if (escape) { // 一定要在替换完字符串中变量之后再执行 {@link #unescapeSQL(String)} 方法
            str = this.unescapeSQL(str);
        }
        return removeQuote ? this.unQuotation(str) : str;
    }

    /**
     * 执行命令替换 <br>
     * 脚本引擎执行字符串str中 `` 中的内容，并将运行输出的标准输出信息替换到字符串str中
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param script      脚本语句
     * @param ignoreQuote true表示不会替换单引号中的命令
     * @return
     */
    private String replaceSubCommand(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String script, boolean ignoreQuote) {
        if (script == null) {
            return null;
        }

        for (int index = 0; index < script.length(); index++) {
            char c = script.charAt(index);
            if (ignoreQuote && c == '\'') {
                int end = this.indexOfQuotation(script, index);
                if (end != -1) {
                    index = end;
                }
                continue;
            }

            if (c == '`') {
                int end = this.indexOfAccent(script, index);
                if (end == -1) {
                    continue;
                } else {
                    int begin = index + 1;
                    String command = script.substring(begin, end);
                    ScriptStdbuf cache = new ScriptStdbuf(stdout);
                    int exitcode = context.getEngine().eval(session, context, cache, stderr, command);
                    if (exitcode != 0) {
                        throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(58, command));
                    }

                    String message = StringUtils.rtrimBlank(cache);
                    int length = command.length() + 2;
                    script = StringUtils.replace(script, index, length, message);
                    index--;
                    continue;
                }
            }
        }
        return script;
    }

    /**
     * 替换shell文本中的变量 <br>
     * 忽略引号中的字符串常量 <br>
     * 变量格式: ${name} $name
     *
     * @param str            字符串
     * @param map            变量名与变量值集合
     * @param convert        字符串转换接口, 可以为null
     * @param ignoreQuote    true表示不会替换单引号中的占位符
     * @param keepBlankValue true表示变量值是null时,保留变量名的占位符
     * @return
     */
    public String replaceShellVariable(String str, Map<? extends Object, ? extends Object> map, Format convert, boolean ignoreQuote, boolean keepBlankValue) {
        if (str == null || map == null) {
            return null;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (ignoreQuote && c == '\'') {
                int end = this.indexOfQuotation(str, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            if (c == '$') { // variable
                int next = i + 1;
                if (next >= str.length()) {
                    continue;
                }

                char nc = str.charAt(next);
                if (nc == '{') { // ${name}
                    int end = this.indexOfBrace(str, next);
                    if (end != -1) {
                        String name = str.substring(next + 1, end); // variable name
                        if (map.containsKey(name)) {
                            Object value = map.get(name);
                            String valStr = convert == null ? StringUtils.toString(value) : convert.format(value);
                            str = str.substring(0, i) + valStr + str.substring(end + 1);
                            i--; // 替换字符串之后继续从原 $ 字符所在位置开始搜索
                        } else if (!keepBlankValue) {
                            str = str.substring(0, i) + str.substring(end + 1);
                            i--; // 替换字符串之后继续从原 $ 字符所在位置开始搜索
                        }
                    }
                    continue;
                }

                // $name
                if (StringUtils.isLetter(nc) || nc == '_') { // 变量名只能是英文与下划线开头
                    int end = this.indexOfVariableName(str, next);
                    String name = str.substring(next, end);
                    if (map.containsKey(name)) {
                        Object value = map.get(name);
                        String valStr = (convert == null) ? StringUtils.toString(value) : convert.format(value);
                        str = str.substring(0, i) + valStr + str.substring(end);
                        i--;
                    } else if (!keepBlankValue) {
                        str = str.substring(0, i) + str.substring(end);
                        i--;
                    }
                    continue;
                }
            }
        }

        return str;
    }

    /**
     * 替换字符串参数str中的占位符, 如: $? $# $1 $2 <br>
     * <br>
     * $$ 表示用户会话编号 <br>
     * $? 表示上一个命令执行返回值 <br>
     * $# 表示命令输入参数的个数 <br>
     * $0 表示命令名 <br>
     * $1 表示第一个参数值 <br>
     * $2 表示第二个参数值 <br>
     * $3 表示第三个参数值 <br>
     *
     * @param session     用户会话信息
     * @param str         字符串
     * @param ignoreQuote true表示不会替换单引号中的内置命令
     * @return
     */
    public String replaceShellSpecialVariable(UniversalScriptSession session, String str, boolean ignoreQuote) {
        if (str == null) {
            return str;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (ignoreQuote && c == '\'') {
                int end = this.indexOfQuotation(str, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            if (c == '$') {
                int next = i + 1;
                if (next >= str.length()) {
                    break;
                }

                char nc = str.charAt(next);
                if (nc == '{') { // 变量
                    int end = this.indexOfBrace(str, next);
                    if (end != -1) {
                        i = end;
                    }
                    continue;
                }

                if (nc == '?') { // 上一个命令的返回值
                    Integer exitcode = session.getMainProcess().getExitcode();
                    str = str.substring(0, i) + (exitcode == null ? "" : exitcode) + str.substring(next + 1);
                    i--;
                    continue;
                }

                if (nc == '$') { // 当前会话的编号
                    str = str.substring(0, i) + session.getId() + str.substring(next + 1);
                    i--;
                    continue;
                }

                if (nc == '#') { // 参数个数
                    String[] args = session.getFunctionParameter();
                    str = str.substring(0, i) + ((args != null && args.length >= 1) ? args.length - 1 : 0) + str.substring(next + 1);
                    i--;
                    continue;
                }

                if (StringUtils.isNumber(nc)) { // $0 表示方法名 $1 表示第一个参数
                    int end = this.indexOfInteger(str, next);
                    if (end != -1) {
                        String[] args = session.getFunctionParameter();
                        String number = str.substring(next, end);
                        int index = Integer.parseInt(number);
                        if (args != null && index < args.length) {
                            str = str.substring(0, i) + args[index] + str.substring(end);
                            i--;
                        } else {
                            i = end - 1;
                        }
                    }
                    continue;
                }
            }
        }
        return str;
    }

}
