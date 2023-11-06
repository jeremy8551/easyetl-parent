package icu.etl.script.compiler;

import java.io.IOException;
import java.io.Reader;

import icu.etl.io.CacheLineReader;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptReader;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 词法分析器的接口实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptReader extends ScriptAnalysis implements UniversalScriptReader {

    /** 缓存行 */
    private String cacheline;

    /** 语句输入流 */
    protected CacheLineReader in;

    /** 语句分析器 */
    private UniversalScriptAnalysis analysis;

    /** 语句分隔符 */
    private char token;

    /** 注释起始标志 */
    private char comment;

    /** 转义字符 */
    private char escape;

    /**
     * 初始化
     *
     * @param in 语句输入流
     */
    public ScriptReader(Reader in) {
        this.analysis = this;
        this.token = this.getToken();
        this.comment = this.getComment();
        this.escape = this.getEscape();
        this.in = new CacheLineReader(in);
    }

    /**
     * 初始化
     *
     * @param in 语句输入流
     * @param n  起始行数
     */
    public ScriptReader(Reader in, long n) {
        this.analysis = this;
        this.token = this.getToken();
        this.comment = this.getComment();
        this.escape = this.getEscape();
        this.in = new CacheLineReader(in);
        this.in.setLineNumber(n);
    }

    public long getLineNumber() {
        return this.in.getLineNumber();
    }

    public void setNextline(String str) throws IOException {
        this.in.setCurrentLine(str);
    }

    public synchronized String previewline() throws IOException {
        // 只能缓存一行
        if (this.cacheline != null) {
            return this.cacheline;
        }

        String line = this.in.readLine();
        if (line == null) {
            return null;
        }

        int length = line.length();
        if (length == 0) {
            this.cacheline = null;
            return this.previewline();
        }

        // 从左向右过滤空白字符与语句分隔符
        int index = 0;
        char c = ' ';
        while (index < length && (Character.isWhitespace((c = line.charAt(index))) || c == this.token)) {
            index++;
        }

        // 最左侧是 # 表示脚本单行注释
        if (c == this.comment) {
            int indexOf = line.indexOf(this.in.getLineSeparator(), index);
            if (indexOf == -1) {
                this.cacheline = null;
                return this.previewline();
            } else {
                this.cacheline = null;
                this.in.setCurrentLine(line.substring(indexOf));
                return this.previewline();
            }
        }

        // 最左侧是 -- 表示SQL单行注释
        if (c == '-') {
            int next = index + 1;
            if (next < length && line.charAt(next) == '-') {
                int indexOf = line.indexOf(this.in.getLineSeparator(), next);
                if (indexOf == -1) {
                    this.cacheline = null;
                    return this.previewline();
                } else {
                    this.cacheline = null;
                    this.in.setCurrentLine(line.substring(indexOf));
                    return this.previewline();
                }
            }
        }

        if (index == length) { // 空白字符行
            this.cacheline = null;
            return this.previewline();
        } else {
            return (this.cacheline = line.substring(index));
        }
    }

    public synchronized String readLine() throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : this.readline(this.in, line);
        } finally {
            this.cacheline = null;
        }
    }

    /**
     * 读取一行信息
     *
     * @param in
     * @param currentLine
     * @return
     * @throws IOException
     */
    private String readline(CacheLineReader in, String currentLine) throws IOException {
        int index = this.analysis.indexOf(currentLine, new char[]{'\n', '\r'}, 0);
        if (index == -1) {
            return currentLine;
        } else {
            String newline = currentLine.substring(0, index);
            String nextline = StringUtils.ltrimBlank(currentLine.substring(index + 1)); // 下一行数据
            in.setCurrentLine(nextline);
            return newline;
        }
    }

    public synchronized String readSinglelineScript() throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : StringUtils.rtrimBlank(this.readSinglelineScript(this.in, line), this.token);
        } finally {
            this.cacheline = null;
        }
    }

    /**
     * 读取当前行内容直到语句分隔符或回车符或换行符或脚本注释符
     *
     * @param in
     * @param currentLine
     * @return
     * @throws IOException
     */
    private StringBuilder readSinglelineScript(CacheLineReader in, String currentLine) throws IOException {
        int index = this.analysis.indexOf(currentLine, new char[]{this.comment, this.token, '\n', '\r'}, 0);
        if (index == -1) {
            StringBuilder buf = this.mergeNextLine(in, currentLine);
            index = this.analysis.indexOf(buf, new char[]{this.comment, this.token, '\n', '\r'}, 0);
            if (index == -1) {
                return buf;
            } else {
                in.setCurrentLine(buf.substring(index));
                return buf.delete(index, buf.length());
            }
        } else {
            in.setCurrentLine(currentLine.substring(index));
            return new StringBuilder(currentLine.substring(0, index));
        }
    }

    /**
     * 如果行末是转义字符，则读取下一行合并到当前行
     *
     * @param in
     * @param currentLine
     * @return
     * @throws IOException
     */
    private StringBuilder mergeNextLine(CacheLineReader in, String currentLine) throws IOException {
        StringBuilder buf = new StringBuilder(currentLine);
        while (buf.length() > 0 && buf.charAt(buf.length() - 1) == this.escape) { // 字符串最后一个字符是转义字符，需要合并下一行
            buf.deleteCharAt(buf.length() - 1); // 删除最后一个转义字符

            String line = in.readLine();
            if (line == null) {
                return buf;
            } else {
                buf.append(line);
            }
        }
        return buf;
    }

    public synchronized String readMultilineScript() throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : StringUtils.rtrimBlank(this.readMultilineScript(this.in, line), this.token);
        } finally {
            this.cacheline = null;
        }
    }

    /**
     * 读取一个 SQL 语句
     *
     * @param in
     * @param currentLine
     * @return
     * @throws IOException
     */
    private StringBuilder readMultilineScript(CacheLineReader in, String currentLine) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < currentLine.length(); i++) {
            char c = currentLine.charAt(i);
            int next = i + 1; // 下一个字符的位置

            // 忽略字符串常量中的内容
            if (c == '\'') {
                int index = this.indexOfSQLQuotation(currentLine, next); // 查找sql中单引号的结束位置
                if (index == -1) { // 不是字符常量
                    buf.append(c);
                } else {
                    buf.append(currentLine.substring(i, index + 1));
                    i = index;
                }
                continue;
            }

            // 忽略字符串中的内容
            else if (c == '"') {
                int index = this.analysis.indexOfDoubleQuotation(currentLine, i);
                if (index == -1) {
                    buf.append(c);
                } else {
                    buf.append(currentLine.substring(i, index + 1));
                    i = index;
                }
                continue;
            }

            // 忽略命令替换语句中的内容
            else if (c == '`') {
                int index = this.analysis.indexOfAccent(currentLine, i);
                if (index == -1) {
                    buf.append(c);
                } else {
                    buf.append(currentLine.substring(i, index + 1));
                    i = index;
                }
                continue;
            }

            // 忽略脚本单行注释中的内容
            else if (c == '#') {
                buf.append(currentLine.substring(i));
                break;
            }

            // 忽略SQL单行注释中的内容
            else if (c == '-' && next < currentLine.length() && currentLine.charAt(next) == '-') {
                buf.append(currentLine.substring(i));
                break;
            }

            // 忽略SQL中的多行注释内容
            else if (c == '/' && next < currentLine.length() && currentLine.charAt(next) == '*') {
                int index = this.indexOfSQLComment(currentLine, next + 1);
                if (index == -1) {
                    buf.append(currentLine.substring(i)).append(in.getLineSeparator()); // 保存多行注释的第一行

                    // 读取多行注释的第二行
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        if ((index = this.indexOfSQLComment(line, 0)) == -1) {
                            buf.append(line).append(in.getLineSeparator()); // 保存多行注释的一行
                        } else {
                            int start = index + 1;
                            buf.append(line.substring(0, start)); // 保存多行注释
                            buf.append(this.readMultilineScript(in, line.substring(start)));
                            return buf;
                        }
                    }

                    throw new IOException(ResourcesUtils.getScriptStderrMessage(104, buf));
                } else {
                    buf.append(currentLine.substring(i, index + 1)); // 保存多行注释
                    i = index;
                    continue;
                }
            }

            // 语句分隔符
            else if (c == this.token) {
                // 如果最后一个字符正好是行末最后一个字符
                if (next >= currentLine.length()) {
                    return buf;
                }

                // 分隔符后如果是单行注释, 需要在语句中保留sql单行注释
                if (this.analysis.startsWith(currentLine, "--", next, true)) {
                    buf.append(currentLine.substring(next));
                    return buf;
                }

                // 分隔符后如果是多行注释，需要在语句中保留多行注释信息
                else if (this.analysis.startsWith(currentLine, "/*", next, true)) {
                    int index = this.indexOfSQLComment(currentLine, next + 1);
                    if (index == -1) {
                        buf.append(currentLine.substring(next)).append(in.getLineSeparator()); // 保存多行注释的一行

                        // 读取多行注释的第二行
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            if ((index = this.indexOfSQLComment(line, 0)) == -1) {
                                buf.append(line).append(in.getLineSeparator()); // 保存多行注释的一行
                            } else {
                                int start = index + 1;
                                buf.append(line.substring(0, start)); // 保存多行注释
                                in.setCurrentLine(line.substring(start));
                                return buf;
                            }
                        }

                        throw new IOException(ResourcesUtils.getScriptStderrMessage(104, buf));
                    } else {
                        buf.append(currentLine.substring(next));
                        return buf;
                    }
                }

                // 将分隔符后的内容保存到下一个语句中使用
                else {
                    in.setCurrentLine(currentLine.substring(next));
                    return buf;
                }
            }

            // 保存其他字符
            else {
                buf.append(c);
            }
        }
        buf.append(in.getLineSeparator()); // 保存换行符

        // 读取下一行直到语句分隔符为止
        String line = null;
        if ((line = in.readLine()) != null) {
            buf.append(this.readMultilineScript(in, line));
        }
        return buf;
    }

    /**
     * 在sql语句中搜索单引号的结束位置
     *
     * @param sql  SQL语句
     * @param from 搜索起始位置
     * @return
     */
    private int indexOfSQLQuotation(String sql, int from) {
        if (from < 0) {
            throw new IllegalArgumentException(String.valueOf(from));
        }

        for (int i = from; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'') {
                int next = i + 1;
                if (next < sql.length() && sql.charAt(next) == '\'') { // 连续2个单引号表示转义字符
                    i = next;
                    continue;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 在sql中搜索多行注释的结束位置
     *
     * @param sql  SQL语句
     * @param from 搜索起始位置
     * @return
     */
    private int indexOfSQLComment(String sql, int from) {
        if (from < 0) {
            throw new IllegalArgumentException(String.valueOf(from));
        }

        for (int i = from; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '*') {
                int next = i + 1;
                if (next < sql.length() && sql.charAt(next) == '/') {
                    i = next;
                    return next;
                }
            }
        }
        return -1;
    }

    public synchronized String readPieceofScript(String begin, String end) throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : this.readPieceofScript(this.in, line, begin, end).toString();
        } finally {
            this.cacheline = null;
        }
    }

    private StringBuilder readPieceofScript(CacheLineReader in, String currentLine, String beginStr, String endStr) throws IOException {
        beginStr = StringUtils.trimBlank(beginStr);
        endStr = StringUtils.trimBlank(endStr);

        if (beginStr.equalsIgnoreCase(endStr)) { // 开始标志与结束标志不能相等
            throw new UniversalScriptException(beginStr + " == " + endStr);
        }

        int[] value = null;
        String[] begArray = StringUtils.splitByBlank(beginStr); // 语句段落的起始词组
        Ensure.isTrue(begArray.length >= 1, (Object) begArray);

        String[] endArray = StringUtils.splitByBlank(StringUtils.trimBlank(endStr)); // 语句段落的结束词组
        Ensure.isTrue(endArray.length >= 1, (Object) endArray);

        char bc = beginStr.charAt(0); // 起始标志位字符串的第一个字符
        Ensure.isTrue(!StringUtils.inArray(bc, '\'', '"'), bc);

        char ec = endStr.charAt(0); // 结束标志为字符串的第一个字符
        Ensure.isTrue(!StringUtils.inArray(ec, '\'', '"'), ec);

        boolean exists = false;
        String line = currentLine;
        int count = 0; // 嵌套使用次数
        StringBuilder buf = new StringBuilder();
        do {
            for (int i = 0; i < line.length(); i++) {
                char c = Character.toLowerCase(line.charAt(i));

                // 忽略字符串常量
                if (c == '\'') {
                    int end = this.analysis.indexOfQuotation(line, i);
                    if (end != -1) {
                        i = end;
                    }
                    continue;
                }

                // 忽略字符串
                else if (c == '"') {
                    int end = this.analysis.indexOfDoubleQuotation(line, i);
                    if (end != -1) {
                        i = end;
                    }
                    continue;
                }

                int previous = i - 1; // 上一个字符位置
                if (begArray.length == 1) {
                    if (c == bc && this.analysis.startsWith(line, beginStr, i, false) && this.analysis.charAt(line, previous, 1) && this.analysis.charAt(line, i + beginStr.length(), 1)) {
                        exists = true;
                        ++count;
                        i += beginStr.length() - 1;
                        continue;
                    }
                } else {
                    if (c == bc && this.analysis.startsWith(line, begArray[0], i, false) && this.analysis.charAt(line, previous, 1) && (value = this.analysis.indexOf(line, begArray, i)) != null) {
                        exists = true;
                        ++count;
                        i += value[1] + begArray[begArray.length - 1].length() - 1; // 起始单词的最后一个字符位置
                        continue;
                    }
                }

                if (endArray.length == 1) { // 如果是以一个单词结束
                    int index = i + endStr.length();
                    if (c == ec && this.analysis.startsWith(line, endStr, i, false) && this.analysis.charAt(line, previous, 1) && this.analysis.charAt(line, index, 1)) {
                        if (--count == 0) {
                            buf.append(line.substring(0, index));
                            in.setCurrentLine(line.substring(index));
                            return buf;
                        }
                    }
                } else { // 如果是以多个单词结束
                    if (c == ec && this.analysis.startsWith(line, endArray[0], i, false) && (value = this.analysis.indexOf(line, endArray, i)) != null) {
                        int index = value[1] + endArray[endArray.length - 1].length(); // 最后一个单词的右端的第一个字符的位置
                        if (--count == 0) {
                            buf.append(line.substring(0, index));
                            in.setCurrentLine(line.substring(index));
                            return buf;
                        } else { // 嵌套循环
                            i = index - 1;
                            continue;
                        }
                    }
                }
            }

            buf.append(line).append(in.getLineSeparator());
        } while ((line = in.readLine()) != null);
        throw new IOException(ResourcesUtils.getScriptStderrMessage(89, buf, exists ? endStr : beginStr));
    }

    public synchronized String readSymmetryScript(String str) throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : this.readSymmetryScript(this.in, line, str).toString();
        } finally {
            this.cacheline = null;
        }
    }

    private String readSymmetryScript(CacheLineReader in, String currentLine, String flag) throws IOException {
        if (StringUtils.isBlank(flag)) {
            throw new IllegalArgumentException(flag);
        }

        flag = StringUtils.trimBlank(flag);
        if (StringUtils.inArray(flag, "\"", "'")) {
            throw new IllegalArgumentException(flag);
        }

        char fc = flag.charAt(0);
        int count = 0;
        for (int i = 0; i < currentLine.length(); i++) {
            char c = currentLine.charAt(i);

            // 字符常量
            if (c == '\'') {
                int end = this.analysis.indexOfQuotation(currentLine, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            // 字符变量
            else if (c == '"') {
                int end = this.analysis.indexOfDoubleQuotation(currentLine, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            int previous = i - 1; // 上一个字符的位置
            if (c == fc && this.analysis.startsWith(currentLine, flag, i, false) && this.analysis.charAt(currentLine, previous, 1) && this.analysis.charAt(currentLine, i + flag.length(), 1)) {
                int end = i + flag.length(); // 关键字中最后一个字符的位置
                if (++count == 2) {
                    in.setCurrentLine(currentLine.substring(end));
                    return currentLine.substring(0, end);
                }
                i = end - 1;
                continue;
            }
        }
        throw new IOException(ResourcesUtils.getScriptStderrMessage(89, currentLine, flag));
    }

    public synchronized String readSingleWord() throws IOException {
        try {
            String line = this.previewline();
            return line == null ? null : StringUtils.rtrimBlank(this.readSingleWord(this.in, line));
        } finally {
            this.cacheline = null;
        }
    }

    /**
     * 从字符串参数 currentLine 中读取第一个单词, 如果单词右侧存在语句分隔符
     *
     * @param in
     * @param currentLine
     * @return
     * @throws IOException
     */
    private StringBuilder readSingleWord(CacheLineReader in, String currentLine) throws IOException {
        int index = -1;
        for (int i = 0; i < currentLine.length(); i++) {
            char c = currentLine.charAt(i);

            if (Character.isWhitespace(c)) {
                index = i;

                // 搜索是否以语句分隔符结尾
                for (int j = i + 1; j < currentLine.length(); j++) {
                    char nc = currentLine.charAt(j);
                    if (nc == this.token) { // 语句分隔符
                        index = ++j;
                        break;
                    } else if (Character.isWhitespace(nc)) { // 空白字符
                        continue;
                    } else {
                        break;
                    }
                }
                break;
            }

            // 忽略字符串中的内容
            else if (c == '\'') {
                int end = this.analysis.indexOfQuotation(currentLine, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }

            // 忽略字符串中的内容
            else if (c == '"') {
                int end = this.analysis.indexOfDoubleQuotation(currentLine, i);
                if (end != -1) {
                    i = end;
                }
                continue;
            }
        }

        if (index == -1) {
            return new StringBuilder(currentLine);
        } else {
            String str = currentLine.substring(0, index);
            in.setCurrentLine(currentLine.substring(index));
            return new StringBuilder(str);
        }
    }

    /**
     * 关闭输入流
     */
    public void close() {
        IO.close(this.in);
        this.cacheline = null;
    }

}