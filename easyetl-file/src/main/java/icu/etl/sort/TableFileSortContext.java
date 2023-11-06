package icu.etl.sort;

import java.io.File;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.io.TextTableFile;
import icu.etl.util.IO;

/**
 * 表格文件排序的配置信息
 *
 * @author jeremy8551@qq.com
 */
public class TableFileSortContext {

    /** 表格型文件 */
    private TextTableFile file;

    /** 写文件时缓存文件行数 */
    private int cacheRows;

    /** 文件输入流缓冲区长度，单位：字符 */
    private int readerBuffer;

    /** 临时文件中最大行数 */
    private int maxRows;

    /** true表示排序结束后删除临时文件 */
    private boolean deleteFile;

    /** 每个合并临时文件的线程中临时文件的最大个数 */
    private int fileCount;

    /** 合并文件任务同时运行的最大任务数 */
    private int threadNumber;

    /** 排序过程中已读文件行数 */
    private long readLineNumber;

    /** 排序过程中合并文件行数 */
    private long mergeLineNumber;

    /** true 表示保留源文件 false 表示覆盖源文件内容 */
    private boolean keepSource;

    /** 临时文件存储目录 */
    private File tempDir;

    /** 其他属性信息集合 */
    protected CaseSensitivMap<Object> values;

    /**
     * 创建一个表格文件排序配置信息
     */
    public TableFileSortContext() {
        this.deleteFile = true;
        this.maxRows = 10000;
        this.cacheRows = 100;
        this.threadNumber = 3;
        this.fileCount = 4;
        this.mergeLineNumber = 0;
        this.readLineNumber = 0;
        this.keepSource = false;
        this.readerBuffer = IO.FILE_BYTES_BUFFER_SIZE;
        this.values = new CaseSensitivMap<Object>();
    }

    /**
     * 设置输出流缓存行数
     *
     * @param n 缓存行数
     */
    public void setWriterBuffer(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        this.cacheRows = n;
    }

    /**
     * 返回输出流缓存行数
     *
     * @return 缓存行数
     */
    public int getWriterBuffer() {
        return this.cacheRows;
    }

    /**
     * 返回文件输入流的缓冲区长度，单位：字符
     *
     * @return
     */
    public int getReaderBuffer() {
        return readerBuffer;
    }

    /**
     * 设置文件输入流的缓冲区长度，单位：字符
     *
     * @param n
     */
    public void setReaderBuffer(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        this.readerBuffer = n;
    }

    /**
     * 设置临时文件最大记录数
     *
     * @param n
     */
    public void setMaxRows(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        this.maxRows = n;
    }

    /**
     * 返回临时文件最大记录数
     *
     * @return
     */
    public int getMaxRows() {
        return this.maxRows;
    }

    /**
     * 返回 true 表示删除临时文件
     *
     * @return
     */
    public boolean isDeleteFile() {
        return this.deleteFile;
    }

    /**
     * 设置 true 表示删除临时文件
     *
     * @param deleteFile
     */
    public void setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
    }

    /**
     * 设置线程合并文件过程中最大文件个数
     *
     * @param n
     */
    public void setFileCount(int n) {
        if (n <= 1) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        this.fileCount = n;
    }

    /**
     * 返回线程合并文件过程中最大文件个数
     *
     * @return
     */
    public int getFileCount() {
        return fileCount;
    }

    /**
     * 设置排序过程中的并发线程数
     *
     * @param n 线程数
     */
    public void setThreadNumber(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        this.threadNumber = n;
    }

    /**
     * 返回排序过程中的并发线程数
     *
     * @return 线程数
     */
    public int getThreadNumber() {
        return threadNumber;
    }

    /**
     * 返回排序文件
     *
     * @return 排序文件
     */
    protected TextTableFile getFile() {
        return file;
    }

    /**
     * 设置排序文件
     *
     * @param file 排序文件
     */
    protected void setFile(TextTableFile file) {
        this.file = file;
    }

    /**
     * 返回临时文件存储目录
     *
     * @return
     */
    public File getTempDir() {
        return tempDir;
    }

    /**
     * 设置临时文件存储的目录
     *
     * @param tempDir
     */
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * 返回排序过程中已读文件行数
     *
     * @return
     */
    public long getReadLineNumber() {
        return this.readLineNumber;
    }

    /**
     * 设置排序过程中已读文件行数
     *
     * @param beforeLineNumber
     */
    protected void setReadLineNumber(long beforeLineNumber) {
        this.readLineNumber = beforeLineNumber;
    }

    /**
     * 返回排序过程中合并文件行数
     *
     * @return
     */
    public long getMergeLineNumber() {
        return this.mergeLineNumber;
    }

    /**
     * 设置排序过程中合并文件行数
     *
     * @param afterLineNumber
     */
    protected void setMergeLineNumber(long afterLineNumber) {
        this.mergeLineNumber = afterLineNumber;
    }

    /**
     * 返回 true 表示排序操作不影响源文件，排序返回的文件与源文件不同 <br>
     * 返回 false 表示排序操作会覆盖源文件，排序返回的文件与源文件相同
     *
     * @return
     */
    public boolean keepSource() {
        return keepSource;
    }

    /**
     * 设置 true 表示排序操作不影响源文件，排序返回的文件与源文件不同 <br>
     * 设置 false 表示排序操作会覆盖源文件，排序返回的文件与源文件相同
     *
     * @param b
     */
    public void setKeepSource(boolean b) {
        this.keepSource = b;
    }

    /**
     * 判断缓存中是否存在属性
     *
     * @param key 属性名
     * @return
     */
    protected boolean contains(String key) {
        return this.values.containsKey(key);
    }

    /**
     * 返回属性值
     *
     * @param key 属性名
     * @param <E>
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    protected <E> E getAttribute(String key) {
        return (E) this.values.get(key);
    }

    /**
     * 设置属性
     *
     * @param key   属性名
     * @param value 属性值
     */
    protected void setAttribute(String key, Object value) {
        this.values.put(key, value);
    }

}
