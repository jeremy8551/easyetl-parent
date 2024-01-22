package icu.etl.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class CommonTextTableFileWriter implements TextTableFileWriter {

    /** 表格型数据文件 */
    protected TextTableFile table;

    /** 文件输出流 */
    protected BufferedLineWriter out;

    /** 字段间的分隔符 */
    protected String coldel;

    /** 换行符 */
    protected String lineSeparator;

    protected TableLineRuler rule;

    /**
     * 创建一个表格型文件的输出流
     *
     * @param file   表格型文件
     * @param append true表示追加写入方式
     * @param cache  缓存行数
     * @throws IOException 打开输出流错误
     */
    public CommonTextTableFileWriter(TextTableFile file, boolean append, int cache) throws IOException {
        this(file, new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(), append), file.getCharsetName()), cache);
    }

    /**
     * 创建一个表格型的输出流
     *
     * @param file  表格型文件
     * @param out   输出流
     * @param cache 缓存行数
     * @throws IOException 打开输出流错误
     */
    public CommonTextTableFileWriter(TextTableFile file, Writer out, int cache) throws IOException {
        super();
        this.table = file;
        this.out = new BufferedLineWriter(out, cache);
        this.coldel = file.getDelimiter();
        this.lineSeparator = file.getLineSeparator();
        this.rule = this.table.getRuler();
    }

    public TextTableFile getTable() {
        return this.table;
    }

    public synchronized void addLine(String line) throws IOException {
        this.out.writeLine(line, this.lineSeparator);
    }

    public synchronized void addLine(TableLine line) throws IOException {
        this.out.writeLine(this.rule.join(line), this.lineSeparator);
    }

    public synchronized void flush() throws IOException {
        this.out.flush();
    }

    public synchronized void close() throws IOException {
        if (this.out != null) {
            this.out.close();
            this.out = null;
        }
    }

}
