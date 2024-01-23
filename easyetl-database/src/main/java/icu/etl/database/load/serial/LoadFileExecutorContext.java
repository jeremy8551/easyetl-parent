package icu.etl.database.load.serial;

import icu.etl.database.load.LoadFileRange;
import icu.etl.io.TextTableFile;
import icu.etl.util.Ensure;
import icu.etl.util.IO;

/**
 * 数据文件片段信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-09
 */
public class LoadFileExecutorContext {

    /** 数据文件 */
    private TextTableFile file;

    /** 输入流起始位置 */
    private long pointer;

    /** 输入流读取最大字节总数 */
    private long length;

    /** 输入流缓冲区长度，单位字符 */
    private int readBuffer;

    /**
     * 初始化
     */
    public LoadFileExecutorContext() {
        this.readBuffer = IO.FILE_BYTES_BUFFER_SIZE;
    }

    /**
     * 返回表格型文件
     *
     * @return 表格型文件
     */
    public TextTableFile getFile() {
        return file;
    }

    /**
     * 设置表格型文件
     *
     * @param file 表格型文件
     */
    public void setFile(TextTableFile file) {
        this.file = Ensure.notNull(file);
    }

    /**
     * 返回从输入流开始读取字节的位置
     *
     * @return 位置信息，从0开始
     */
    public long getStartPointer() {
        return pointer;
    }

    /**
     * 设置文件扫描范围
     *
     * @param range 范围信息
     */
    public void setRange(LoadFileRange range) {
        Ensure.notNull(range);
        this.pointer = range.getStart();
        this.length = range.getEnd() - range.getStart();
    }

    /**
     * 返回输入流读取的最大字节总数
     *
     * @return 最多字节总数
     */
    public long length() {
        return length;
    }

    /**
     * 返回输入流中缓冲区长度，单位：字符
     *
     * @return 缓冲区长度，单位：字符
     */
    public int getReadBuffer() {
        return readBuffer;
    }

    /**
     * 设置输入流的缓冲区长度，单位：字符
     *
     * @param size 缓冲区长度，单位：字符
     */
    public void setReadBuffer(int size) {
        this.readBuffer = Ensure.isFromOne(size);
    }

}
