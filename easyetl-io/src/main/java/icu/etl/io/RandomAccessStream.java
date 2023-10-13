package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 按行读取文件字节信息，如果输入流当前位置在第一行不全，则自动从下一行内容开始读取
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-08
 */
public class RandomAccessStream extends InputStream {

    /** 输入流 */
    protected RandomAccessFile in;

    /** 从输入流中读取的最多字节总数 */
    protected long size;

    /** true 表示上一个字符是 \r 如果下一个字符是 \n 的话需要忽略（LF 等于 \n） */
    protected boolean skipLF;

    /** true表示输入流结束 */
    protected boolean EOF;

    /** 标记位置时保留的输入流的位置信息 */
    protected long markedPointer;

    /** 标记位置时保留的可获取字节个数 */
    protected long markedSize;

    /** 标记位置时保留的状态 */
    protected boolean markedSkipLF;

    /** 标记位置时保留的状态 */
    protected boolean markedEOF;

    /** 文件输入流实际开始读取字节的位置，从 0 开始 */
    protected long startPointer;

    /**
     * 创建一个随机访问的输入流
     *
     * @param in     随机输入流
     * @param length 从输入流中最多能读取的字节总数
     * @throws IOException
     */
    public RandomAccessStream(RandomAccessFile in, long length) throws IOException {
        super();
        this.open(in, length);
    }

    /**
     * 创建一个文件随机访问的输入流，从指定位置开始读取字节流，最多读取 length 个字节
     *
     * @param file   文件
     * @param offset 文件位置
     * @param length 读取的最多字节总数
     * @throws IOException
     */
    public RandomAccessStream(File file, long offset, long length) throws IOException {
        super();
        RandomAccessFile in = new RandomAccessFile(file, "r");
        in.seek(offset);
        this.open(in, length);
    }

    /**
     * 打开输入流
     *
     * @param in     文件随机输入流
     * @param length 读取最大字节数
     * @throws IOException
     */
    protected void open(RandomAccessFile in, long length) throws IOException {
        this.in = in;
        this.size = length;
        this.skipLF = false;
        this.EOF = false;
        this.mark(); // 保存位置信息
        this.start(this.markedPointer);
    }

    /**
     * 搜索首行的起始位置
     *
     * @throws IOException
     */
    protected void start(long pointer) throws IOException {
        if (pointer <= 0) { // 文件中第一个字节
            return;
        }

        int first = this.in.read(); // 起始位置上的字节
        if (first == -1) {
            this.reset();
            return;
        }

        this.in.seek(pointer - 1); // 读取第一个位置左侧的字节
        int left = this.in.read(); // 起始位置左侧的字节
        if (left == '\n') { // 读取的第一个字节正好是行的起始位置
            this.reset();
            return;
        } else if (left == '\r') {
            if (first == '\n') {
                this.mark(); // 标记当前位置是起始位置
            } else {
                this.reset(); // 第一个位置正好是行的起始位置
            }
            return;
        } else {
            // 从输入流中读取第一行的行末位置
            byte[] array = new byte[8192];
            int read = 0, count = 0;
            while ((read = this.in.read(array, 0, array.length)) != -1) {
                for (int i = 0; i < read; i++) {
                    byte b = array[i];
                    count++;

                    if (b == '\r') {
                        int next = i + 1;
                        if (next < array.length) {
                            if (array[next] == '\n') {
                                count++;
                            }
                        } else if (this.in.read() == '\n') {
                            count++;
                        }
                        this.reset(count);
                        return;
                    } else if (b == '\n') {
                        this.reset(count);
                        return;
                    }
                }
            }

            // 一直读取到文件结束位置都没有找到换行符时
            this.EOF = true;
            return;
        }
    }

    public synchronized int read() throws IOException {
        byte[] value = new byte[1];
        return this.read(value, 0, 1) == -1 ? -1 : value[0];
    }

    public synchronized int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public synchronized int read(byte[] array, int off, int len) throws IOException {
        if (this.EOF) {
            return -1;
        } else if (this.size == 0) { // 已读取了指定字节数
            if (this.skipLF) { // 上一个字符是 \r
                this.skipLF = false;

                // 如果读取0个字节则继续等待
                int read = 0;
                while ((read = this.in.read(array, off, 1)) == 0) {
                    // 阻塞输入流
                }

                if (read == -1) {
                    this.EOF = true;
                    return -1;
                } else if (array[off] == '\n') {
                    this.EOF = true;
                    return 1;
                } else {
                    this.EOF = true;
                    return -1;
                }
            } else { // 继续向下读取字节直到行末的最后一个字符
                int read = 0;
                while ((read = this.in.read(array, off, len)) == 0) {
                    // 阻塞输入流
                }

                if (read == -1) { // 已读取到输入流结束位置
                    this.EOF = true;
                    return -1;
                }

                for (int i = off; i < read; i++) {
                    byte b = array[i];
                    if (b == '\r') {
                        int next = i + 1;
                        if (next < read) {
                            if (array[next] == '\n') {
                                this.EOF = true;
                                read = next + 1; // 读取字节数
                                return read;
                            } else {
                                this.EOF = true;
                                return next;
                            }
                        } else {
                            this.skipLF = true;
                        }
                    } else if (b == '\n') {
                        this.EOF = true;
                        read = i + 1; // 读取字节数
                        return read;
                    }
                }

                // 未读取到换行符的情况
                return read;
            }
        } else {
            int read = 0;
            while ((read = this.in.read(array, off, Math.min(len, (int) this.size))) == 0) {
                // 阻塞输入流
            }

            // 输入流结束
            if (read == -1) {
                this.EOF = true;
                return -1;
            }

            this.size -= read; // 可读字节总数 减 本次读取字节总数
            if (this.size == 0) { // 达到读取字节的最大数
                int last = read - 1;
                byte b = array[last]; // 最右端的字节
                if (b == '\n') { // 最后一个字段就是换行符的情况
                    this.EOF = true;
                } else if (b == '\r') { // 最后一个字符是 \r
                    this.skipLF = true;
                }
            }
            return read;
        }
    }

    public synchronized long skip(long n) throws IOException {
        if (this.size == 0) {
            return 0;
        } else {
            int size = (int) Math.min(this.size, n);
            int skip = this.in.skipBytes(size);
            if (skip > 0) {
                this.size -= skip;
            }
            return skip;
        }
    }

    public synchronized int available() throws IOException {
        return (int) this.size;
    }

    /**
     * 标记起始位置信息
     *
     * @throws IOException
     */
    protected synchronized void mark() throws IOException {
        this.mark(0);
        this.startPointer = this.markedPointer;
    }

    public synchronized void mark(int limit) {
        try {
            this.markedPointer = this.in.getFilePointer();
        } catch (Exception e) {
            throw new RuntimeException("mark stream error!", e);
        }
        this.markedSize = this.size;
        this.markedSkipLF = this.skipLF;
        this.markedEOF = this.EOF;
    }

    /**
     * 设置输入流起始状态
     *
     * @param count 字节总数
     * @throws IOException
     */
    protected synchronized void reset(int count) throws IOException {
        this.markedPointer += count;
        this.startPointer = this.markedPointer;
        this.reset();
    }

    public synchronized void reset() throws IOException {
        this.in.seek(this.markedPointer);
        this.size = this.markedSize;
        this.skipLF = this.markedSkipLF;
        this.EOF = this.markedEOF;
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
    }

    /**
     * 返回输入流实际开始读取字节的位置，从 0 开始
     *
     * @return
     */
    public long getStartPointer() {
        return startPointer;
    }

}
