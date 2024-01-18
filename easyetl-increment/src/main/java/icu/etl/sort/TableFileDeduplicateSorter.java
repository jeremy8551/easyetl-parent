package icu.etl.sort;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.AbstractJob;
import icu.etl.concurrent.EasyJobReader;
import icu.etl.concurrent.EasyJobReaderImpl;
import icu.etl.concurrent.EasyJobService;
import icu.etl.concurrent.Terminate;
import icu.etl.concurrent.Terminates;
import icu.etl.expression.Analysis;
import icu.etl.expression.BaseAnalysis;
import icu.etl.io.BufferedLineWriter;
import icu.etl.io.TableLine;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileCounter;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableFileWriter;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 表格文件排序
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-01-26
 */
public class TableFileDeduplicateSorter implements Terminate {
    private final static Log log = LogFactory.getLog(TableFileDeduplicateSorter.class);

    private final static String TEMPFILE_CREATOR = "TEMPFILE_CREATOR";

    private final static String FILESIZE_MORE = "FILE_MORE";

    private final static String FILELINE_NUMBER = "FILELINE_NUMBER";

    /** 上下文信息 */
    private TableFileSortContext context;

    /** true 表示终止排序操作 */
    private volatile boolean terminate;

    /** 观察者, 在终止剥离增量时，通知所有观察者执行终止操作 */
    protected Terminates observers;

    /** 表格型记录排序规则 */
    private RecordComparator recordComparator;

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public TableFileDeduplicateSorter(TableFileSortContext context) {
        super();
        this.context = Ensure.notNull(context);
        this.terminate = false;
        this.observers = new Terminates();
    }

    /**
     * 返回排序配置信息
     *
     * @return 上下文信息
     */
    public TableFileSortContext getContext() {
        return context;
    }

    public void terminate() {
        this.terminate = true;
        this.observers.terminate();
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * 判断是否已被中断了
     *
     * @throws IOException 已被中断
     */
    private void terminated() throws IOException {
        if (this.terminate) { // 已被终止
            TextTableFile file = this.context.getFile();
            throw new IOException(ResourcesUtils.getCommonMessage(6, "sort " + file));
        }
    }

    /**
     * 对文件中的行按设置字段进行排序
     *
     * @param context 容器上下文信息
     * @param file    数据文件
     * @param orders  排序字段表达式数组
     * @return 排序后的文件
     * @throws Exception 发生错误
     */
    public synchronized File execute(EasyContext context, TextTableFile file, String... orders) throws Exception {
        Analysis analysis = new BaseAnalysis();
        OrderByExpression[] array = new OrderByExpression[orders.length];
        for (int i = 0; i < orders.length; i++) {
            array[i] = new OrderByExpression(context, analysis, orders[i]);
        }
        return this.execute(file, array);
    }

    /**
     * 对文件中的行按设置字段进行排序
     *
     * @param file   数据文件
     * @param orders 排序字段表达式数组
     * @return 排序后的文件
     * @throws Exception 发生错误
     */
    public synchronized File execute(TextTableFile file, OrderByExpression... orders) throws Exception {
        Ensure.notNull(file);
        Ensure.notEmpty(orders);
        FileUtils.assertFile(file.getFile());

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("io.standard.output.msg056", file.getAbsolutePath(), Arrays.toString(orders)));
        }

        this.context.setFile(file);
        TempFileCreator creator = new TempFileCreator(this.context.getTempDir(), file.getFile());
        this.context.setAttribute(TEMPFILE_CREATOR, creator); // 临时文件工厂

        // 设置排序规则
        this.recordComparator = new RecordComparator(orders.length);
        for (OrderByExpression expr : orders) {
            this.recordComparator.add(expr.getPosition(), expr.getComparator(), expr.isAsc());
        }

        try {
            return this.execute(file, creator);
        } finally {
            if (this.context.isDeleteFile()) {
                creator.deleteTempfiles(); // 删除临时文件
            }
        }
    }

    /**
     * 排序文件
     *
     * @param file    文件
     * @param creator 临时文件工厂
     * @return 排序后文件
     * @throws Exception 发生错误
     */
    protected File execute(TextTableFile file, TempFileCreator creator) throws Exception {
        TimeWatch watch = new TimeWatch();

        // 排序前文件信息
        File oldfile = file.getFile();
        long oldlength = oldfile.length();
        if (oldfile.exists() && oldlength == 0) {
            return oldfile;
        }

        this.terminated();
        File listfile = this.divide(file); // 将大文件分成多个小文件，并返回清单文件
        this.terminated();

        File mergefile = this.merge(listfile, file.getCharsetName()); // 合并清单文件中记录的临时文件
        this.terminated();

        // 是否自动删除右端的行号字段
        if (this.context.isRemoveLastField()) {
            File removefile = this.removeLineNumber(file, mergefile);
            FileUtils.deleteFile(mergefile);
            mergefile = removefile;
        }

        // 判断移动前后文件大小是否有变化
        long mergefilelength = mergefile.length() - (this.context.isRemoveLastField() ? 0L : (Long) this.context.getAttribute(FILESIZE_MORE));
        if (!mergefile.exists() || mergefilelength != oldlength) {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg015", mergefile.getAbsolutePath(), mergefilelength, oldfile.getAbsolutePath(), oldlength));
        }

        // 判断合并前与合并后文件记录数是否相等
        if (this.context.getReadLineNumber() != this.context.getMergeLineNumber()) {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg036", file.getAbsolutePath(), this.context.getReadLineNumber(), this.context.getMergeLineNumber()));
        }

        // 保留源文件时，直接返回排序后的文件
        if (this.context.keepSource()) {
            File newfile = creator.toSortfile();

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("io.standard.output.msg053", oldfile.getAbsolutePath(), newfile.getAbsolutePath(), watch.useTime()));
            }

            if (mergefile.renameTo(newfile)) {
                return newfile;
            } else {
                throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg035", mergefile.getAbsolutePath(), newfile.getAbsolutePath()));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("io.standard.output.msg054", oldfile.getAbsolutePath(), mergefile.getAbsolutePath(), oldfile.getAbsolutePath(), watch.useTime()));
            }

            // 不保留源文件的处理逻辑:
            // 1.将原文件（oldfile）重命名为一个带 bak 扩展名的文件
            // 2.将排序后文件（mergefile）重命名为原文件名（oldfile）
            // 3.再删除带 bak 扩展名的文件
            File bakfile = creator.toBakfile();
            if (FileUtils.rename(mergefile, oldfile, bakfile) && bakfile.delete()) { // 删除备份文件
                return oldfile;
            } else {
                throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg016", bakfile.getAbsolutePath()));
            }
        }
    }

    /**
     * 删除表格文件的最右侧的一列
     *
     * @param file     表格文件
     * @param sortfile 排序后文件
     * @return 删除列后的文件
     * @throws IOException 访问文件错误
     */
    private File removeLineNumber(TextTableFile file, File sortfile) throws IOException {
        File mergefile = FileUtils.createNewFile(sortfile.getParentFile(), sortfile.getName());
        TextTableFile clone = file.clone();
        clone.setAbsolutePath(mergefile.getAbsolutePath());

        TextTableFile clonefile = file.clone();
        clonefile.setAbsolutePath(sortfile.getAbsolutePath());
        TextTableFileReader in = clonefile.getReader(this.context.getReaderBuffer());
        try {
            TextTableFileWriter out = clone.getWriter(false, this.context.getWriterBuffer());
            try {
                TextTableLine line;
                while ((line = in.readLine()) != null) {
                    String content = line.getContent();
                    String delimiter = clonefile.getDelimiter();
                    int end = Ensure.isFromZero(content.lastIndexOf(delimiter));
                    String str = content.substring(0, end);
                    out.addLine(str); // 截取字符串，删除最后一个字段（行号）
                }
                out.flush();
                return mergefile;
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * 将大文件中内容分批写入小文件中，同时对小文件内容进行排序, 将小文件绝对路径写入清单文件
     *
     * @param file 待排序的表格文件
     * @return 临时文件的清单文件(清单文件内容是所有临时文件的绝对路径)
     * @throws IOException 划分文件发生错误
     */
    private File divide(TextTableFile file) throws IOException {
        TextTableFileReader in = file.getReader(this.context.getReaderBuffer());
        try {
            TextTableLine line = in.readLine();
            String lineSeparator = FileUtils.lineSeparator;
            if (line != null && line.getLineSeparator() != null && line.getLineSeparator().length() > 0) {
                lineSeparator = line.getLineSeparator();
            }

            TempFileCreator creator = this.context.getAttribute(TEMPFILE_CREATOR);
            File listfile = creator.toListfile(); // 清单文件：记录小文件的绝对路径
            TempFileWriter out = new TempFileWriter(this.context, file, creator, listfile, lineSeparator, this.recordComparator);
            try {
                while (line != null) {
                    if (this.terminate) {
                        break;
                    } else {
                        out.writeRecord(new FileRecord(line, line.getLineNumber()));
                        line = in.readLine();
                    }
                }
                out.flush();
                return listfile;
            } finally {
                out.close();
            }
        } finally {
            in.close();
            this.calc(file, in.getLineNumber());
        }
    }

    /**
     * 统计每行右侧增加的新列，所占用的字节数总数
     *
     * @param file       文件
     * @param lineNumber 表格型文件总行数
     * @throws UnsupportedEncodingException 计算总字节数发生错误
     */
    private void calc(TextTableFile file, long lineNumber) throws UnsupportedEncodingException {
        long bytes = 0; // 在每行右侧添加的列内容占用的字节总数
        String charsetName = file.getCharsetName();
        String delimiter = file.getDelimiter();
        StringBuilder buf = new StringBuilder(1000);
        for (long i = 1, cout = 0; i <= lineNumber; i++) {
            buf.append(delimiter).append(String.valueOf(i));
            if (++cout >= 100) {
                bytes += buf.toString().getBytes(charsetName).length;
                buf.setLength(0);
                cout = 0;
            }
        }
        bytes += buf.toString().getBytes(charsetName).length;

        this.context.setAttribute(FILESIZE_MORE, bytes);
        this.context.setReadLineNumber(lineNumber);
        file.setColumn(file.getColumn() + 1); // 因为每行右侧都增加了一个新列：行号，所以需要将列数加一
    }

    /**
     * 合并清单文件中的临时文件
     *
     * @param listfile    清单文件(存储临时文件的绝对路径)
     * @param charsetName 清单文件的字符集
     * @return 合并后的文件
     * @throws IOException 合并临时文件发生错误
     */
    private File merge(File listfile, String charsetName) throws Exception {
        long number = FileUtils.count(listfile, charsetName);
        if (number == 0) {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg018", listfile.getAbsolutePath()));
        }

        // 清单文件中只有一个文件路径时
        if (number == 1) {
            File newfile = new File(StringUtils.trimBlank(FileUtils.readline(listfile, charsetName, 0)));
            FileUtils.assertFile(newfile);
            this.deleteListFile(listfile);
            Long mergeLines = this.context.getAttribute(FILELINE_NUMBER); // 最后一次合并的文件记录数作为最终值
            if (mergeLines == null) {
                this.context.setMergeLineNumber(new TextTableFileCounter(this.context.getThreadSource(), this.context.getThreadNumber()).execute(newfile, charsetName));
            } else {
                this.context.setMergeLineNumber(mergeLines);
            }
            return newfile;
        }

        // 合并清单文件中记录的文件
        else {
            TempFileCreator creator = this.context.getAttribute(TEMPFILE_CREATOR);
            File newlistfile = creator.toListfile(); // 合并后的清单文件
            ListfileWriter out = new ListfileWriter(newlistfile, charsetName);
            try {
                this.merge(listfile, out);
                out.flush();
            } finally {
                out.close();
                this.deleteListFile(listfile); // 合并完清单文件后，需要删除清单文件
            }

            return this.merge(newlistfile, charsetName); // 递归调用，继续合并新清单文件中的临时文件
        }
    }

    /**
     * 合并清单文件 {@code listfile} 中的临时文件, 并将合并后结果（也就是新产生的临时文件路径）写入到 {@code listfileout} 输出流中
     *
     * @param listfile 清单文件
     * @param out      清单文件输出流
     * @throws IOException 访问文件错误
     */
    protected void merge(File listfile, ListfileWriter out) throws Exception {
        MergeJobReader in = new MergeJobReader(this.context, listfile, out, this.recordComparator);
        try {
            if (this.context.getThreadNumber() <= 1) { // 串行执行
                while (in.hasNext() && !this.terminate) {
                    MergeJob job = in.next();
                    try {
                        this.observers.add(job);
                        job.execute();
                    } finally {
                        this.observers.remove(job);
                    }
                }
            } else { // 使用线程池并行执行
                EasyJobService service = this.context.getThreadSource().getJobService(this.context.getThreadNumber());
                try {
                    this.observers.add(service);
                    service.execute(new EasyJobReaderImpl(in));
                } finally {
                    this.observers.remove(service);
                }
            }
        } finally {
            in.close();
        }
    }

    /**
     * 删除清单文件
     *
     * @param listfile 清单文件
     */
    private void deleteListFile(File listfile) {
        FileUtils.delete(listfile, 5, 200);
    }

    protected static class RecordComparator implements Comparator<TableLine> {

        /** 字段位置的数组，元素都是位置信息，从1开始 */
        private int[] positions;

        /** 字符串比较方法 */
        private Comparator<String>[] comparators;

        /** 容量，从0开始 */
        private int count;

        /**
         * 初始化
         */
        public RecordComparator(int size) {
            Ensure.isFromZero(size);
            this.positions = new int[size];
            this.comparators = new Comparator[size];
            this.count = 0;
        }

        /**
         * 添加排序字段
         *
         * @param position   字段位置信息，从1开始
         * @param comparator 排序规则
         * @param asc        true：正向 false：反向排序
         */
        public void add(int position, Comparator<String> comparator, boolean asc) {
            this.positions[this.count] = position;
            this.comparators[this.count] = asc ? comparator : new ReverseComparator(comparator);
            this.count++;
        }

        public int compare(TableLine record1, TableLine record2) {
            for (int i = 0; i < this.count; i++) {
                int position = this.positions[i];
                String col1 = record1.getColumn(position);
                String col2 = record2.getColumn(position);
                int v = this.comparators[i].compare(col1, col2);
                if (v != 0) {
                    return v;
                }
            }
            return 0;
        }

    }

    /**
     * 反转排序规则
     */
    protected static class ReverseComparator implements Comparator<String> {
        private Comparator<String> comparator;

        public ReverseComparator(Comparator<String> comp) {
            this.comparator = comp;
        }

        public int compare(String o1, String o2) {
            return this.comparator.compare(o2, o1);
        }
    }

    /**
     * 合并文件任务信息输入类
     */
    protected static class MergeJobReader implements EasyJobReader {

        /** 排序组件 */
        private final TableFileSortContext context;

        /** 当前合并任务对象 */
        private MergeJob task;

        /** 清单文件的输入流 */
        private BufferedReader in;

        /** 新清单文件的IO流 */
        private ListfileWriter listfileout;

        /** true 表示已终止 */
        private volatile boolean terminate;

        /** 记录排序规则 */
        private RecordComparator recordComparator;

        /**
         * 初始化
         *
         * @param context  待排序文件
         * @param listfile 清单文件
         * @param out      清单文件输出流（用于存储合并后产生的临时文件绝对路径）
         * @param c        记录排序规则
         * @throws IOException 访问文件错误
         */
        public MergeJobReader(TableFileSortContext context, File listfile, ListfileWriter out, RecordComparator c) throws IOException {
            this.context = Ensure.notNull(context);
            FileUtils.assertExists(listfile);
            this.in = IO.getBufferedReader(listfile, this.context.getFile().getCharsetName());
            this.listfileout = Ensure.notNull(out);
            this.recordComparator = Ensure.notNull(c);
        }

        public synchronized boolean hasNext() throws IOException {
            TextTableFile template = this.context.getFile();
            List<TextTableFile> list = new ArrayList<TextTableFile>();
            String filepath;
            for (int i = 1; i <= this.context.getFileCount() && (filepath = this.in.readLine()) != null; i++) {
                if (StringUtils.isNotBlank(filepath)) {
                    TextTableFile file = template.clone();
                    file.setAbsolutePath(StringUtils.trimBlank(filepath));
                    list.add(file);
                }
            }

            if (list.size() == 0) {
                return false;
            } else {
                this.task = new MergeJob(this.context, this, this.listfileout, this.recordComparator, list);
                return true;
            }
        }

        public synchronized MergeJob next() {
            return this.task;
        }

        public synchronized void close() throws IOException {
            if (this.in != null) {
                this.in.close();
                this.in = null;
            }
            this.task = null;
        }

        public void terminate() {
            this.terminate = true;
        }

        public boolean isTerminate() {
            return terminate;
        }

        /**
         * 添加合并行数
         *
         * @param lines 任务合并的行数
         */
        public void addLineNumbers(long lines) {
            synchronized (this.context) {
                this.context.setAttribute(FILELINE_NUMBER, lines);
            }
        }
    }

    /**
     * 将一个大文件切分成若干小文件
     */
    protected static class TempFileWriter implements Closeable, Flushable {

        /** 缓冲区数组 */
        private TextTableLine[] array;

        /** 缓冲区实际容量 */
        private int size;

        /** 缓冲区数组的容量 */
        private int capacity;

        /** 清单文件输出流 */
        private BufferedLineWriter listfileout;

        /** 表格型记录排序规则 */
        private RecordComparator recordComparator;

        /** 表格型文件中的字段分隔符 */
        private String delimiter;

        /** 写文件时的缓存行数 */
        private int writeBuffer;

        /** 临时文件工厂 */
        private TempFileCreator creator;

        /** 换行符 */
        private String lineSeparator;

        /** 数据文件 */
        private TextTableFile file;

        /** 排序上下文信息 */
        private TableFileSortContext context;

        /** 是否检查重复 */
        private boolean duplicate;

        public TempFileWriter(TableFileSortContext context, TextTableFile file, TempFileCreator creator, File listfile, String lineSeparator, RecordComparator recordComparator) throws IOException {
            this.size = 0;
            this.context = Ensure.notNull(context);
            this.file = Ensure.notNull(file);
            this.creator = Ensure.notNull(creator);
            this.listfileout = new BufferedLineWriter(listfile, file.getCharsetName(), 1);
            this.capacity = context.getMaxRows();
            this.array = new TextTableLine[this.capacity];
            this.delimiter = file.getDelimiter();
            this.writeBuffer = context.getWriterBuffer();
            this.recordComparator = recordComparator;
            this.lineSeparator = lineSeparator;
            this.duplicate = context.isDuplicate();
        }

        /**
         * 将记录写入到临时文件中
         *
         * @param record 数据文件
         * @throws IOException 写入文件发生错误
         */
        public void writeRecord(TextTableLine record) throws IOException {
            this.array[this.size++] = record;
            if (this.size == this.capacity) {
                this.flush();
            }
        }

        public void flush() throws IOException {
            if (this.size > 0) {
                Arrays.sort(this.array, 0, this.size, this.recordComparator); // 排序
                if (this.duplicate) {
                    this.checkRepeat(); // 检查记录中是否有索引重复的数据
                }

                File file = this.creator.toTempFile(); // 临时文件
                try {
                    this.writeFile(file); // 写入临时文件
                } finally {
                    if (file.exists()) {
                        this.listfileout.writeLine(file.getAbsolutePath()); // 将临时文件绝对路径写入到清单文件
                        this.listfileout.flush();
                    }
                }
                this.size = 0;
            }
        }

        /**
         * 检查记录中是否有索引重复的数据
         *
         * @throws IOException 文件访问错误
         */
        protected void checkRepeat() throws IOException {
            TextTableLine last = this.array[0]; // 上一行的位置
            for (int i = 1; i < this.size; i++) {
                TextTableLine line = this.array[i];
                int v = this.recordComparator.compare(last, line);
                if (v == 0) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg029", this.file.getAbsolutePath(), last.getLineNumber(), line.getLineNumber()));
                } else {
                    last = line;
                }
            }
        }

        /**
         * 把缓存记录写入到临时文件中
         *
         * @param file 临时文件
         * @throws IOException IO错误
         */
        protected void writeFile(File file) throws IOException {
            OutputStreamWriter out = IO.getFileWriter(file, this.listfileout.getCharsetName(), false);
            try {
                for (int i = 0; i < this.size; i++) {
                    TextTableLine line = this.array[i];
                    out.write(line.getContent());
                    out.write(this.delimiter);
                    out.write(String.valueOf(line.getLineNumber())); // 因为在临时文件的每行右侧，增加了一列用来记录所属的行号
                    out.write(this.lineSeparator);
                    if (i % this.writeBuffer == 0) {
                        out.flush();
                    }
                }
                out.flush();
            } finally {
                out.close();
            }
        }

        public void close() throws IOException {
            try {
                this.flush();
            } finally {
                this.listfileout.close();
                this.array = null;
                this.size = 0;
            }
        }
    }

    /**
     * 合并数据文件
     */
    protected static class MergeJob extends AbstractJob {

        /** 合并任务容器 */
        private MergeJobReader reader;

        /** 待合并数据文件 */
        private List<TextTableFile> files;

        /** 文件清单的输出流 */
        private ListfileWriter out;

        /** 排序规则 */
        private Comparator<TableLine> comp;

        /** 排序上下文信息 */
        private TableFileSortContext context;

        /**
         * 初始化
         *
         * @param context          剥离增量任务上下文信息
         * @param in               任务输入流
         * @param out              文件清单的输出流（将产生的临时文件绝对路径写入到输出流中）
         * @param recordComparator 记录排序规则
         * @param list             记录排序规则
         */
        public MergeJob(TableFileSortContext context, MergeJobReader in, ListfileWriter out, RecordComparator recordComparator, List<TextTableFile> list) {
            super();
            this.context = Ensure.notNull(context);
            this.setName(ResourcesUtils.getMessage("io.standard.output.msg024", FileUtils.getFilename(context.getFile().getAbsolutePath())));
            this.reader = in;
            this.files = list;
            this.out = out;
            this.comp = recordComparator;
        }

        public int execute() throws IOException {
            TextTableFile file = this.merge(this.files); // 合并文件
            if (file != null) {
                this.out.writeLine(file.getAbsolutePath()); // 将合并后文件写入清单文件
                this.out.flush();
            }
            return 0;
        }

        /**
         * 合并数据文件
         *
         * @param files 数据文件
         * @return 返回合并后文件, 操作被中断时返回null
         * @throws IOException 合并文件发生错误
         */
        public TextTableFile merge(List<TextTableFile> files) throws IOException {
            if (this.status.isTerminate()) {
                return null;
            }

            if (files.size() == 0) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("io.standard.output.msg027"));
            }

            // 如果只有一个文件则直接退出
            if (files.size() == 1) {
                return files.get(0);
            }

            // 待合并的文件个数大于1时
            List<TextTableFile> list = new ArrayList<TextTableFile>((files.size() / 2) + 1);
            for (int i = 0; i < files.size(); i++) {
                if (this.status.isTerminate()) {
                    return null;
                }

                TextTableFile file0 = files.get(i); // 第一个数据文件
                if (++i >= files.size()) {
                    list.add(file0);
                    break;
                }

                TextTableFile file1 = files.get(i); // 第二个数据文件
                list.add(this.merge(file0, file1)); // 合并后的文件保存到 list 中

                boolean delete0 = file0.delete();
                boolean delete1 = file1.delete();

                if (!delete0) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg025", file0.getAbsolutePath()));
                }

                if (!delete1) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg025", file1.getAbsolutePath()));
                }
            }

            return this.merge(list); // 递归调用，直到合并为1个文件为止
        }

        /**
         * 合并两个文件
         *
         * @param file1 文件1
         * @param file2 文件2
         * @return 返回合并后的文件, 被中断时返回 null
         * @throws IOException 合并文件发生错误
         */
        public TextTableFile merge(TextTableFile file1, TextTableFile file2) throws IOException {
            FileUtils.assertFile(file1.getFile());
            FileUtils.assertFile(file2.getFile());

            int readerBuffer = this.context.getReaderBuffer();
            TempFileCreator creator = context.getAttribute(TEMPFILE_CREATOR);

            // 合并操作
            TextTableFileReader in1 = null;
            TextTableFileReader in2 = null;
            try {
                in1 = file1.getReader(readerBuffer);
                in2 = file2.getReader(readerBuffer);

                // 合并后的文件
                TextTableFile file = file1.clone();
                file.setAbsolutePath(creator.toMergeFile().getAbsolutePath());
                BufferedLineWriter out = new BufferedLineWriter(file.getFile(), file.getCharsetName());
                try {
                    this.merge(in1, in2, out);
                } finally {
                    out.close();
                }

                // 保存合并记录数
                if (this.reader != null) {
                    this.reader.addLineNumbers(in1.getLineNumber() + in2.getLineNumber());
                }
                return file;
            } finally {
                IO.close(in1, in2);
            }
        }

        /**
         * 合并操作
         *
         * @param reader1 文件输入流
         * @param reader2 文件输入流
         * @param out     文件输出流
         * @throws IOException 访问文件错误
         */
        private void merge(TextTableFileReader reader1, TextTableFileReader reader2, BufferedLineWriter out) throws IOException {
            boolean duplicate = this.context.isDuplicate();
            TextTableFile file = this.context.getFile();
            TextTableLine line1 = reader1.readLine();
            TextTableLine line2 = reader2.readLine();

            // 比较文本 没有数据
            if (line1 == null) {
                while (line2 != null) {
                    if (this.status.isTerminate()) {
                        return;
                    } else {
                        out.writeLine(line2.getContent(), reader2.getLineSeparator());
                        line2 = reader2.readLine();
                    }
                }
                return;
            }

            // 被比较文本 没有数据
            if (line2 == null) {
                while (line1 != null) {
                    if (this.status.isTerminate()) {
                        return;
                    } else {
                        out.writeLine(line1.getContent(), reader1.getLineSeparator());
                        line1 = reader1.readLine();
                    }
                }
                return;
            }

            while (line1 != null && line2 != null) {
                if (this.status.isTerminate()) {
                    return;
                }

                TableLine record1 = new FileRecord(line1);
                TableLine record2 = new FileRecord(line2);
                int v = this.comp.compare(record1, record2);
                if (v == 0) {
                    if (duplicate) {
                        int column1 = line1.getColumn(); // 最右侧字段的位置，代表就在原文件中的行号
                        int column2 = line2.getColumn();
                        throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg029", file.getAbsolutePath(), line1.getColumn(column1), line2.getColumn(column2)));
                    } else {
                        out.writeLine(line1.getContent(), reader1.getLineSeparator());
                        out.writeLine(line2.getContent(), reader2.getLineSeparator());

                        line1 = reader1.readLine();
                        line2 = reader2.readLine();
                    }
                } else if (v < 0) {
                    out.writeLine(line1.getContent(), reader1.getLineSeparator());
                    line1 = reader1.readLine();
                } else {
                    out.writeLine(line2.getContent(), reader2.getLineSeparator());
                    line2 = reader2.readLine();
                }
            }

            while (line2 != null) {
                if (this.status.isTerminate()) {
                    return;
                } else {
                    out.writeLine(line2.getContent(), reader2.getLineSeparator());
                    line2 = reader2.readLine();
                }
            }

            while (line1 != null) {
                if (this.status.isTerminate()) {
                    return;
                } else {
                    out.writeLine(line1.getContent(), reader1.getLineSeparator());
                    line1 = reader1.readLine();
                }
            }

            out.flush();
        }
    }

    /**
     * 清单文件的输出流（必须支持多线程同步）
     */
    private static class ListfileWriter extends BufferedLineWriter {

        public ListfileWriter(File file, String charsetName) throws IOException {
            super(file, charsetName, 1);
        }

        public synchronized void write(String str) {
            super.write(str);
        }

        public synchronized boolean writeLine(String line) throws IOException {
            return super.writeLine(line);
        }

        public synchronized boolean writeLine(String line, String lineSeperator) throws IOException {
            return super.writeLine(line, lineSeperator);
        }

        public synchronized void flush() throws IOException {
            super.flush();
        }

        public synchronized void close() throws IOException {
            super.close();
        }
    }

    /**
     * 文件记录类
     */
    protected static class FileRecord implements TextTableLine {
        protected String line;
        protected String lineSeparator;
        protected String[] fields;
        protected int column;
        protected long lineNumber;

        public FileRecord(TextTableLine line) {
            this.column = line.getColumn();
            this.line = line.getContent();
            this.lineSeparator = line.getLineSeparator();
            this.fields = new String[this.column + 1];
            for (int i = 1; i <= this.column; i++) {
                this.fields[i] = line.getColumn(i);
            }
        }

        public FileRecord(TextTableLine line, long lineNumber) {
            this(line);
            this.lineNumber = lineNumber;
        }

        public String getContent() {
            return this.line;
        }

        public String getColumn(int index) {
            return this.fields[index];
        }

        public String getLineSeparator() {
            return this.lineSeparator;
        }

        public String toString() {
            return this.line;
        }

        public boolean isColumnBlank(int position) {
            return StringUtils.isBlank(this.fields[position]);
        }

        public void setColumn(int position, String value) {
            throw new UnsupportedOperationException();
        }

        public int getColumn() {
            return this.column;
        }

        public void setContext(String line) {
            throw new UnsupportedOperationException();
        }

        public long getLineNumber() {
            return this.lineNumber;
        }
    }

}
