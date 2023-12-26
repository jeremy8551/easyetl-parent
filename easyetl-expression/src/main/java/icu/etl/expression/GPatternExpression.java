package icu.etl.expression;

import java.util.ArrayList;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 通配符表达式
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-08
 */
public class GPatternExpression {
    private final static Log log = LogFactory.getLog(GPatternExpression.class);

    /**
     * 判断字符串参数 {@code str} 是否与通配符参数 {@code gpattern} 匹配
     *
     * @param str      字符串
     * @param gpattern 通配符表达式 {@linkplain #GPatternExpression(Analysis, String)}
     * @return 返回true表示字符串与通配符匹配
     */
    public static boolean match(String str, String gpattern) {
        return str != null && gpattern != null && (str.equals(gpattern) || new GPatternExpression(gpattern).match(str));
    }

    /** 语句分析器 */
    protected Analysis analysis;

    /** 通配符表达式 */
    protected String pattern;

    /** 通配符扩展结果 */
    protected List<String> patterns;

    /** 子表达式对应的 java 正则表达式 */
    protected List<String> javaRegexs;

    /**
     * 初始化
     *
     * @param pattern 通配符表达式 <br>
     *                * 表示匹配零个或多个字符（不包含 / 字符） <br>
     *                ? 表示匹配一个字符（不包含 / 字符） <br>
     *                [abc] 表示匹配 abc中的一个字符 <br>
     *                {a,b,c{1,2},d{g..i}} 表示匹配 a 或 b 或 c1 或 c2 或 dg 或 gh 或 gi <br>
     */
    public GPatternExpression(String pattern) {
        this(new BaseAnalysis(), pattern);
    }

    /**
     * 解析通配符并执行初始化操作
     *
     * @param analysis 语句分析器
     * @param pattern  通配符表达式 <br>
     *                 * 表示匹配零个或多个字符（不包含 / 字符） <br>
     *                 ? 表示匹配一个字符（不包含 / 字符） <br>
     *                 [abc] 表示匹配 abc中的一个字符 <br>
     *                 {a,b,c{1,2},d{g..i}} 表示匹配 a 或 b 或 c1 或 c2 或 dg 或 gh 或 gi <br>
     */
    public GPatternExpression(Analysis analysis, String pattern) {
        this.analysis = Ensure.notNull(analysis);
        this.javaRegexs = new ArrayList<String>();
        this.pattern = Ensure.notNull(pattern);
        this.patterns = new ArrayList<String>();
        this.patterns.add(pattern);
        this.expandPattern();
        this.toJavaRegexs();

        if (log.isTraceEnabled()) {
            log.trace(ResourcesUtils.getExpressionMessage(63, pattern, this.patterns, this.javaRegexs));
        }
    }

    /**
     * 判断字符串参数是否与通配符匹配
     *
     * @param str
     * @return
     */
    public boolean match(String str) {
        for (String regex : this.javaRegexs) {
            if (str.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通配符扩展 <br>
     * <br>
     * t{1,2,{a*, b, d{1,2,3}},0} <br>
     * <br>
     * t{1,2,a*,0} <br>
     * t{1,2,b,0} <br>
     * t{1,2,d{1,2,3},0} <br>
     * <br>
     * t{1,2,d1,0} <br>
     * t{1,2,d2,0} <br>
     * t{1,2,d3,0} <br>
     * <br>
     */
    protected synchronized void expandPattern() {
        for (int index = 0; index < this.patterns.size(); index++) {
            String str = this.patterns.get(index);

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '{') {
                    int end = this.analysis.indexOfBrace(str, i);
                    if (end != -1) {
                        String expr = str.substring(i + 1, end);
                        if (this.analysis.indexOf(expr, "{", 0, 2, 2) == -1 && this.analysis.indexOf(expr, "..", 0, 2, 2) != -1) {
                            String[] array = StringUtils.split(expr, "..");
                            if (array.length == 2 && StringUtils.isNotBlank(array[0]) && StringUtils.isNotBlank(array[1])) {
                                if ((StringUtils.isNumber(array[0]) && StringUtils.isNumber(array[1])) //
                                        || (array[0].length() == 1 && array[1].length() == 1 && StringUtils.isLetter(array[0].charAt(0)) && StringUtils.isLetter(array[1].charAt(0))) //
                                ) {
                                    char c0 = array[0].charAt(0);
                                    char c1 = array[1].charAt(0);
                                    if (c0 <= c1) {
                                        this.patterns.remove(index);
                                        while (c0 <= c1) {
                                            int length = end - i + 1;
                                            String newExpr = StringUtils.replace(str, i, length, String.valueOf(c0));
                                            this.patterns.add(index, newExpr);
                                            c0++;
                                        }
                                        index--;
                                    } else {
                                        this.patterns.remove(index);
                                        while (c0 >= c1) {
                                            int length = end - i + 1;
                                            String newExpr = StringUtils.replace(str, i, length, String.valueOf(c0));
                                            this.patterns.add(index, newExpr);
                                            c0--;
                                        }
                                        index--;
                                    }
                                }
                            }
                            // index--; 如果表达式不合法则不做解析处理
                            break;
                        } else { // {element, element, element}
                            List<String> list = this.analysis.split(expr, ',');
                            this.patterns.remove(index);
                            for (String pattern : list) {
                                int length = end - i + 1;
                                String newExpr = StringUtils.replace(str, i, length, pattern); // 替换所有模式类型
                                this.patterns.add(index, newExpr); // 保存新模式
                            }
                            index--;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 将通配符扩展结果转为正则表达式
     */
    protected void toJavaRegexs() {
        this.javaRegexs = new ArrayList<String>(this.patterns.size());
        for (String pattern : this.patterns) {
            String javaRegex = this.toJavaRegex(pattern);
            this.javaRegexs.add(javaRegex);
        }
    }

    /**
     * 将linux通配符转为JAVA 正则表达式
     *
     * @param pattern 通配符表达式
     * @return
     */
    protected String toJavaRegex(String pattern) {
        StringBuilder buf = new StringBuilder(pattern.length() + 12);
        buf.append('^');
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            // 匹配任意单个字符，不包括路径标识”/“
            if (c == '?') {
                buf.append("[^\\/]");
            }

            // 匹配任意长度的字符，包括空字符,不包括路径标识”/“
            else if (c == '*') {
                buf.append("[^\\/]*");
            }

            // 引用文件名的时候，需要把文件名放在单引号里面 'fo*'
            else if (c == '\'') {
                int next = i + 1;
                int end = this.analysis.indexOfQuotation(pattern, next);
                if (end == -1) {
                    buf.append("\\").append(c);
                } else {
                    buf.append(escape(pattern.substring(next, end)));
                }
            } else if (c == '"') {
                int next = i + 1;
                int end = this.analysis.indexOfDoubleQuotation(pattern, next);
                if (end == -1) {
                    buf.append("\\").append(c);
                } else {
                    buf.append(escape(pattern.substring(next, end)));
                }
            }

            // 匹配方括号之中的任意一个字符，比如[aeiou]可以匹配五个元音字母。
            else if (c == '[') {
                // [^ac-f] 转为[^a|c-f]
                int end = pattern.indexOf(']', i);
                if (end == -1) {
                    throw new IllegalArgumentException(pattern);
                }

                buf.append('[');
                String str = pattern.substring(i + 1, end);
                for (int z = 0; z < str.length(); ) {
                    char cc = str.charAt(z);

                    if (cc == '^' || cc == '-') {
                        buf.append(cc);
                        z++;
                        continue;
                    }

                    if (cc == '!') {
                        buf.append('^');
                        z++;
                        continue;
                    }

                    buf.append(cc);
                    if (++z < str.length() && str.charAt(z) != '-') {
                        buf.append('|');
                    }
                }
                buf.append(']');
                i = end;
            }

            // 对 java 正则表达式的关键字符进行转义
            else if ("$^[](){}&|?*+.,!=<>/\\".indexOf(c) != -1) {
                buf.append('\\').append(c);
            } else {
                buf.append(c);
            }
        }
        buf.append('$');
        return buf.toString();
    }

    /**
     * 将字符串中关键字字符转义为普通字符
     *
     * @param str 字符串
     * @return
     */
    protected String escape(String str) {
        StringBuilder buf = new StringBuilder(str.length() + 10);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ("$^[](){}&|?*+.,!=<>/\\-".indexOf(c) != -1) {
                buf.append('\\');
            }
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * 返回通配符对应的所有匹配模式集合
     *
     * @return
     */
    public List<String> getPatterns() {
        return this.patterns;
    }

    public String toString() {
        return this.pattern;
    }

}
