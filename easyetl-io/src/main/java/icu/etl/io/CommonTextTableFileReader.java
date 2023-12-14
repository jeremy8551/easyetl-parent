package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icu.etl.collection.NatureRandomList;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 表格型文件的输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-02-22
 */
public class CommonTextTableFileReader implements TextTableFileReader, TextTableLine {
    private final static Log log = LogFactory.getLog(CommonTextTableFileReader.class);

    /** 表格数据文件 */
    protected TextTableFile file;

    /** 字符输入流 */
    protected CacheLineReader in;

    /** 字段个数 */
    protected int column;

    /** 当前读取文件的行号, 从 1 开始 */
    protected long lineNumber;

    /** 当前行内容 */
    protected String line;

    /** 当前行的字段集合 */
    protected NatureRandomList<String> fields;

    /** 缓冲行数 */
    protected int bufferSize;

    /** 文件读取监听器 */
    protected TextTableFileReaderListener listener;

    /** 字符串分段规则 */
    protected TableLineRuler rule;

    /** 输入流起始位置 */
    protected long startPointer;

    /**
     * 打开表格文件的输入流
     *
     * @param file       表格数据文件
     * @param bufferSize 缓冲区长度，单位：字符
     * @throws IOException
     */
    public CommonTextTableFileReader(TextTableFile file, int bufferSize) throws IOException {
        super();
        this.init(file, bufferSize);
        this.open(file);
    }

    /**
     * 打开表格文件的输入流
     *
     * @param file       表格数据文件
     * @param start      输入流的起始位置，从0开始
     * @param length     输入流读取的最大字节总数
     * @param readBuffer 输入流缓冲区长度，单位：字符
     * @throws IOException
     */
    public CommonTextTableFileReader(TextTableFile file, long start, long length, int readBuffer) throws IOException {
        super();
        this.init(file, readBuffer);

        RandomAccessStream stream = new RandomAccessStream(file.getFile(), start, length);
        this.startPointer = stream.getStartPointer();
        InputStreamReader reader = new InputStreamReader(stream, file.getCharsetName());
        this.open(reader);
    }

    /**
     * 初始化
     *
     * @param file       文件
     * @param bufferSize 缓冲区容量，单位字符
     */
    protected void init(TextTableFile file, int bufferSize) {
        this.file = Ensure.notNull(file);
        this.bufferSize = bufferSize;
        this.rule = file.getRuler();
        this.lineNumber = 0;
        this.line = null;
        this.startPointer = 0;
    }

    /**
     * 打开输入流
     *
     * @param file 文件
     * @throws IOException
     */
    protected void open(TextTableFile file) throws IOException {
        this.ensureClose();
        File txtfile = file.getFile();
        this.in = new CacheLineReader(txtfile, file.getCharsetName(), this.bufferSize);
        this.createFieldList();
    }

    /**
     * 打开输入流
     *
     * @param in 输入流
     * @throws IOException
     */
    protected void open(Reader in) throws IOException {
        this.ensureClose();
        this.in = new CacheLineReader(in, this.bufferSize);
        this.createFieldList();
    }

    /**
     * 创建一个字段集合
     *
     * @throws IOException
     */
    protected void createFieldList() throws IOException {
        this.column = this.file.getColumn();
        if (this.column <= 0) { // 自动计算每行字段个数
            this.column = this.calcColumn();
            this.file.setColumn(this.column);
        }
        this.fields = new NatureRandomList<String>(this.column == 0 ? 10 : this.column);
        this.lineNumber = 0; // 需要重制行数与当前行内容
        this.line = null;
    }

    public long getStartPointer() {
        return startPointer;
    }

    /**
     * 判断输入流是否已关闭
     *
     * @throws IOException
     */
    protected void ensureClose() throws IOException {
        if (this.in != null) {
            throw new IOException(ResourcesUtils.getIoxMessage(34));
        }
    }

    /**
     * 判断输入流是否已打开
     *
     * @throws IOException
     */
    protected void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException(ResourcesUtils.getIoxMessage(10, this.file.getAbsolutePath()));
        }
    }

    public boolean skip(long chars, long rows) throws IOException {
        this.ensureOpen();
        long[] value = this.in.skip(chars, rows);
        if (value == null) {
            return true;
        } else {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getIoxMessage(33, this.file.getAbsolutePath(), value[0], value[1]));
            }
            return false;
        }
    }

    public synchronized void close() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
        this.line = null;
        this.fields = null;
    }

    /**
     * 计算当前行的字段个数
     *
     * @return
     * @throws IOException
     */
    protected synchronized int calcColumn() throws IOException {
        this.ensureOpen();
        this.in.cacheLine(20);
        List<TextFileLine> lines = this.in.getCacheLines();
        int[] array = new int[lines.size()];
        Arrays.fill(array, 0);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).getContent();
            list.clear();
            this.rule.split(line, list);
            array[i] = list.size();
        }

        int column = (array.length == 0) ? 0 : Numbers.max(array);
        if (array.length > 0 && log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getIoxMessage(8, this.file.getAbsolutePath(), column));
        }
        return column;
    }

    public long getLineNumber() {
        return this.lineNumber;
    }

    public boolean isColumnBlank(int position) {
        return StringUtils.isBlank(this.fields.get(position));
    }

    public String getColumn(int position) {
        return this.fields.get(position);
    }

    public void setColumn(int position, String str) {
        this.fields.set(position, str);
        this.line = this.rule.replace(this, position, str);
    }

    public int getColumn() {
        return this.column;
    }

    public TextTableLine readLine() throws IOException {
        String line = this.in.readLine();
        if (line == null) {
            return null;
        } else {
            this.lineNumber++;
            this.splitLine(line);
            if (this.listener != null && this.listener.processLine(this.file, this, this.lineNumber)) {
                return this.readLine();
            } else {
                return this;
            }
        }
    }

    public TextTableLine readLine(int lineNumber) throws IOException {
        if (lineNumber < 0) { // 请求行数不合法
            throw new IOException(String.valueOf(lineNumber));
        } else if (lineNumber == 0) { // 请求行数等于0 表示重新打开输入流
            IO.closeQuietly(this);
            this.open(this.file);
            return null;
        } else if (lineNumber > this.lineNumber) { // 请求行大于当前行
            while (this.readLine() != null) {
                if (this.lineNumber == lineNumber) {
                    return this;
                }
            }
            return null;
        } else if (lineNumber < this.lineNumber) { // 请求行小于当前行
            IO.closeQuietly(this);
            this.open(this.file);
            while (this.readLine() != null) {
                if (this.lineNumber == lineNumber) {
                    return this;
                }
            }
            return null;
        } else { // 请求行等于当前行
            return this;
        }
    }

    public TextTableLine getLine() {
        return this.lineNumber == 0 ? null : this;
    }

    /**
     * 提取字符串参数 line 中的字段信息
     *
     * @param line 记录内容，不能为null
     * @throws IOException
     */
    protected void splitLine(String line) throws IOException {
        this.fields.clear();
        this.rule.split(line, this.fields);
        this.line = line;

        if (this.fields.size() == this.column) {
            return;
        } else if (this.fields.size() < this.column && this.mergeNextLine(line)) {
            return;
        } else if (this.listener == null || this.listener.processColumnException(this, line, this.lineNumber)) {
            throw new IOException(ResourcesUtils.getIoxMessage(12, this.file.getAbsolutePath(), this.in.getLineNumber(), this.fields.size(), this.column, this.line));
        }
    }

    /**
     * 合并下一行记录到当前行
     *
     * @param line 当前行内容
     * @return 返回 true 表示合并成功 返回 false 表示失败
     * @throws IOException
     */
    protected boolean mergeNextLine(String line) throws IOException {
        int count = 0;
        TextFileLine next = null; // next line
        long currentLineNumber = this.in.getLineNumber();
        String mergeStr = line + this.in.getLineSeparator();

        List<String> list = new ArrayList<String>(this.column);
        while (list.size() < this.column) {
            if (next != null) {
                mergeStr += next.getLineSeparator(); // add line separator
            }

            ArrayList<TextFileLine> cache = this.in.getCacheLines(); // 缓存一行
            if (cache.size() == count) {
                this.in.cacheLine(count + 1);
                cache = this.in.getCacheLines();
            }

            if (cache.size() <= count || (next = cache.get(count++)) == null) {
                return false;
            }

            mergeStr += next.getContent(); // add line
            list.clear();
            this.rule.split(mergeStr, list);
        }

        if (list.size() == this.column) {
            for (int i = count; i > 0; i--) {
                this.in.readLine(); // 读取缓冲中的行
            }

            // 将字段添加到当前行
            this.fields.clear();
            this.fields.addAll(list);
            this.line = mergeStr;

            // 处理存在回车换行符
            if (this.listener != null) {
                this.listener.processLineSeparator(this.file, this, currentLineNumber);
            }
            return true;
        } else {
            return false;
        }
    }

    public String getContent() {
        return this.line;
    }

    public void setContext(String line) {
        this.line = line;
    }

    public String getLineSeparator() {
        return this.in.getLineSeparator();
    }

    public TextTableFile getTable() {
        return this.file;
    }

    public TextTableFileReaderListener getListener() {
        return listener;
    }

    public void setListener(TextTableFileReaderListener listener) {
        this.listener = listener;
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().getName().equals(this.getClass().getName())) {
            return false;
        } else {
            return this.file.equals(((CommonTextTableFileReader) obj).file);
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[file=" + file + ", column=" + this.column + ", lineNumber=" + this.lineNumber + "]";
    }

}
