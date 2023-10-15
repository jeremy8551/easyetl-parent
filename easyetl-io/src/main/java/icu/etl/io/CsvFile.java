package icu.etl.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * Csv 文件读取类 <br>
 * 逗号分隔文件中字段, 文件以纯文本形式存储表格数据， 文件的每一行都是一个数据记录。 <br>
 * 每个记录由一个或多个字段组成，用逗号分割。<br>
 * 每行记录位于一个单独行上，用回车换行分隔。 <br>
 * 文件的最后一行可以有结尾回车换行符，也可以没有<br>
 * 每一行可以存在一个可选的标题头， 格式和普通记录行的格式一样。<br>
 * 在标题头行和普通行每行记录中，会存在一个或多个由半角逗号分隔的字段。 <br>
 * 如果csv每个单元格中存在回车换行，则用双引号自动闭包。 <br>
 *
 * @author jeremy8551@qq.com
 */
@EasyBeanClass(kind = "csv", mode = "file", major = "", minor = "", description = "CSV格式文件", type = TextTableFile.class)
public class CsvFile extends CommonTextTableFile implements TextTableFile {

    public CsvFile() {
        super();
        this.setEscape('\"');
    }

    /**
     * 初始化
     *
     * @param filepath csv文件的绝对路径
     */
    public CsvFile(String filepath) {
        super();
        this.setAbsolutePath(filepath);
    }

    /**
     * 初始化
     *
     * @param file csv文件
     */
    public CsvFile(File file) {
        this(file.getAbsolutePath());
    }

    public CsvFile clone() {
        CsvFile obj = new CsvFile();
        this.clone(obj);
        return obj;
    }

    public TableLineRuler getRuler() {
        return new TableLineRuler() {
            public void split(String str, List<String> list) {
                CsvFile.splitCsvFileLine(str, list);
            }

            public String join(TableLine line) {
                int column = line.getColumn();
                StringBuilder buf = new StringBuilder();
                for (int i = 1; i <= column; ) {
                    String str = line.getColumn(i);

                    if (str != null) {
                        /**
                         * 如果文本信息中包含半角逗号、半角双引号、回车或换行时前后需要加双引号标记 <br>
                         * 需要在字符串前后添加双引号
                         */
                        if (str.indexOf(',') != -1 || str.indexOf('"') != -1 || str.indexOf('\r') != -1 || str.indexOf('\n') != -1) {
                            str = StringUtils.replaceAll(str, "\"", "\"\"");
                            str = "\"" + str + "\"";
                        } else {
                            str = StringUtils.replaceAll(str, "\"", "\"\"");
                        }
                        buf.append(str);
                    }

                    if (++i < column) {
                        buf.append(',');
                    }
                }
                return buf.toString();
            }

            public String replace(TextTableLine line, int position, String value) {
                return replaceFieldValue(line.getContent(), position, value);
            }
        };
    }

    /**
     * 提取csv字符串中的字段
     *
     * @param str 字符串
     * @return 字段数组
     */
    public static String[] splitCsvFileLine(String str) {
        List<String> list = new ArrayList<String>(10);
        CsvFile.splitCsvFileLine(str, list);
        return list.toArray(new String[list.size()]);
    }

    /**
     * 提取csv格式字符串(字符串用双引号包围, 使用半角逗号分隔字段)中的所有字段数值
     *
     * @param str  字符串
     * @param list 字段集合，用于存储解析后的所有字段
     */
    public static void splitCsvFileLine(String str, Collection<String> list) {
        if (str == null) {
            return;
        }

        StringBuilder buf = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '"') {
                i = indexOfCsvStrEndPosition(str, i + 1, length, buf);
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
     * 搜索字符串结束位置，并将字符串保存到 buffer 中同时删除字符串二端的双引号
     *
     * @param str    字符数组
     * @param begin  起始位置
     * @param end    终止位置（不包括）
     * @param buffer 缓冲区
     * @return
     */
    protected static int indexOfCsvStrEndPosition(String str, int begin, int end, StringBuilder buffer) {
        for (int j = begin, next = 0; j < end; j++) {
            char sc = str.charAt(j);
            if (sc == '"') {
                next = j + 1;
                if (next >= end) { // 字符串结尾
                    // buffer.append(sc);
                    return j;
                } else {
                    char nc = str.charAt(next);
                    if (nc == '"') { // 转义字符
                        buffer.append(sc);
                        j = next;
                    } else if (nc == ',') { // 转义字符
                        // buffer.append(sc);
                        return j;
                    } else {
                        buffer.append(sc);
                    }
                }
            } else {
                buffer.append(sc);
                continue;
            }
        }
        return end;
    }

    /**
     * 把字符串数组按csv文件格式拼成一个行 <br>
     * 如果字符串中包含半角逗号、双引号、回车符换行符的需要在字符串二端增加双引号 <br>
     * 字符串内部的双引号前加一个双引号表示转义 <br>
     *
     * @param array 字符串数组
     * @return csv文件格式字符串
     */
    public static String joinFields(String... array) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String str = array[i];

            if (str != null) {
                /**
                 * 如果文本信息中包含半角逗号、半角双引号、回车或换行时前后需要加双引号标记 <br>
                 * 需要在字符串前后添加双引号
                 */
                if (str.indexOf(',') != -1 || str.indexOf('"') != -1 || str.indexOf('\r') != -1 || str.indexOf('\n') != -1) {
                    str = StringUtils.replaceAll(array[i], "\"", "\"\"");
                    str = "\"" + str + "\"";
                } else {
                    str = StringUtils.replaceAll(array[i], "\"", "\"\"");
                }
                buf.append(str);
            }

            if ((i + 1) < array.length) {
                buf.append(',');
            }
        }
        return buf.toString();
    }

    /**
     * 使用字符串参数newStr替换DB2 del文件格式(逗号分隔)字符串str中第column个字段值
     *
     * @param str    字符串
     * @param column 列数,从1开始
     * @param newStr 替换的字符串
     * @return
     */
    public static String replaceFieldValue(CharSequence str, int column, String newStr) {
        if (newStr == null) {
            throw new NullPointerException();
        }
        if (column <= 0) {
            throw new IllegalArgumentException(String.valueOf(column));
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
                index = indexOfStrEndPosition(str, index + 1, length);
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
            throw new RuntimeException(ResourcesUtils.getDatabaseMessage(51, str, column));
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
    protected static int indexOfStrEndPosition(CharSequence str, int begin, int length) {
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

}