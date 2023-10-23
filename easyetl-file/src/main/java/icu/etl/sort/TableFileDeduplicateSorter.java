package icu.etl.sort;

import java.io.BufferedReader;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.Executor;
import icu.etl.concurrent.ExecutorContainer;
import icu.etl.concurrent.ExecutorReader;
import icu.etl.expression.Analysis;
import icu.etl.expression.OrderByExpression;
import icu.etl.expression.StandardAnalysis;
import icu.etl.io.BufferedLineWriter;
import icu.etl.io.TableLine;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileCounter;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.BeanContext;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.Terminate;
import icu.etl.util.TerminateObserver;

/**
 * 表格文件排序
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-01-26
 */
public class TableFileDeduplicateSorter implements Terminate {

    private static String TEMPFILE_CREATOR = "TEMPFILE_CREATOR";

    private static String FILESIZE_MORE = "FILE_MORE";

    private static String FILELINE_NUMBER = "FILELINE_NUMBER";

    /** 上下文信息 */
    private TableFileSortContext context;

    /** true 表示终止排序操作 */
    private volatile boolean terminate;

    /** 观察者, 在终止剥离增量时，通知所有观察者执行终止操作 */
    protected TerminateObserver observers;

    /** 表格型记录排序规则 */
    private RecordComparator recordComparator;

    /**
     * 初始化
     *
     * @param context
     */
    public TableFileDeduplicateSorter(TableFileSortContext context) {
        super();
        this.context = Ensure.notnull(context);
        this.terminate = false;
        this.observers = new TerminateObserver();
    }

    /**
     * 初始化
     */
    public TableFileDeduplicateSorter() {
        this(new TableFileSortContext());
    }

    /**
     * 返回排序配置信息
     *
     * @return
     */
    public TableFileSortContext getContext() {
        return context;
    }

    public void terminate() {
        this.terminate = true;
        this.observers.terminate(false);
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
            throw new IOException(ResourcesUtils.getCommonMessage(6, "sort " + file.getAbsolutePath()));
        }
    }

    /**
     * 对文件中的行按设置字段进行排序
     *
     * @param context 容器上下文信息
     * @param file    数据文件
     * @param orders  排序字段表达式数组
     * @return 排序后的文件
     * @throws IOException
     */
    public synchronized File sort(BeanContext context, TextTableFile file, String... orders) throws IOException {
        Analysis analysis = new StandardAnalysis();
        OrderByExpression[] array = new OrderByExpression[orders.length];
        for (int i = 0; i < orders.length; i++) {
            array[i] = new OrderByExpression(context, analysis, orders[i], true);
        }
        return this.sort(file, array);
    }

    /**
     * 对文件中的行按设置字段进行排序
     *
     * @param file   数据文件
     * @param orders 排序字段表达式数组
     * @return 排序后的文件
     * @throws IOException
     */
    public synchronized File sort(TextTableFile file, OrderByExpression... orders) throws IOException {
        Ensure.notnull(file);
        Ensure.isfile(file.getFile());
        Ensure.notempty(orders);

        this.context.setFile(file);
        TempFileCreator creator = new TempFileCreator(this.context.getTempDir(), file.getFile());
        this.context.setAttribute(TEMPFILE_CREATOR, creator); // 临时文件工厂

        // 设置排序规则
        this.recordComparator = new RecordComparator(orders.length);
        for (OrderByExpression expr : orders) {
            this.recordComparator.add(expr.getPosition(), expr.getComparator(), expr.isAsc());
        }

        try {
            return this.sort(file, creator);
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
     * @throws IOException 排序发生错误
     */
    protected File sort(TextTableFile file, TempFileCreator creator) throws IOException {
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

        // 判断移动前后文件大小是否有变化
        long mergefilelength = mergefile.length() - ((Long) this.context.getAttribute(FILESIZE_MORE)).longValue();
        if (!mergefile.exists() || mergefilelength != oldlength) {
            throw new IOException(ResourcesUtils.getIoxMessage(15, mergefile.getAbsolutePath(), mergefilelength, oldfile.getAbsolutePath(), oldlength));
        }

        // 判断合并前与合并后文件记录数是否相等
        if (this.context.getReadLineNumber() != this.context.getMergeLineNumber()) {
            throw new IOException(ResourcesUtils.getIoxMessage(36, file.getAbsolutePath(), this.context.getReadLineNumber(), this.context.getMergeLineNumber()));
        }

        // 保留源文件时，直接返回排序后的文件
        if (this.context.keepSource()) {
            File newfile = creator.toSortfile();
            if (mergefile.renameTo(newfile)) {
                return newfile;
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(35, mergefile.getAbsolutePath(), newfile.getAbsolutePath()));
            }
        } else {
            /**
             * 不保留源文件时:
             * 1.将原文件（oldfile）重命名为一个带 bak 扩展名的文件
             * 2.将排序后文件（mergefile）重命名为原文件名（oldfile）
             * 3.再删除带 bak 扩展名的文件
             */
            File bakfile = creator.toBakfile();
            if (!oldfile.renameTo(bakfile)) { // 原文件名 修改为 备份文件名
                throw new IOException(ResourcesUtils.getIoxMessage(35, oldfile.getAbsolutePath(), bakfile.getAbsolutePath()));
            }

            if (!mergefile.renameTo(oldfile)) { // 新排序的文件名 修改为 原文件名
                bakfile.renameTo(oldfile); // 不能重命名时，需要将 备份文件名 恢复为 原文件名
                throw new IOException(ResourcesUtils.getIoxMessage(35, mergefile.getAbsolutePath(), oldfile.getAbsolutePath()));
            }

            if (bakfile.delete()) { // 删除备份文件
                return oldfile;
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(16, bakfile.getAbsolutePath()));
            }
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
            TempFileWriter out = new TempFileWriter(creator, listfile, lineSeparator, file.getDelimiter(), file.getCharsetName(), this.context.getMaxRows(), this.context.getWriterBuffer(), this.recordComparator);
            try {
                while (line != null) {
                    if (this.terminate) {
                        break;
                    } else {
                        out.writeRecord(new FileRecord(line, line.getLineNumber()));
                        line = in.readLine();
                    }
                }
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

        this.context.setAttribute(FILESIZE_MORE, Long.valueOf(bytes));
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
    private File merge(File listfile, String charsetName) throws IOException {
        long number = new TextTableFileCounter().execute(listfile, charsetName);
        if (number == 0) {
            throw new IOException(ResourcesUtils.getIoxMessage(18, listfile.getAbsolutePath()));
        }

        // 清单文件中只有一个文件路径时
        if (number == 1) {
            File newfile = new File(StringUtils.trimBlank(FileUtils.readline(listfile, charsetName, 0)));
            if (!newfile.exists() || !newfile.isFile() || !newfile.canRead()) {
                throw new IOException(ResourcesUtils.getIoxMessage(19, newfile.getAbsolutePath()));
            } else {
                this.deleteListfile(listfile);
                Long mergeLines = this.context.getAttribute(FILELINE_NUMBER); // 最后一次合并的文件记录数作为最终值
                if (mergeLines == null) {
                    this.context.setMergeLineNumber(new TextTableFileCounter().execute(newfile, charsetName));
                } else {
                    this.context.setMergeLineNumber(mergeLines.longValue());
                }
                return newfile;
            }
        }

        // 清单文件中有很多文件路径
        else {
            TempFileCreator creator = this.context.getAttribute(TEMPFILE_CREATOR);
            File newlistfile = creator.toListfile(); // 合并后的清单文件
            ListfileWriter listfileout = new ListfileWriter(newlistfile, charsetName);
            try {
                this.merge(listfile, listfileout);
            } finally {
                listfileout.close();
                this.deleteListfile(listfile); // 合并完清单文件后，需要删除清单文件
            }

            return this.merge(newlistfile, charsetName); // 递归调用，继续合并新清单文件中的临时文件
        }
    }

    /**
     * 合并清单文件 {@code listfile} 中的临时文件, 并将合并后结果（也就是新产生的临时文件路径）写入到 {@code listfileout} 输出流中
     *
     * @param listfile    清单文件
     * @param listfileout 新清单文件输出流
     * @throws IOException
     */
    protected void merge(File listfile, ListfileWriter listfileout) throws IOException {
        MergeExecutorReader in = new MergeExecutorReader(this.context, listfile, listfileout, this.recordComparator);
        try {
            if (this.context.getThreadNumber() <= 1) { // 串行执行
                while (in.hasNext() && !this.terminate) {
                    MergeExecutor task = in.next();
                    try {
                        this.observers.add(task);
                        task.execute();
                    } finally {
                        this.observers.remove(task);
                    }
                }
            } else { // 使用线程池并行执行
                ExecutorContainer container = new ExecutorContainer(in);
                try {
                    this.observers.add(container);
                    container.execute(this.context.getThreadNumber());
                    if (container.hasError()) {
                        throw new IOException(ResourcesUtils.getIoxMessage(21));
                    }
                } finally {
                    this.observers.remove(container);
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
     * @throws IOException 删除文件时发生错误
     */
    private void deleteListfile(File listfile) throws IOException {
        if (this.context.isDeleteFile() && !listfile.delete()) { // 删除清单文件
            throw new IOException(ResourcesUtils.getIoxMessage(20, listfile));
        }
    }

    protected class RecordComparator implements Comparator<TableLine> {

        /** 字段位置的数组，元素都是位置信息，从1开始 */
        private int[] positions;

        /** 字符串比较方法 */
        private Comparator<String>[] comparators;

        /** 容量，从0开始 */
        private int size;

        /**
         * 初始化
         */
        public RecordComparator(int size) {
            Ensure.isPosition(size);
            this.positions = new int[size];
            this.comparators = new Comparator[size];
            this.size = 0;
        }

        /**
         * 添加排序字段
         *
         * @param position   字段位置信息，从1开始
         * @param comparator 排序规则
         * @param asc        true：正向 false：反向排序
         */
        public void add(int position, Comparator<String> comparator, boolean asc) {
            this.positions[this.size] = position;
            this.comparators[this.size] = asc ? comparator : new ReverseComparator(comparator);
            this.size++;
        }

        public int compare(TableLine record1, TableLine record2) {
            for (int i = 0; i < this.positions.length; i++) {
                int position = this.positions[i];
                int c = this.comparators[i].compare(record1.getColumn(position), record2.getColumn(position));
                if (c == 0) {
                    continue;
                } else {
                    return c;
                }
            }
            return 0;
        }

    }

    /**
     * 反转排序规则
     */
    protected class ReverseComparator implements Comparator<String> {
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
    protected class MergeExecutorReader implements ExecutorReader {

        /** 排序组件 */
        private TableFileSortContext context;

        /** 当前合并任务对象 */
        private MergeExecutor task;

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
         * @param cxt              待排序文件
         * @param listfile         清单文件
         * @param listfileout      清单文件输出流（用于存储合并后产生的临时文件绝对路径）
         * @param recordComparator 记录排序规则
         * @throws IOException
         */
        public MergeExecutorReader(TableFileSortContext cxt, File listfile, ListfileWriter listfileout, RecordComparator recordComparator) throws IOException {
            if (listfile == null || !listfile.exists()) {
                throw new IllegalArgumentException(ResourcesUtils.getIoxMessage(23, listfile));
            }

            this.context = cxt;
            this.in = IO.getBufferedReader(listfile, this.context.getFile().getCharsetName());
            this.listfileout = listfileout;
            this.recordComparator = recordComparator;
        }

        public synchronized boolean hasNext() throws IOException {
            List<TextTableFile> list = new ArrayList<TextTableFile>();
            String filepath;
            TextTableFile file = this.context.getFile();
            for (int i = 1; i <= this.context.getFileCount() && (filepath = this.in.readLine()) != null; i++) {
                if (StringUtils.isNotBlank(filepath)) {
                    TextTableFile clone = file.clone();
                    clone.setAbsolutePath(StringUtils.trimBlank(filepath));
                    list.add(clone);
                }
            }

            if (list.size() == 0) {
                return false;
            } else {
                String name = ResourcesUtils.getIoxMessage(24, FileUtils.getFilename(file.getAbsolutePath()));
                TempFileCreator creator = context.getAttribute(TEMPFILE_CREATOR);
                boolean deleteFile = this.context.isDeleteFile();
                int readerBuffer = this.context.getReaderBuffer();
                this.task = new MergeExecutor(this, name, this.listfileout, deleteFile, readerBuffer, creator, this.recordComparator, list);
                return true;
            }
        }

        public synchronized MergeExecutor next() {
            return this.task;
        }

        public synchronized void close() throws IOException {
            this.in.close();
            this.in = null;
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
                this.context.setAttribute(FILELINE_NUMBER, Long.valueOf(lines));
            }
        }
    }

    /**
     * 将一个大文件切分成若干小文件
     */
    protected class TempFileWriter implements java.io.Closeable, Flushable {

        /** 缓冲区数组 */
        private TextTableLine[] array;

        /** 缓冲区实际容量 */
        private int size;

        /** 缓冲区数组的容量 */
        private int capacity;

        /** 清单文件写入流 */
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

        public TempFileWriter(TempFileCreator creator, File listfile, String lineSeparator, String delimiter, String charsetName, int bufferSize, int writeBuffer, RecordComparator recordComparator) throws IOException {
            this.size = 0;
            this.creator = creator;
            this.listfileout = new BufferedLineWriter(listfile, charsetName, 1);
            this.capacity = bufferSize;
            this.array = new TextTableLine[this.capacity];
            this.delimiter = delimiter;
            this.writeBuffer = writeBuffer;
            this.recordComparator = recordComparator;
            this.lineSeparator = lineSeparator;
        }

        /**
         * 讲记录写入到临时文件中
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
                File file = this.creator.toTempFile(); // 临时文件
                try {
                    this.writeFile(file); // 写入临时文件
                } finally {
                    if (file.exists()) {
                        this.listfileout.writeLine(file.getAbsolutePath()); // 将临时文件绝对路径写入到清单文件
                    }
                }
                this.size = 0;
            }
        }

        /**
         * 把缓存记录写入到临时文件中
         *
         * @param file 临时文件
         * @throws IOException
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
            this.flush();
            this.listfileout.close();
            this.array = null;
            this.size = 0;
        }
    }

    /**
     * 合并数据文件
     */
    protected class MergeExecutor extends Executor {

        /** 合并任务容器 */
        private MergeExecutorReader reader;

        /** 待合并数据文件 */
        private List<TextTableFile> files;

        /** true表示排序结束后删除临时文件 */
        private boolean deleteTempFile;

        /** 文件清单的输出流 */
        private ListfileWriter out;

        /** 排序规则 */
        private Comparator<TableLine> comp;

        /** 文件输入流的缓冲区长度，单位：字符 */
        private int readerBuffer;

        /** 临时文件工厂 */
        private TempFileCreator creator;

        /**
         * 初始化
         *
         * @param in               任务所属的输入流
         * @param name             任务名
         * @param listfileout      文件清单的输出流（将产生的临时文件绝对路径写入到输出流中）
         * @param deleteTempFile   是否删除临时文件
         * @param readerBuffer     输入流缓冲区大小
         * @param creator          临时文件工厂
         * @param recordComparator 记录排序规则
         * @param list             记录排序规则
         */
        public MergeExecutor(MergeExecutorReader in, String name, ListfileWriter listfileout, boolean deleteTempFile, int readerBuffer, TempFileCreator creator, RecordComparator recordComparator, List<TextTableFile> list) {
            super();
            this.setName(name);
            this.reader = in;
            this.files = list;
            this.out = listfileout;
            this.comp = recordComparator;
            this.deleteTempFile = deleteTempFile;
            this.readerBuffer = readerBuffer;
            this.creator = creator;
        }

        public void execute() throws IOException {
            TextTableFile file = this.merge(this.files); // 合并文件
            if (file != null) {
                this.out.writeLine(file.getAbsolutePath()); // 将合并后文件写入清单文件
            }
        }

        /**
         * 合并数据文件
         *
         * @param files 数据文件
         * @return 返回合并后文件, 操作被中断时返回null
         * @throws IOException 合并文件发生错误
         */
        public TextTableFile merge(List<TextTableFile> files) throws IOException {
            if (this.terminate) {
                return null;
            }

            if (files.size() == 0) {
                throw new IllegalArgumentException(ResourcesUtils.getIoxMessage(27));
            }

            // 如果只有一个文件则直接退出
            if (files.size() == 1) {
                return files.get(0);
            }

            // 待合并的文件个数大于1时
            List<TextTableFile> list = new ArrayList<TextTableFile>((files.size() / 2) + 1);
            for (int i = 0; i < files.size(); i++) {
                if (this.terminate) {
                    return null;
                }

                TextTableFile file0 = files.get(i); // 第一个数据文件
                if (++i >= files.size()) {
                    list.add(file0);
                    break;
                }

                TextTableFile file1 = files.get(i); // 第二个数据文件
                list.add(this.merge(file0, file1)); // 合并后的文件保存到 list 中
                if (this.terminate) {
                    return null;
                }

                if (this.deleteTempFile) { // 删除已合并的临时文件
                    if (!file0.delete()) {
                        throw new IOException(ResourcesUtils.getIoxMessage(25, file0.getAbsolutePath()));
                    }
                    if (!file1.delete()) {
                        throw new IOException(ResourcesUtils.getIoxMessage(25, file1.getAbsolutePath()));
                    }
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
            if (!file1.getFile().exists()) {
                throw new IOException(ResourcesUtils.getIoxMessage(37, file1.getAbsolutePath()));
            }
            if (file1.getFile().isDirectory()) {
                throw new IOException(ResourcesUtils.getIoxMessage(38, file1.getAbsolutePath()));
            }
            if (!file2.getFile().exists()) {
                throw new IOException(ResourcesUtils.getIoxMessage(37, file2.getAbsolutePath()));
            }
            if (file2.getFile().isDirectory()) {
                throw new IOException(ResourcesUtils.getIoxMessage(38, file2.getAbsolutePath()));
            }

            TextTableFile newfile = file1.clone();
            newfile.setAbsolutePath(this.creator.toMergeFile().getAbsolutePath());
            BufferedLineWriter out = new BufferedLineWriter(newfile.getFile(), newfile.getCharsetName());
            TextTableFileReader in1 = file1.getReader(this.readerBuffer);
            TextTableFileReader in2 = file2.getReader(this.readerBuffer);
            try {
                TextTableLine r1 = in1.readLine();
                TextTableLine r2 = in2.readLine();

                // 比较文本 没有数据
                if (r1 == null) {
                    while (r2 != null) {
                        if (this.terminate) {
                            return null;
                        }

                        out.writeLine(r2.getContent(), in2.getLineSeparator());
                        r2 = in2.readLine();
                    }
                    return newfile;
                }

                // 被比较文本 没有数据
                if (r2 == null) {
                    while (r1 != null) {
                        if (this.terminate) {
                            return null;
                        }

                        out.writeLine(r1.getContent(), in1.getLineSeparator());
                        r1 = in1.readLine();
                    }
                    return newfile;
                }

                while (r1 != null && r2 != null) {
                    if (this.terminate) {
                        return null;
                    }

                    TableLine record1 = new FileRecord(r1);
                    TableLine record2 = new FileRecord(r2);
                    int ret = this.comp.compare(record1, record2);
                    if (ret == 0) {
                        out.writeLine(r1.getContent(), in1.getLineSeparator());
                        out.writeLine(r2.getContent(), in2.getLineSeparator());

                        r1 = in1.readLine();
                        r2 = in2.readLine();
                    } else if (ret < 0) {
                        out.writeLine(r1.getContent(), in1.getLineSeparator());
                        r1 = in1.readLine();
                    } else {
                        out.writeLine(r2.getContent(), in2.getLineSeparator());
                        r2 = in2.readLine();
                    }
                }

                while (r2 != null) {
                    if (this.terminate) {
                        return null;
                    }

                    out.writeLine(r2.getContent(), in2.getLineSeparator());
                    r2 = in2.readLine();
                }

                while (r1 != null) {
                    if (this.terminate) {
                        return null;
                    }

                    out.writeLine(r1.getContent(), in1.getLineSeparator());
                    r1 = in1.readLine();
                }

                return newfile;
            } finally {
                in1.close();
                in2.close();
                if (this.reader != null) {
                    this.reader.addLineNumbers(in1.getLineNumber() + in2.getLineNumber());
                }
                out.flush();
                out.close();
            }
        }

        public int getPRI() {
            return 0;
        }
    }

    /**
     * 清单文件的输出流（必须支持多线程同步）
     */
    class ListfileWriter extends BufferedLineWriter {

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
    protected class FileRecord implements TextTableLine {
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
