package icu.etl.database.db2;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.CharsetEncoder;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.database.DatabaseException;
import icu.etl.database.db2.format.DB2DecimalFormat;
import icu.etl.database.db2.format.DB2DoubleFormat;
import icu.etl.database.db2.format.DB2FloatFormat;
import icu.etl.io.CommonTextTableFile;
import icu.etl.io.TableLine;
import icu.etl.io.TableLineRuler;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableLine;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * DB2 export 命令导出的文件格式
 *
 * @author jeremy8551@qq.com
 */
@EasyBeanClass(kind = "del", mode = "export", major = "", minor = "", description = "DB2数据库export命令导出文件格式, 逗号分隔，双引号转义字符，双引号是字符串限定符", type = TextTableFile.class)
public class DB2ExportFile extends CommonTextTableFile implements TextTableFile {

    public DB2ExportFile() {
        super();
        this.setCharDelimiter("\"");
        this.setEscape('\"');
        this.setLineSeparator(FileUtils.lineSeparatorUnix);
    }

    public DB2ExportFile(String filepath) {
        this();
        this.setAbsolutePath(filepath);
    }

    public DB2ExportFile(File file) {
        this(file.getAbsolutePath());
    }

    public TableLineRuler getRuler() {
        return new TableLineRuler() {

            public void split(String str, List<String> list) {
                DB2ExportFile.splitDB2ExportFileLine(str, false, list);
            }

            public String join(TableLine line) {
                int column = line.getColumn();

                int length = column + 2; // the length of join field array
                for (int i = 1; i <= column; i++) {
                    length += line.getColumn(i).length() + 4;
                }

                StringBuilder buf = new StringBuilder(length); // create a buffer size
                for (int i = 1; i <= column; ) {
                    String field = line.getColumn(i); // set element of array value

                    if (field != null) {
                        buf.append('\"');
                        buf.append(StringUtils.replaceAll(StringUtils.replaceAll(field, "\"", "\"\""), ",", "\",")); // 对字符串中的半角逗号与双引号进行转义
                        buf.append('\"');
                    }

                    if (++i <= column) {
                        buf.append(',');
                    }
                }
                return buf.toString();
            }

            public String replace(TextTableLine line, int position, String value) {
                return DB2ExportFile.replaceDB2FieldValue(line.getContent(), position, value);
            }

        };
    }

    // 返回一个副本
    public DB2ExportFile clone() {
        DB2ExportFile obj = new DB2ExportFile();
        this.clone(obj);
        return obj;
    }

    /**
     * 解析字符串参数str中的字段 <br>
     * 默认使用半角逗号分隔字段 <br>
     * 增加对双引号的支持：连续两个双引号则认为是一个双引号 <br>
     *
     * @param str       字符串
     * @param keepQuote true表示保留字符串数值二端的双引号
     * @return
     */
    public static String[] splitDB2ExportFileLine(CharSequence str, boolean keepQuote) {
        List<String> list = new ArrayList<String>();
        DB2ExportFile.splitDB2ExportFileLine(str, keepQuote, list);
        return list.toArray(new String[list.size()]);
    }

    /**
     * 解析字符串参数str中的字段 <br>
     * 默认使用半角逗号分隔字段 <br>
     * 增加对双引号的支持：连续两个双引号则认为是一个双引号 <br>
     *
     * @param str       字符串
     * @param keepQuote true表示保留字符串数值二端的双引号
     * @param list      分隔后字段的存储集合
     */
    public static void splitDB2ExportFileLine(CharSequence str, boolean keepQuote, Collection<String> list) {
        if (str == null) {
            return;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0, length = str.length(); i < length; i++) {
            char c = str.charAt(i);
            if (c == '"') {
                if (keepQuote) {
                    i = indexOfDB2StrEndPosition1(str, i + 1, length, buf);
                } else {
                    i = indexOfDB2StrEndPosition2(str, i + 1, length, buf);
                }
                continue;
            }

            // 字段分隔符
            else if (c == ',') {
                list.add(buf.toString());
                buf.setLength(0);
                continue;
            }

            // 其他字符
            else {
                buf.append(c);
                continue;
            }
        }

        list.add(buf.toString());
        buf.setLength(0);
    }

    /**
     * 搜索字符串结束位置，并将字符串保存到 buffer 中同时保留字符串二端的双引号
     *
     * @param str    字符数组
     * @param begin  起始位置
     * @param end    终止位置（不包括）
     * @param buffer 缓冲区
     * @return
     */
    protected static int indexOfDB2StrEndPosition1(CharSequence str, int begin, int end, StringBuilder buffer) {
        buffer.append('"');
        for (int j = begin, next = 0; j < end; j++) {
            char c = str.charAt(j);
            if (c == '"') {
                buffer.append('"');
                next = j + 1;
                if (next >= end) { // 字符串结尾
                    return j;
                } else {
                    char nc = str.charAt(next);
                    if (nc == '"') { // 转义字符
                        buffer.append(c);
                        j = next;
                    } else if (nc == ',') { // 转义字符
                        // buffer.append(sc);
                        return j;
                    } else {
                        buffer.append(c);
                    }
                }
            } else {
                buffer.append(c);
                continue;
            }
        }
        return end;
    }

    /**
     * 搜索字符串结束位置，并将字符串保存到 buffer 中同时删除字符串二端的双引号
     *
     * @param str    字符数组
     * @param begin  起始位置
     * @param end    终止位置（不包括）
     * @param buffer 缓冲区
     * @return
     */
    protected static int indexOfDB2StrEndPosition2(CharSequence str, int begin, int end, StringBuilder buffer) {
        for (int j = begin, next = 0; j < end; j++) {
            char c = str.charAt(j);
            if (c == '"') {
                next = j + 1;
                if (next >= end) { // 字符串结尾
                    // buffer.append(sc);
                    return j;
                } else {
                    char nc = str.charAt(next);
                    if (nc == '"') { // 转义字符
                        buffer.append(c);
                        j = next;
                    } else if (nc == ',') { // 转义字符
                        // buffer.append(sc);
                        return j;
                    } else {
                        buffer.append(c);
                    }
                }
            } else {
                buffer.append(c);
                continue;
            }
        }
        return end;
    }

    /**
     * 使用字符串参数newStr替换DB2 del文件格式(逗号分隔)字符串str中第column个字段值
     *
     * @param str    字符串
     * @param column 列数,从1开始
     * @param newStr 替换的字符串
     * @return
     */
    public static String replaceDB2FieldValue(CharSequence str, int column, String newStr) {
        if (column <= 0 || newStr == null) {
            throw new IllegalArgumentException("replaceDB2FieldValue(\"" + str + "\", " + column + ", \"" + newStr + "\")");
        }
        if (str == null) {
            return null;
        }

        int start = -1, end = -1, index = 0;
        int length = str.length(); // 字符串长度
        int fieldCol = 1; // 当前字段的列数
        boolean isFC = column == 1; // 是否请求第一个字段
        if (isFC) {
            start = 0;
        }

        for (; index < length; index++) {
            char c = str.charAt(index);
            if (c == '"') { // 字符串开始位置
                if (isFC) {
                    start = index;
                }
                index = indexOfDB2StrEndPosition(str, index + 1, length);
                if (isFC) {
                    end = index;
                    break;
                } else {
                    continue;
                }
            } else if (c == ',') { // 字段分隔符
                if (isFC) {
                    end = index - 1;
                    break;
                }

                fieldCol++;
                if (fieldCol == column) {
                    start = index + 1;
                    isFC = true;
                    continue;
                }
            } else { // 其他字符
                continue;
            }
        }

        if (isFC) {
            if (end == -1) {
                if (index == str.length()) { // 替换最后一个字段
                    return str.subSequence(0, start) + newStr;
                } else { // 替换第一个字段
                    return newStr + str;
                }
            } else { // 替换中间字段
                return str.subSequence(0, start) + newStr + str.subSequence(end + 1, str.length());
            }
        } else {
            throw new DatabaseException(ResourcesUtils.getDatabaseMessage(51, str, column));
        }
    }

    /**
     * 返回DB2 字符串的结尾位置
     *
     * @param str    字符串
     * @param begin  字符串起始位置
     * @param length 最大搜索长度（不包括）
     * @return
     */
    protected static int indexOfDB2StrEndPosition(CharSequence str, int begin, int length) {
        for (int j = begin, next = 0; j < length; j++) {
            char c = str.charAt(j);
            if (c == '"') {
                next = j + 1;
                if (next >= length) { // 字符串结尾
                    return j;
                } else {
                    char nc = str.charAt(next);
                    if (nc == '"') { // 转义字符
                        j = next;
                    } else if (nc == ',') { // 转义字符
                        return j;
                    }
                }
            }
        }
        return length;
    }

    /**
     * 把字符串中的字段按 DB2 导出规则编码同时删除字符串中的非法字符
     *
     * @param encoder 字符串编码类
     * @param str     字符串
     * @return 编码后的字符串
     */
    public static String toDB2ExportString(CharsetEncoder encoder, CharSequence str) {
        if (str == null) {
            return null;
        }
        if (encoder == null) {
            java.nio.charset.Charset charset = java.nio.charset.Charset.forName(StringUtils.CHARSET);
            encoder = charset.newEncoder();
        }

        StringBuilder buf = new StringBuilder(str.length());
        buf.append('"');
        for (int i = 0, length = str.length(); i < length; i++) {
            char c = str.charAt(i);
            if (encoder.canEncode(c)) {
                if (c == '\"') {
                    buf.append('\"');
                } else if (c == '\r' || c == '\n') {
                    // 过滤回车换行
                } else {
                    buf.append(c);
                }
            } else {
                // if (log.isWarnEnabled()) {
                // log.warn("过滤非法字符 {" + c + "}");
                // }
            }
        }
        buf.append('"');
        return buf.toString();
    }

    /**
     * 把字符串转为 DB2 EXPORT导出格式
     *
     * @param str 字符串
     * @return 字符串, 格式：s12"s\nd\rf == s12""sdf
     */
    public static String toDB2ExportString(CharSequence str) {
        if (str == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(str.length());
        buf.append('"');
        for (int i = 0, length = str.length(); i < length; i++) {
            char c = str.charAt(i);
            if (c == '\"') {
                buf.append('\"');
            } else if (c == '\r' || c == '\n') {
                // 过滤回车换行
            } else {
                buf.append(c);
            }
        }
        buf.append('"');
        return buf.toString();
    }

    /**
     * 把 BigDecimal 对象转为 DB2 EXPORT导出格式
     *
     * @param value     数值对象
     * @param precision 整数部分精确值
     * @param scale     小数位数保留个数
     * @return 字符串, 格式：s12"s\nd\rf == s12""sdf
     */
    public static String toDB2ExportString(BigDecimal value, int precision, int scale) {
        if (value == null) {
            return "";
        } else {
            DB2DecimalFormat format = new DB2DecimalFormat();
            format.applyPattern(precision, scale);
            return new String(format.getChars(), 0, format.length());
        }
    }

    /**
     * 把 Integer 对象转为 DB2 EXPORT导出格式
     *
     * @param value 整数
     * @return 字符串, 格式：10000
     */
    public static String toDB2ExportString(Integer value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 把 Long 对象转为 DB2 EXPORT导出格式
     *
     * @param value long型数值
     * @return
     */
    public static String toDB2ExportString(Long value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 把 Double 对象转为 DB2 EXPORT导出格式
     *
     * @param value double型数值
     * @return 字符串, 格式: +1.23456789000000E+008
     */
    public static String toDB2ExportString(Double value) {
        return value == null ? "" : new DB2DoubleFormat().format(value).toString();
    }

    /**
     * 把 Float 对象转为 DB2 EXPORT导出格式
     *
     * @param value 浮点数
     * @return 字符串, 格式: +0.00000000001234567
     */
    public static String toDB2ExportString(Float value) {
        return value == null ? "" : new DB2FloatFormat().format(value).toString();
    }

    /**
     * 把 Date 对象转为 DB2 EXPORT导出格式
     *
     * @param value 日期
     * @return 字符串, 格式: yyyy-MM-dd
     */
    public static String toDB2ExportString(java.util.Date value) {
        return value == null ? "" : Dates.format10(value);
    }

    /**
     * 把 Time 对象转为 DB2 EXPORT导出格式
     *
     * @param time 时间
     * @return 字符串, 格式: hh.mm.ss
     */
    public static String toDB2ExportString(Time time) {
        if (time == null) {
            return "";
        } else {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("hh.mm.ss");
            return format.format(time);
        }
    }

    /**
     * 把 Timestamp 对象转为 DB2 EXPORT导出格式
     *
     * @param timestamp 时间戳
     * @return 字符串, 格式: yyyy-MM-dd hh.mm.ss.SSSSSS
     */
    public static String toDB2ExportString(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        } else {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("yyyy-MM-dd HH.mm.ss.SSSSSS");
            return format.format(timestamp);
        }
    }

    /**
     * 按DB2 Export 文件格式拼接字段数组
     *
     * @param array
     * @return
     */
    public static String joinFields(String... array) {
        int length = array.length + 2; // the length of join field array
        for (int i = 0; i < array.length; i++) {
            length += array[i].length() + 4;
        }

        StringBuilder buf = new StringBuilder(length); // create a buffer size
        for (int i = 0; i < array.length; ) {
            String field = array[i]; // set element of array value

            if (field != null) {
                buf.append('\"');
                buf.append(StringUtils.replaceAll(StringUtils.replaceAll(field, "\"", "\"\""), ",", "\",")); // 对字符串中的半角逗号与双引号进行转义
                buf.append('\"');
            }

            if (++i < array.length) {
                buf.append(',');
            }
        }
        return buf.toString();
    }

}