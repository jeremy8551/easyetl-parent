package icu.etl.database.load.parallel;

import icu.etl.database.load.LoadFileRange;
import icu.etl.io.TextTableFile;
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
     * @return
     */
    public TextTableFile getFile() {
        return file;
    }

    /**
     * 设置表格型文件
     *
     * @param file
     */
    public void setFile(TextTableFile file) {
        if (file == null) {
            throw new NullPointerException();
        } else {
            this.file = file;
        }
    }

    /**
     * 返回从输入流开始读取字节的位置
     *
     * @return
     */
    public long getStartPointer() {
        return pointer;
    }

    /**
     * 设置文件扫描范围
     *
     * @param range
     */
    public void setRange(LoadFileRange range) {
        if (range == null) {
            throw new NullPointerException();
        } else {
            this.pointer = range.getStart();
            this.length = range.getEnd() - range.getStart();
        }
    }

    /**
     * 返回输入流读取的最大字节总数
     *
     * @return
     */
    public long length() {
        return length;
    }

    /**
     * 返回输入流中缓冲区长度，单位：字符
     *
     * @return
     */
    public int getReadBuffer() {
        return readBuffer;
    }

    /**
     * 设置输入流的缓冲区长度，单位：字符
     *
     * @param size
     */
    public void setReadBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(String.valueOf(size));
        } else {
            this.readBuffer = size;
        }
    }

}
