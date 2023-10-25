package icu.etl.database.export.inernal;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.annotation.EasyBean;
import icu.etl.database.SQL;
import icu.etl.database.export.ExtractMessage;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.ExtracterContext;
import icu.etl.io.TableLine;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

@EasyBean(kind = "local", mode = "filepath", major = "", minor = "", description = "卸载数据到本地文件")
public class ExtractFileWriter implements ExtractWriter, EasyetlContextAware {

    /** 文件路径 */
    private String filepath;

    /** 文件名的序号 */
    private int fileNumber;

    /** 最大记录数 */
    private long maximum;

    /** 计数器 */
    private long count;

    /** true 表示重新写入 */
    private boolean rewrite;

    /** 文件输出流 */
    private TextTableFileWriter writer;

    /** 上下文信息 */
    private ExtracterContext context;

    /** 写入行数 */
    protected long lineNumber;

    /** 写入总字节数 */
    private long bytes;

    /** 标题输入流 */
    private TableTitle title;

    /** 消息信息 */
    private ExtractMessage message;

    protected EasyetlContext ioccxt;

    public void setContext(EasyetlContext context) {
        this.ioccxt = context;
    }

    /**
     * 初始化
     *
     * @param context
     * @param message
     * @throws IOException
     * @throws SQLException
     */
    public ExtractFileWriter(ExtracterContext context, ExtractMessage message) throws IOException, SQLException {
        if (context == null) {
            throw new NullPointerException();
        }
        if (message == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.message = message;
        this.filepath = context.getTarget();
        this.maximum = context.getMaximum();
        this.rewrite = this.maximum > 0;
        this.fileNumber = -1;
        this.lineNumber = 0;
        this.count = 0;
        this.open();
    }

    /**
     * 打开文件输出流
     *
     * @throws IOException
     * @throws SQLException
     */
    protected void open() throws IOException, SQLException {
        this.closeWriter();
        File file = this.createNewfile();

        // 保存文件路径
        String target = this.message.getTarget();
        if (StringUtils.isNotBlank(target)) {
            target += ", ";
        }
        target += file.getAbsolutePath();
        this.message.setTarget(target);

        // 打开文件输出流
        TextTableFile template = (TextTableFile) this.context.getFormat();
        TextTableFile table = template.clone();
        table.setAbsolutePath(file.getAbsolutePath());
        this.writer = table.getWriter(this.context.isAppend(), this.context.getCacheLines());

        // 写入列标题信息
        if (this.context.isTitle()) {
            if (this.title == null) {
                this.title = new TableTitle(this.context, this.ioccxt);
            }
            this.write(this.title);
        }
    }

    /**
     * 创建文件
     *
     * @return
     */
    protected File createNewfile() {
        this.fileNumber++;
        int begin = this.filepath.indexOf('{');
        if (begin != -1) {
            int end = SQL.indexOfBrace(this.filepath, begin);
            if (end == -1) {
                throw new IllegalArgumentException(this.filepath);
            }

            int size = end - begin + 1;
            String filepath = StringUtils.replace(this.filepath, begin, size, (this.fileNumber == 0 ? "" : String.valueOf(this.fileNumber)));
            return new File(filepath);
        } else {
            File file = new File(this.filepath);
            String suffix = FileUtils.getFilenameSuffix(file.getName());
            String filename = FileUtils.getFilenameNoSuffix(file.getName());
            String filepath = filename + (this.fileNumber == 0 ? "" : "-" + this.fileNumber) + "." + suffix;
            return new File(file.getParentFile(), filepath);
        }
    }

    public void write(TableLine line) throws IOException {
        this.count++;
        this.lineNumber++;
        this.writer.addLine(line);
    }

    public boolean rewrite() throws IOException, SQLException {
        if (this.rewrite && this.count >= this.maximum) { // 已达最大记录
            this.count = 0;
            this.open();
            return true;
        } else {
            return false;
        }
    }

    public void flush() throws IOException {
        this.writer.flush();
    }

    public void close() throws IOException {
        this.closeWriter();
        this.message.setRows(this.lineNumber);
        this.message.setBytes(this.bytes);
    }

    /**
     * 关闭文件输出流
     *
     * @throws IOException
     */
    protected void closeWriter() throws IOException {
        if (this.writer != null) {
            this.writer.flush();
            this.bytes += this.writer.getTable().getFile().length();
            this.writer.close();
            this.writer = null;
        }
    }

}
