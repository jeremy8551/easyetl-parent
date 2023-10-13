package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.BeanFactory;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 表格型文本文件接口的实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-02-22
 */
@EasyBeanClass(kind = "txt", mode = "file", major = "", minor = "", description = "文本文件, 逗号分隔，无转义字符，无字符串限定符", type = TextTableFile.class)
public class CommonTextTableFile implements TextTableFile {

    /** 表格数据文件 */
    protected File file;

    /** 字段分隔符 */
    protected String separator;

    /** 转义字符 */
    protected char escapeChar;

    /** true 表示存在转义字符 */
    protected boolean existsEscape;

    /** 每行的字段个数 */
    protected int column;

    /** 数据文件的字符集 */
    protected String charsetName;

    /** 字符串二端的限定符 */
    protected String charDelimiter;

    /** 行间分隔符 */
    protected String lineSeparator;

    /** 列名集合 */
    protected List<String> columnNames;

    /**
     * 初始化
     */
    public CommonTextTableFile() {
        super();
        this.column = 0;
        this.separator = ",";
        this.charDelimiter = "";
        this.lineSeparator = FileUtils.lineSeparator;
        this.existsEscape = false;
        this.escapeChar = 0;
        this.charsetName = StringUtils.CHARSET;
        this.columnNames = new ArrayList<String>();
    }

    public String getColumnName(int position) {
        return position >= this.columnNames.size() ? null : this.columnNames.get(position);
    }

    public void setColumnName(int position, String name) {
        while (this.columnNames.size() <= position) {
            this.columnNames.add("");
        }
        this.columnNames.set(position, name);
    }

    public String getCharsetName() {
        return this.charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getDelimiter() {
        return this.separator;
    }

    public void setDelimiter(String delimiter) {
        this.separator = delimiter;
    }

    public void setEscape(char c) {
        this.escapeChar = c;
        this.existsEscape = true;
    }

    public char getEscape() {
        return this.escapeChar;
    }

    public void removeEscape() {
        this.existsEscape = false;
    }

    public boolean existsEscape() {
        return this.existsEscape;
    }

    public void setAbsolutePath(String filepath) {
        this.file = StringUtils.isBlank(filepath) ? null : new File(filepath);
    }

    public String getAbsolutePath() {
        return this.file == null ? null : this.file.getAbsolutePath();
    }

    public File getFile() {
        return this.file;
    }

    public boolean delete() {
        return FileUtils.deleteFile(this.getFile());
    }

    public String getLineSeparator() {
        return this.lineSeparator;
    }

    public void setLineSeparator(String str) {
        if (str == null) {
            throw new NullPointerException();
        } else {
            this.lineSeparator = str;
        }
    }

    public String getCharDelimiter() {
        return this.charDelimiter;
    }

    public void setCharDelimiter(String str) {
        this.charDelimiter = str == null ? "" : str;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return this.column;
    }

    public TableLineRuler getRuler() {
        return BeanFactory.get(TableLineRuler.class, this);
    }

    public TextTableFileReader getReader(int cache) throws IOException {
        return new CommonTextTableFileReader(this, cache);
    }

    public TextTableFileReader getReader(long start, long length, int readBuffer) throws IOException {
        return new CommonTextTableFileReader(this, start, length, readBuffer);
    }

    public TextTableFileWriter getWriter(boolean append, int cache) throws IOException {
        return new CommonTextTableFileWriter(this, append, cache);
    }

    public TextTableFileWriter getWriter(Writer writer, int cache) throws IOException {
        return new CommonTextTableFileWriter(this, writer, cache);
    }

    public int countColumn() throws IOException {
        this.setColumn(0);
        IO.close(this.getReader(IO.FILE_BYTES_BUFFER_SIZE));
        return this.getColumn();
    }

    public CommonTextTableFile clone() {
        CommonTextTableFile obj = new CommonTextTableFile();
        this.clone(obj);
        return obj;
    }

    /**
     * 复制所有属性到参数 obj 中
     *
     * @param obj
     */
    public void clone(TextTableFile obj) {
        obj.setAbsolutePath(this.getAbsolutePath());
        obj.setCharsetName(this.getCharsetName());
        obj.setColumn(this.getColumn());
        obj.setDelimiter(this.getDelimiter());
        obj.setLineSeparator(this.getLineSeparator());
        obj.setCharDelimiter(this.getCharDelimiter());

        if (this.existsEscape()) {
            obj.setEscape(this.getEscape());
        } else {
            obj.removeEscape();
        }

        // 复制列名
        for (int i = 0; i < this.columnNames.size(); i++) {
            obj.setColumnName(i + 1, this.columnNames.get(i));
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{file: " + this.file + ", column: " + this.column + ", charsetName: " + this.charsetName + ", separator: " + this.separator + "}";
    }

    public boolean equals(Object obj) {
        return obj != null //
                && obj.getClass().getName().equals(this.getClass().getName()) // 类名相同
                && this.file == null ? //
                ((TextTableFile) obj).getFile() == null //
                : //
                this.file.equals(((TextTableFile) obj).getFile()) // 文件相同
                ;
    }

    public boolean equalsStyle(TextTable obj) {
        if (!this.getLineSeparator().equals(obj.getLineSeparator())) {
            return false;
        }

        if (!this.getCharDelimiter().equals(obj.getCharDelimiter())) {
            return false;
        }

        if (!this.getCharsetName().equals(obj.getCharsetName())) {
            return false;
        }

        if (this.getColumn() != obj.getColumn()) {
            return false;
        }

        if (!this.getDelimiter().equals(obj.getDelimiter())) {
            return false;
        }

        if (this.getEscape() != obj.getEscape()) {
            return false;
        }

        return true;
    }

}
