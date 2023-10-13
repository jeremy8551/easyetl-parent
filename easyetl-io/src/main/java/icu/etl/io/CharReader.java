package icu.etl.io;

import java.io.IOException;
import java.io.Reader;

/**
 * 源为字符序列的字符流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-10
 */
public class CharReader extends Reader {

    /** 字符序列对象 */
    private CharSequence source;

    /** 上一次读取字符的位置信息 */
    private int next;

    /** 读取字符序列的最大长度 */
    private int length;

    /** 标记位置信息 */
    private int mark;

    /**
     * 创建字符序列读取器
     *
     * @param source 字符序列信息
     * @param off    从字符序列开始读取字符的位置信息
     * @param len    从字符序列读取字符的最大长度
     */
    public CharReader(CharSequence source, int off, int len) {
        if (source == null) {
            source = "";
        }
        if (off < 0 || len < 0 || (off + len) > source.length()) {
            throw new IllegalArgumentException(off + ", " + len + ", " + source.length());
        }

        this.source = source;
        this.next = off;
        this.length = len;
        this.mark = 0;
    }

    /**
     * 创建字符序列读取器
     *
     * @param source 字符序列信息
     */
    public CharReader(CharSequence source) {
        this(source, 0, (source == null ? 0 : source.length()));
    }

    /**
     * 检查以确保流尚未关闭
     *
     * @throws IOException
     */
    private void ensureOpen() throws IOException {
        if (this.source == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * 读取单个字符。
     *
     * @return 读取的字符，如果已到达流的结尾，则为-1
     * @throws IOException
     */
    public int read() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if (this.next >= this.length || this.next >= this.source.length()) {
                return -1;
            } else {
                return this.source.charAt(this.next++);
            }
        }
    }

    /**
     * 将字符写入字符数组的指定位置上
     *
     * @param cbuf 目标缓冲区
     * @param off  开始写入字符的偏移量
     * @param len  要读取的最大字符数
     * @return 读取的字符数，如果已到达流的结尾，则为-1
     * @throws IOException
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            // 如果超长
            if (this.next >= this.length) {
                return -1;
            }

            // 从 next 开始复制字符到 next + n 位置结束，复制到 cbuf 数组的 off 位置上
            int n = Math.min(this.length - this.next, len);
            for (int i = this.next, size = this.next + n, index = off; i < size && i < this.source.length(); i++) {
                cbuf[index++] = this.source.charAt(i);
            }

            this.next += n;
            return n;
        }
    }

    /**
     * 跳过流中指定的字符数。返回跳过的字符数。 <br>
     * len 参数可能为负值，即使在本例中读卡器超类的跳过方法抛出异常。len 的负值会导致流向后跳过。负返回值表示向后跳过。不可能向后跳过字符串的开头。 <br>
     * 如果整个字符串已被读取或跳过，则此方法无效，并且始终返回0。 <br>
     *
     * @throws IOException
     */
    public long skip(long len) throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if (this.next >= this.length) {
                return 0;
            }

            // 按源的开头和结尾绑定跳过
            long n = Math.min(this.length - this.next, len);
            n = Math.max(-this.next, n);
            this.next += n;
            return n;
        }
    }

    /**
     * 判断流是否准备好读取。
     *
     * @return 返回 true 表示保证下一次执行 {@linkplain #read()} 不阻塞输入
     * @throws IOException
     */
    public boolean ready() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            return true;
        }
    }

    /**
     * 告诉此流是否支持 {@linkplain #mark(int)} 操作，它确实支持。
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * 标记流中的当前位置。接下来可以调用 {@linkplain #reset()} 将流重新定位到此点。
     *
     * @param readAheadLimit 限制在保留标记的同时可以读取的字符数。因为流的输入来自字符串，所以没有实际的限制，所以此参数不能为负，否则将被忽略。
     * @throws IOException
     */
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException(String.valueOf(readAheadLimit));
        }

        synchronized (this.lock) {
            this.ensureOpen();
            this.mark = this.next;
        }
    }

    /**
     * 将流重置为最近的标记，如果从未标记，则重置为字符串的开头。
     *
     * @throws IOException
     */
    public void reset() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            this.next = this.mark;
        }
    }

    public void close() {
        this.source = null;
    }
}
