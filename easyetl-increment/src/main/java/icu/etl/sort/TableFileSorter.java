package icu.etl.sort;

import java.io.BufferedReader;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import icu.etl.concurrent.ThreadSource;
import icu.etl.expression.Analysis;
import icu.etl.expression.BaseAnalysis;
import icu.etl.io.BufferedLineWriter;
import icu.etl.io.TableLine;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileCounter;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyContext;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 表格文件排序
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-01-26
 */
public class TableFileSorter implements Terminate {

    /** 上下文信息 */
    private TableFileSortContext context;

    /** true 表示终止排序操作 */
    private volatile boolean terminate;

    /** 观察者 */
    protected Terminates observers;

    /** 表格型记录排序规则 */
    private RecordComparator recordComparator;

    /**
     * 初始化
     *
     * @param context
     */
    public TableFileSorter(TableFileSortContext context) {
        super();

        if (context == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.terminate = false;
        this.observers = new Terminates();
        this.recordComparator = new RecordComparator();
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
        this.observers.terminate();
    }

    public boolean isTerminate() {
        return this.terminate;
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
    public synchronized File sort(EasyContext context, TextTableFile file, String... orders) throws Exception {
        Analysis analysis = new BaseAnalysis();
        OrderByExpression[] array = new OrderByExpression[orders.length];
        for (int i = 0; i < orders.length; i++) {
            array[i] = new OrderByExpression(context, analysis, orders[i]);
        }
        return this.sort(file, array);
    }

    /**
     * 对文件中的行按设置字段进行排序
     *
     * @param file   数据文件
     * @param orders 排序字段表达式数组
     * @return 排序后的文件
     * @throws Exception 发生错误
     */
    public synchronized File sort(TextTableFile file, OrderByExpression... orders) throws Exception {
        if (file == null) {
            throw new NullPointerException();
        }
        if (orders.length == 0) {
            return new File(file.getAbsolutePath());
        }

        this.context.setFile(file);

        // 设置排序规则
        this.recordComparator.clear();
        for (OrderByExpression expr : orders) {
            this.recordComparator.add(expr.getPosition(), expr.getComparator(), expr.isAsc());
        }

        // 排序前文件信息
        File oldfile = file.getFile();
        long oldlength = oldfile.length();
        if (oldfile.exists() && oldlength == 0) {
            return oldfile;
        }

        // 将大文件分成多个小文件，并返回清单文件
        File listfile = this.divide(file);

        // 合并小文件
        File newfile = this.merge(file, listfile);

        // 已被终止
        if (this.terminate) {
            throw new IOException(ResourcesUtils.getMessage("commons.standard.output.msg006", "sort " + file.getAbsolutePath()));
        }

        // 判断移动前后文件大小是否有变化
        if (!newfile.exists() || newfile.length() != oldlength) {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg015", newfile.getAbsolutePath(), newfile.length(), oldfile.getAbsolutePath(), oldlength));
        }

        // 判断合并前与合并后文件记录数是否相等
        if (this.context.getReadLineNumber() != this.context.getMergeLineNumber()) {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg036", file.getAbsolutePath(), this.context.getReadLineNumber(), this.context.getMergeLineNumber()));
        }

        // 删除临时文件
        if (this.context.isDeleteFile()) {
            if (!this.context.keepSource() && !oldfile.delete()) {
                throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg016", oldfile.getAbsolutePath()));
            }
        } else { // 保留排序前的文件
            String newfilename = FileUtils.getFilenameNoSuffix(oldfile.getName()) + ".bak" + Dates.format17() + StringUtils.toRandomUUID();
            File bakfile = new File(oldfile.getParentFile(), newfilename);
            if (!oldfile.renameTo(bakfile)) {
                throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg035", oldfile.getAbsolutePath(), bakfile.getAbsolutePath()));
            }
        }

        if (this.context.keepSource()) { // 保留源文件
            return newfile;
        } else if (newfile.renameTo(oldfile)) { // 不保留源文件
            return oldfile;
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg035", oldfile.getAbsolutePath(), newfile.getAbsolutePath()));
        }
    }

    /**
     * 将大文件中内容写入到小文件中，同时对小文件内容进行排序, 将小文件绝对路径写入清单文件
     *
     * @param file 数据文件
     * @return 临时文件的清单文件(清单文件内容是所有临时文件的绝对路径 ， 路径分隔符是回车或换行符)
     * @throws IOException 访问文件错误
     */
    private File divide(TextTableFile file) throws IOException {
        TextTableFileReader in = file.getReader(this.context.getReaderBuffer());
        try {
            TempFileWriter out = new TempFileWriter(this.context, this.recordComparator);
            try {
                TextTableLine line = null;
                while ((line = in.readLine()) != null) {
                    if (this.terminate) {
                        break;
                    }
                    out.writeRecord(new FileRecord(line));
                }
            } finally {
                out.close();
            }
            return out.getListfile();
        } finally {
            in.close();
            this.context.setReadLineNumber(in.getLineNumber());
        }
    }

    /**
     * 合并清单文件中的临时文件
     *
     * @param file     文件
     * @param listfile 清单文件(清单文件内容是所有临时文件的绝对路径，路径分隔符是回车或换行符)
     * @return 合并后的文件
     * @throws Exception 发生错误
     */
    private synchronized File merge(TextTableFile file, File listfile) throws Exception {
        while (true) {
            long number = FileUtils.count(listfile, file.getCharsetName());
            if (number == 0) {
                throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg018", listfile.getAbsolutePath()));
            } else if (number == 1) {
                String filePath = StringUtils.trimBlank(FileUtils.readline(listfile, file.getCharsetName(), 0));
                File newfile = new File(filePath);
                if (!newfile.exists() || !newfile.isFile() || !newfile.canRead()) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg019", filePath));
                }
                if (this.context.isDeleteFile() && !listfile.delete()) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg020", listfile));
                }
                if (this.context.getMergeLineNumber() == 0) { // 如果是单线程
                    this.context.setMergeLineNumber(new TextTableFileCounter(this.context.getThreadSource(), this.context.getThreadNumber()).execute(newfile, file.getCharsetName()));
                }
                return newfile;
            } else {
                MergeExecutorReader in = new MergeExecutorReader(this.context, listfile, this.recordComparator);
                try {
                    if (this.context.getThreadNumber() <= 1) { // 串行执行
                        while (in.hasNext() && !this.terminate) {
                            in.next().execute();
                        }
                    } else { // 使用线程池并行执行
                        ThreadSource threadSource = this.context.getThreadSource();
                        EasyJobService runner = threadSource.getJobService(this.context.getThreadNumber());
                        try {
                            this.observers.add(runner);
                            runner.execute(new EasyJobReaderImpl(in));
                        } finally {
                            this.observers.remove(runner);
                        }
                    }
                } finally {
                    in.close();
                }

                if (number == 2) { // 合并最后二个文件的记录总数就是合并后文件总数
                    this.context.setMergeLineNumber(in.getLineNumber());
                }
                if (this.context.isDeleteFile() && !listfile.delete()) {
                    throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg020", listfile));
                }
                listfile = in.getListfile();
            }
        }
    }

    /**
     * 生成清单文件
     *
     * @param file 数据文件
     * @return 清单文件
     * @throws IOException 访问文件错误
     */
    private static synchronized File toListfile(TextTableFile file) throws IOException {
        String newfilename = FileUtils.getFilenameNoSuffix(file.getAbsolutePath()) + ".list" + StringUtils.toRandomUUID();
        File dir = new File(file.getAbsolutePath()).getParentFile();
        File listfile = new File(dir, newfilename);
        if (listfile.exists()) {
            return toListfile(file);
        } else if (FileUtils.createFile(listfile)) {
            return listfile;
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg022", listfile.getAbsolutePath()));
        }
    }

    /**
     * 生成合并后的临时文件
     *
     * @param file 数据文件
     * @return 临时文件
     * @throws IOException 访问文件错误
     */
    private static synchronized File toMergeFile(TextTableFile file) throws IOException {
        String newfilename = FileUtils.getFilenameNoSuffix(file.getAbsolutePath()) + ".merge" + Dates.format17() + StringUtils.toRandomUUID();
        File dir = new File(file.getAbsolutePath()).getParentFile();
        File mergefile = new File(dir, newfilename);
        if (mergefile.exists()) {
            return toMergeFile(file);
        } else if (FileUtils.createFile(mergefile)) {
            return mergefile;
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg022", mergefile.getAbsolutePath()));
        }
    }

    /**
     * 生成临时文件
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    private static synchronized File toTempFile(String filepath) throws IOException {
        String newfilename = FileUtils.getFilenameNoSuffix(filepath) + ".temp" + Dates.format17() + StringUtils.toRandomUUID();
        File dir = new File(filepath).getParentFile();
        File file = new File(dir, newfilename);
        if (file.exists()) {
            return toTempFile(filepath);
        } else if (FileUtils.createFile(file)) {
            return file;
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg022", file.getAbsolutePath()));
        }
    }

    protected class RecordComparator implements Comparator<TableLine> {

        private List<Integer> positionlist;

        private List<Comparator<String>> complist;

        /** 字段数值的数组，从 1 开始 */
        private int[] positions;

        /** 字符串比较方法 */
        private Comparator<String>[] comparators;

        /**
         * 初始化
         */
        public RecordComparator() {
            this.positionlist = new ArrayList<Integer>();
            this.complist = new ArrayList<Comparator<String>>();
        }

        @SuppressWarnings("unchecked")
        public void add(int position, Comparator<String> comparator, boolean asc) {
            this.positionlist.add(new Integer(position));
            this.complist.add(asc ? comparator : new ReverseComparator(comparator));

            int size = this.positionlist.size();
            this.positions = new int[size];
            for (int i = 0; i < this.positionlist.size(); i++) {
                this.positions[i] = this.positionlist.get(i).intValue();
            }
            this.comparators = this.complist.toArray(new Comparator[size]);
        }

        @SuppressWarnings("unchecked")
        public void clear() {
            this.positionlist.clear();
            this.complist.clear();
            this.positions = new int[0];
            this.comparators = new Comparator[0];
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
    protected static class MergeExecutorReader implements EasyJobReader {

        /** 排序组件 */
        private TableFileSortContext context;

        /** 当前合并任务对象 */
        private MergeExecutor task;

        /** 清单文件的输入流 */
        private BufferedReader in;

        /** 清单文件 */
        private File listfile;

        /** 文件合并完成后产生的新清单文件 */
        private File newListfile;

        /** 新清单文件的IO流 */
        private ListfileWriter listfileout;

        /** true 表示已终止 */
        private volatile boolean terminate;

        /** 合并后记录数 */
        private long lineNumber;

        /** 记录排序规则 */
        private RecordComparator recordComparator;

        /**
         * 初始化
         *
         * @param cxt              待排序文件
         * @param listfile         清单文件
         * @param recordComparator 记录排序规则
         * @throws IOException
         */
        public MergeExecutorReader(TableFileSortContext cxt, File listfile, RecordComparator recordComparator) throws IOException {
            if (listfile == null || !listfile.exists()) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("io.standard.output.msg023", listfile));
            }

            this.context = cxt;
            this.listfile = listfile;
            this.recordComparator = recordComparator;

            TextTableFile file = this.context.getFile();
            this.in = IO.getBufferedReader(this.listfile, file.getCharsetName());
            this.newListfile = TableFileSorter.toListfile(file);
            this.listfileout = new ListfileWriter(this.newListfile, file.getCharsetName(), 1);
        }

        public synchronized boolean hasNext() throws IOException {
            ArrayList<TextTableFile> list = new ArrayList<TextTableFile>();
            String filepath = null;
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
                String name = ResourcesUtils.getMessage("io.standard.output.msg024", FileUtils.getFilename(file.getAbsolutePath()));
                this.task = new MergeExecutor(name, this.listfileout, this.context, this.recordComparator);
                this.task.setFiles(list);
                return true;
            }
        }

        public synchronized MergeExecutor next() {
            return this.task;
        }

        public synchronized void close() throws IOException {
            this.lineNumber = this.task.getLineNumber();
            this.in.close();
            this.in = null;
            this.listfileout.close();
            this.listfileout = null;
            this.task = null;
            this.listfile = null;
        }

        /**
         * 返回合并记录总数
         *
         * @return
         */
        public long getLineNumber() {
            return lineNumber;
        }

        /**
         * 合并文件产生的清单文件
         *
         * @return
         */
        public File getListfile() {
            return this.newListfile;
        }

        public void terminate() {
            this.terminate = true;
        }

        public boolean isTerminate() {
            return terminate;
        }
    }

    /**
     * 将一个大文件切分成若干小文件
     *
     * @author jeremy8551@qq.com
     */
    protected class TempFileWriter implements java.io.Closeable, Flushable {

        /** 排序工具 */
        private TableFileSortContext context;

        /** 缓冲区 */
        private TextTableLine[] buffer;

        /** 缓冲区长度 */
        private int bufferSize;

        /** 清单文件写入流 */
        private BufferedLineWriter listfileout;

        /** 清单文件 */
        private File listfile;

        /** 表格型记录排序规则 */
        private RecordComparator recordComparator;

        /**
         * 初始化
         *
         * @param context
         * @param recordComparator
         * @throws IOException
         */
        public TempFileWriter(TableFileSortContext context, RecordComparator recordComparator) throws IOException {
            this.context = context;
            this.recordComparator = recordComparator;
            TextTableFile file = this.context.getFile();
            this.listfile = TableFileSorter.toListfile(file);
            this.listfileout = new BufferedLineWriter(this.listfile, file.getCharsetName(), 1);
            this.buffer = new TextTableLine[this.context.getMaxRows()];
            this.bufferSize = 0;
        }

        /**
         * 清单文件
         *
         * @return
         */
        public File getListfile() {
            return this.listfile;
        }

        /**
         * 讲记录写入到临时文件中
         *
         * @param record 数据文件
         * @throws IOException 写入文件发生错误
         */
        public void writeRecord(TextTableLine record) throws IOException {
            this.buffer[this.bufferSize++] = record;
            if (this.bufferSize == this.buffer.length) {
                this.flush();
            }
        }

        public void flush() throws IOException {
            if (this.bufferSize > 0) {
                Arrays.sort(this.buffer, 0, this.bufferSize, this.recordComparator); // 排序
                File file = TableFileSorter.toTempFile(this.context.getFile().getAbsolutePath()); // 临时文件
                this.writeFile(file); // 写入临时文件
                this.listfileout.writeLine(file.getAbsolutePath(), FileUtils.lineSeparator); // 将临时文件绝对路径写入到清单文件
                this.bufferSize = 0;
            }
        }

        /**
         * 把缓存记录写入到临时文件中
         *
         * @param file 临时文件
         * @throws IOException
         */
        protected void writeFile(File file) throws IOException {
            String charsetName = this.listfileout.getCharsetName();
            OutputStreamWriter out = IO.getFileWriter(file, charsetName, false);
            try {
                for (int i = 0; i < this.bufferSize; i++) {
                    TextTableLine record = this.buffer[i];
                    out.write(record.getContent());
                    out.write(record.getLineSeparator());
                    if (i % this.context.getWriterBuffer() == 0) {
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
            this.buffer = null;
            this.bufferSize = 0;
        }
    }

    /**
     * 合并数据文件
     */
    protected static class MergeExecutor extends AbstractJob {

        /** 待合并数据文件 */
        private ArrayList<TextTableFile> files = new ArrayList<TextTableFile>();

        /** 临时文件 */
        private ArrayList<TextTableFile> tmpFiles = new ArrayList<TextTableFile>();

        /** true表示排序结束后删除临时文件 */
        private boolean deleteTempFile;

        /** 文件清单的输出流 */
        private ListfileWriter out;

        /** 排序规则 */
        private Comparator<TableLine> comp;

        /** 合并的总行数 */
        private long mergeLines;

        /** 文件输入流的缓冲区长度，单位：字符 */
        private int readerBuffer;

        /**
         * 合并数据文件
         *
         * @param name             任务名
         * @param listfileout      文件清单的输出流
         * @param context          排序组件的上下文信息
         * @param recordComparator 记录排序规则
         */
        public MergeExecutor(String name, ListfileWriter listfileout, TableFileSortContext context, RecordComparator recordComparator) {
            this.setName(name);
            this.out = listfileout;
            this.comp = recordComparator;
            this.deleteTempFile = context.isDeleteFile();
            this.readerBuffer = context.getReaderBuffer();
        }

        public int execute() throws IOException {
            this.tmpFiles.clear();
            TextTableFile file = this.merge(this.files); // 合并文件
            this.out.writeLine(file.getAbsolutePath(), FileUtils.lineSeparator); // 将合并后文件写入清单文件

            if (this.deleteTempFile) {
                for (TextTableFile f : this.files) {
                    if (!f.getAbsolutePath().equals(file.getAbsolutePath()) && !f.delete()) {
                        throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg025", f.getAbsolutePath()));
                    }
                }

                for (TextTableFile f : this.tmpFiles) {
                    if (!f.getAbsolutePath().equals(file.getAbsolutePath()) && !f.delete()) {
                        throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg026", f.getAbsolutePath()));
                    }
                }
            }

            return 0;
        }

        /**
         * 合并数据文件
         *
         * @param files 数据文件
         * @return 返回合并后文件
         * @throws IOException
         */
        public TextTableFile merge(List<TextTableFile> files) throws IOException {
            if (files == null || files.size() == 0) {
                throw new IllegalArgumentException(ResourcesUtils.getMessage("io.standard.output.msg027"));
            } else if (files.size() == 1) {
                return files.get(0); // 如果只有一个文件则直接退出
            }

            List<TextTableFile> list = new ArrayList<TextTableFile>((files.size() / 2) + 1);
            for (int i = 0; i < files.size(); i++) {
                TextTableFile file0 = files.get(i); // 第一个数据文件
                if (++i >= files.size()) {
                    list.add(file0);
                    break;
                }

                TextTableFile file1 = files.get(i); // 第二个数据文件
                TextTableFile newfile = this.merge(file0, file1);
                list.add(newfile);
                this.tmpFiles.add(newfile);
            }

            return this.merge(list); // 再次合并文件
        }

        /**
         * 合并二个文件
         *
         * @param file1 文件1
         * @param file2 文件2
         * @throws IOException
         */
        public TextTableFile merge(TextTableFile file1, TextTableFile file2) throws IOException {
            TextTableFile newfile = file1.clone();
            newfile.setAbsolutePath(TableFileSorter.toMergeFile(file1).getAbsolutePath());
            BufferedLineWriter out = new BufferedLineWriter(newfile.getFile(), newfile.getCharsetName());
            TextTableFileReader in1 = file1.getReader(this.readerBuffer);
            TextTableFileReader in2 = file2.getReader(this.readerBuffer);
            try {
                TextTableLine r1 = in1.readLine();
                TextTableLine r2 = in2.readLine();

                // 比较文本 没有数据
                if (r1 == null) {
                    while (r2 != null) {
                        out.writeLine(r2.getContent(), in2.getLineSeparator());
                        r2 = in2.readLine();
                    }
                    return newfile;
                }

                // 被比较文本 没有数据
                if (r2 == null) {
                    while (r1 != null) {
                        out.writeLine(r1.getContent(), in1.getLineSeparator());
                        r1 = in1.readLine();
                    }
                    return newfile;
                }

                while (r1 != null && r2 != null) {
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
                    out.writeLine(r2.getContent(), in2.getLineSeparator());
                    r2 = in2.readLine();
                }

                while (r1 != null) {
                    out.writeLine(r1.getContent(), in1.getLineSeparator());
                    r1 = in1.readLine();
                }

                return newfile;
            } finally {
                in1.close();
                in2.close();
                this.mergeLines += in1.getLineNumber() + in2.getLineNumber();

                out.flush();
                out.close();
            }
        }

        /**
         * 返回记录总和
         *
         * @return
         */
        public long getLineNumber() {
            return mergeLines;
        }

        /**
         * 添加待合并数据文件
         *
         * @param files
         */
        public synchronized void setFiles(List<TextTableFile> files) {
            this.files.clear();
            this.files.addAll(files);
        }
    }

    /**
     * 清单文件的输出流（必须支持多线程同步）
     */
    static class ListfileWriter extends BufferedLineWriter {

        public ListfileWriter(File file, String charsetName, int cache) throws IOException {
            super(file, charsetName, cache);
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
    }

    /**
     * 文件记录类
     */
    protected static class FileRecord implements TextTableLine {
        private String line;
        private String lineSeparator;
        private String[] fields;
        private int column;

        /**
         * 从输入流中读取当前行内容
         *
         * @param line 行信息
         */
        public FileRecord(TextTableLine line) {
            this.column = line.getColumn();
            this.line = line.getContent();
            this.lineSeparator = line.getLineSeparator();
            this.fields = new String[this.column + 1];
            for (int i = 1; i <= this.column; i++) {
                this.fields[i] = line.getColumn(i);
            }
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
            throw new UnsupportedOperationException();
        }

    }

}
