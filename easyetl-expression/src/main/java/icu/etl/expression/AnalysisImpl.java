package icu.etl.expression;

import java.util.ArrayList;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 语句分析器的实现类
 *
 * @author jeremy8551@qq.com
 */
@EasyBean
public class AnalysisImpl extends BaseAnalysis {

    /** 语句分隔符 */
    protected char token;

    /**
     * 初始化
     */
    public AnalysisImpl() {
        super();
        this.token = ';';
    }

    public char getToken() {
        return this.token;
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

            // 忽略字符常量中的空白
            if (c == '\'') {
                i = this.indexOfQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略双引号中的字符串常量
            if (c == '\"') {
                i = this.indexOfDoubleQuotation(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略命令替换
            if (c == '`') {
                i = this.indexOfAccent(str, i);
                if (i == -1) {
                    list.add(str.subSequence(begin, str.length()).toString());
                    return list;
                }
                continue;
            }

            // 忽略空白字符和指定参数字符数组中的字符
            if (Character.isWhitespace(c) || StringUtils.inArray(c, array)) {
                list.add(str.subSequence(begin, i).toString());
                for (int j = i + 1; j < str.length(); j++) {
                    char nextChar = str.charAt(j);
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

        if (begin < str.length()) {
            list.add(str.subSequence(begin, str.length()).toString());
        } else if (begin == str.length()) {
            list.add("");
        }

        return list;
    }

    public int indexOf(CharSequence str, String dest, int from, int left, int right) {
        if (str == null) {
            return -1;
        }

        if (dest == null || dest.length() == 0 || from < 0) {
            throw new IllegalArgumentException(str + ", " + dest + ", " + from + ", " + left);
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

            // 忽略反引号中的内容
            if (c == '`') {
                int end = this.indexOfAccent(str, i);
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

    public int indexOfParenthes(CharSequence str, int from) { // TODO 根上级方法可以合并
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

    public int indexOfBrace(CharSequence str, int from) {
        if (str == null) {
            return -1;
        }
        if (from < 0) {
            throw new IllegalArgumentException(str + ", " + from);
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
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

    public int indexOfSemicolon(CharSequence str, int from) {
        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);

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

            // 忽略括号中的空白字符与逗号
            if (c == '(') {
                int end = this.indexOfParenthes(str, i);
                if (end == -1) {
                    throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(112, str));
                }
                i = end;
                continue;
            }

            // 忽略命令替换中的管道符
            if (c == '`') {
                int end = this.indexOfAccent(str, i);
                if (end != -1) {
                    i = end;
                }
                continue;
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

            // 忽略反引号中的内容
            if (c == '`') {
                int j = this.indexOfAccent(str, i);
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

}

