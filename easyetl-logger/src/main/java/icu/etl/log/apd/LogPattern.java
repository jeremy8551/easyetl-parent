package icu.etl.log.apd;

import java.util.ArrayList;
import java.util.List;

import icu.etl.log.MDC;
import icu.etl.log.apd.field.CategoryField;
import icu.etl.log.apd.field.ClassNameField;
import icu.etl.log.apd.field.ConstantField;
import icu.etl.log.apd.field.DateField;
import icu.etl.log.apd.field.FileField;
import icu.etl.log.apd.field.LineField;
import icu.etl.log.apd.field.LinenoField;
import icu.etl.log.apd.field.MDCField;
import icu.etl.log.apd.field.MessageField;
import icu.etl.log.apd.field.MethodField;
import icu.etl.log.apd.field.MillsField;
import icu.etl.log.apd.field.NewlineField;
import icu.etl.log.apd.field.PriorityField;
import icu.etl.log.apd.field.ProcessidField;
import icu.etl.log.apd.field.ThreadName;
import icu.etl.log.apd.field.ThrowableField;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

/**
 * 日志输出格式
 * <p>
 * %c 输出日志信息所属的类目，通常就是所在类的全名 <br>
 * %d 输出日期或时间，默认格式: HH:mm:ss:SSS, 可以在其后指定格式，如：%d{yyyy/MM/dd HH:mm:ss,SSS} <br>
 * %F 输出源文件名 <br>
 * %L 输出代码中的行号 <br>
 * %C 输出 java 类名，%C{1} 输出最后一个元素 <br>
 * %M 输出产生日志信息的方法名 <br>
 * %l 输出日志时间发生的位置，包括类名、线程、及在代码中的行数。如：Test.main(Test.java:10) <br>
 * %X 输出 {@linkplain MDC} 中存储的数值，可以在其后指定格式, 如: %X{name} <br>
 * %m 输出代码中指定的日志信息 <br>
 * %r 输出自应用程序启动到输出该log信息耗费的毫秒数 <br>
 * %n 输出一个回车换行符，Windows平台为”rn”，Unix平台为”n” <br>
 * %p 输出日志信息的优先级，即DEBUG，INFO，WARN，ERROR，FATAL <br>
 * %processId 输出唯一标识符 <br>
 * %ex 同 %throwable 都是输出异常信息 <br>
 * %t 输出产生该日志事件的线程名 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class LogPattern {

    /** 日志格式解析后的字段信息 */
    private List<LogField> list;

    /** 日志格式 */
    private String pattern;

    /**
     * 日志输出格式
     *
     * @param pattern 日志格式
     */
    public LogPattern(String pattern) {
        this.parse(pattern);
    }

    /**
     * 返回输出格式
     *
     * @return 格式信息
     */
    public String getName() {
        return pattern;
    }

    /**
     * 返回日志字段集合
     *
     * @return 日志字段集合
     */
    public List<LogField> getFields() {
        return list;
    }

    /**
     * 解析日志输出格式
     *
     * @param pattern 输出格式
     * @throws IllegalArgumentException 格式错误
     */
    public void parse(String pattern) {
        List<LogField> fields = new ArrayList<LogField>();
        StringBuilder buf = new StringBuilder();

        LogFieldAlign align = null;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '%') {
                int next = i + 1;
                if (next >= pattern.length()) {
                    throw new IllegalArgumentException(pattern);
                }

                // 读取下一个字符
                char nc = pattern.charAt(next);
                if (nc == '%') {
                    buf.append(c);
                    i = next;
                    continue;
                }

                // 保存符号 % 之前缓存的字符常量
                if (buf.length() > 0) {
                    fields.add(new ConstantField(buf.toString()));
                    buf.setLength(0);
                }

                // 解析修饰符表达式
                // %5c
                // %-5c
                // %.5c
                // %20.30c
                if (nc == '.' || nc == '-' || StringUtils.isNumber(nc)) {
                    int end = this.indexPatternEnd(pattern, next + 1);
                    String str = pattern.substring(next, end); // 截取修饰符
                    align = LogFieldAlign.parse(str);

                    if (end >= pattern.length()) {
                        throw new IllegalArgumentException(pattern);
                    }

                    i = end - 1;
                    next = end;
                    nc = pattern.charAt(end);
                }

                // 日志关联的类名
                if (nc == 'c') {
                    if (pattern.startsWith("{", next + 1)) {
                        int begin = pattern.indexOf('{', next + 1);
                        Ensure.isFromZero(begin);
                        int end = pattern.indexOf('}', begin + 1);
                        Ensure.isFromZero(end);
                        String str = pattern.substring(begin + 1, end);

                        if (!StringUtils.isInt(str)) {
                            throw new IllegalArgumentException(pattern + " [" + str + "]");
                        }
                        fields.add(new CategoryField(Integer.parseInt(str)));
                        i = end;
                    } else {
                        fields.add(new CategoryField(0));
                        i = next;
                    }

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 类名
                if (nc == 'C') {
                    fields.add(new ClassNameField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 日期时间
                if (nc == 'd') {
                    if (pattern.startsWith("{", next + 1)) {
                        int begin = pattern.indexOf('{', next + 1);
                        Ensure.isFromZero(begin);
                        int end = pattern.indexOf('}', begin + 1);
                        Ensure.isFromZero(end);
                        String dateformat = pattern.substring(begin + 1, end);
                        fields.add(new DateField(dateformat));
                        i = end;
                    } else {
                        fields.add(new DateField("HH:mm:ss:SSS"));
                        i = next;
                    }

                    if (align != null) { // 不支持格式化操作
                        throw new IllegalArgumentException(pattern);
                    }
                    continue;
                }

                // 异常信息
                if (nc == 'e' && pattern.startsWith("ex", next)) {
                    fields.add(new ThrowableField());
                    i = next + 1;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                if (nc == 'l') {
                    // 输出日志级别
                    if (pattern.toLowerCase().startsWith("level", next)) {
                        fields.add(new PriorityField());
                        i += "level".length();
                    } else { // 输出类名.方法名(文件名:行号)
                        fields.add(new LineField());
                        i = next;
                    }

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 行号
                if (nc == 'L') {
                    fields.add(new LinenoField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 文件名
                if (nc == 'F') {
                    fields.add(new FileField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 输出日志
                if (nc == 'm') {
                    fields.add(new MessageField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 方法名
                if (nc == 'M') {
                    fields.add(new MethodField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 启动时间
                if (nc == 'r') {
                    fields.add(new MillsField());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 换行符
                if (nc == 'n') {
                    fields.add(new NewlineField());
                    i = next;

                    if (align != null) { // 换行符不支持格式化操作
                        throw new IllegalArgumentException(pattern);
                    }
                    continue;
                }

                if (nc == 'p') {
                    if (pattern.startsWith("processId", next)) { // 进程号
                        fields.add(new ProcessidField());
                        i += "processId".length();
                    } else { // 日志级别
                        fields.add(new PriorityField());
                        i = next;
                    }

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // 线程名
                if (nc == 't') {
                    fields.add(new ThreadName());
                    i = next;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                // MDC值
                if (nc == 'X') {
                    if (!pattern.startsWith("{", next + 1)) {
                        throw new IllegalArgumentException(pattern);
                    }

                    int begin = pattern.indexOf('{', next + 1);
                    Ensure.isFromZero(begin);
                    int end = pattern.indexOf('}', begin + 1);
                    Ensure.isFromZero(end);
                    String key = pattern.substring(begin + 1, end);
                    fields.add(new MDCField(key));
                    i = end;

                    if (align != null) {
                        fields.get(fields.size() - 1).setAlign(align);
                        align = null;
                    }
                    continue;
                }

                throw new UnsupportedOperationException(nc + " in " + pattern + "[" + next + "]");
            } else {
                buf.append(c);
            }
        }

        // 如果还有字符串常量
        if (buf.length() > 0) {
            fields.add(new ConstantField(buf.toString()));
            buf.setLength(0);
        }

        // 保存
        this.pattern = pattern;
        this.list = fields;
    }

    /**
     * 搜索修饰符结束位置
     *
     * @param pattern 表达式
     * @param from    开始位置
     * @return 结束位置
     */
    private int indexPatternEnd(String pattern, int from) {
        for (int i = from; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (StringUtils.isLetter(c)) {
                return i;
            } else if (c == '.' || c == '-' || StringUtils.isNumber(c)) {
                continue;
            } else {
                throw new IllegalArgumentException(pattern + " [" + c + "]");
            }
        }
        return pattern.length();
    }

}
