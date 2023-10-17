package icu.etl.io;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 按文本行读取文件
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-27
 */
public class BufferedLineReader extends Reader implements TextFileReader, Iterator<String> {

    /** 输入流缓冲区的默认长度（单位：字符） */
    public static int defaultCharBufferSize = 8192;

    /** 使用 {@linkplain #readLine()} 方法读取字符串时，字符串的初始长度（单位：字符） */
    public static int defaultExpectedLineCapacity = 80;

    /** 表示标记位置 {@linkplain #markedChar} 失效（即当前位置信息 {@linkplain #nextChar} 与标志位置 {@linkplain #markedChar} 之间的字符数超过 {@linkplain #readAheadLimit} 限制时，会将 {@linkplain #markedChar} = {@linkplain #INVALIDATED}） */
    public final static int INVALIDATED = -2;

    /** 表示当前输入流未执行标记 {@linkplain #mark(int)} 方法 */
    public final static int UNMARKED = -1;

    /** 输入流 */
    protected Reader in;

    /** 缓冲区 */
    protected char buffer[];

    /** 缓冲区有效的长度 */
    protected int count;

    /** 当前字符位置 */
    protected int nextChar;

    /** true 表示上一个字符是 \r 如果下一个字符是 \n 的话需要忽略（LF 等于 \n） */
    protected boolean skipLF;

    /**
     * 限制在保留标记的同时可以读取的字符数。<br>
     * 在读取字符达到或超过此限制后，尝试重置流可能会失败。<br>
     * 大于输入缓冲区大小的限制值将导致分配一个不小于限制的新缓冲区。<br>
     * 因此，应小心使用大值。<br>
     */
    protected int readAheadLimit = 0;

    /** 标记位置时保留的状态 */
    protected boolean markedSkipLF;

    /** 标记位置时保留的行号 */
    private long markedLineNumber;

    /** 标记位置时保留的字符位置 */
    protected int markedChar = UNMARKED;

    /** 上一次执行 {@linkplain #readLine()} 方法所读取的换行符，等于空字符串表示不存在行间分隔符 */
    protected String lineSeparator;

    /** 行号 */
    protected long lineNumber;

    /** 每行文本的默认初始长度 */
    protected int expectedLineLength;

    /** 越过指定字符个数时所使用的缓冲区 */
    protected char skipBuffer[] = null;

    /** 迭代器模式使用的缓存 */
    protected volatile String nextline;

    /**
     * 初始化
     *
     * @param in             输入流
     * @param size           设置缓冲区长度（单位字符）
     * @param expectedLength 每行文本的默认初始长度
     */
    public BufferedLineReader(Reader in, int size, int expectedLength) {
        this.open(in, size, expectedLength);
    }

    /**
     * 初始化
     *
     * @param in   输入流
     * @param size 设置缓冲区长度（单位字符）
     */
    public BufferedLineReader(Reader in, int size) {
        this.open(in, size, 0);
    }

    /**
     * 初始化
     *
     * @param in 输入流
     */
    public BufferedLineReader(Reader in) {
        this.open(in, 0, 0);
    }

    /**
     * 初始化
     *
     * @param str            字符串内容
     * @param size           缓冲区长度
     * @param expectedLength 每行字符串的初始大小
     */
    public BufferedLineReader(CharSequence str, int size, int expectedLength) {
        if (str instanceof String) {
            this.open(new StringReader(str.toString()), size, expectedLength);
        } else {
            this.open(new CharArrayReader(str.toString().toCharArray()), size, expectedLength);
        }
    }

    /**
     * 初始化
     *
     * @param str
     * @param size
     */
    public BufferedLineReader(CharSequence str, int size) {
        this(str, size, 0);
    }

    /**
     * 初始化
     *
     * @param str
     */
    public BufferedLineReader(CharSequence str) {
        this(str, 0, 0);
    }

    /**
     * 初始化
     *
     * @param file           文件
     * @param charsetName    文件字符集，为null时默认取 file.encoding 的属性值
     * @param size           缓冲区长度（单位：字符），小于等于零时会使用默认值
     * @param expectedLength 每行字符串的初始容量长度（单位：字符），小于等于零时会使用默认值
     */
    public BufferedLineReader(File file, String charsetName, int size, int expectedLength) {
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, charsetName);
            this.open(isr, size, expectedLength);
        } catch (Throwable e) {
            throw new RuntimeException(StringUtils.toString(file), e);
        }
    }

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @param size        缓冲区长度（单位：字符），小于等于零时会使用默认值
     */
    public BufferedLineReader(File file, String charsetName, int size) {
        this(file, charsetName, size, 0);
    }

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     */
    public BufferedLineReader(File file, String charsetName) {
        this(file, charsetName, 0, 0);
    }

    /**
     * 初始化
     *
     * @param in             输入流
     * @param size           缓冲区长度（单位：字符），小于等于零时会使用默认值
     * @param expectedLength 每行字符串的初始容量长度（单位：字符），小于等于零时会使用默认值
     */
    protected void open(Reader in, int size, int expectedLength) {
        if (in == null) {
            throw new NullPointerException();
        }

        this.in = in;
        this.buffer = new char[size <= 0 ? defaultCharBufferSize : size];
        this.nextChar = this.count = 0;
        this.expectedLineLength = (expectedLength <= 0) ? defaultExpectedLineCapacity : expectedLength;
        this.markedSkipLF = false;
        this.skipLF = false;
        this.lineSeparator = "";
        this.nextline = null;
    }

    /**
     * 设置每行字符串的初始容量
     *
     * @param length 初始容量（单位: 字符, 如果初始容量过小则每次执行 {@linkplain #readLine()} 方法时自动扩容会消耗大量资源）
     * @throws IllegalArgumentException 如果 {@code length} {@literal < } 0
     */
    public void setReadLineCapacity(int length) {
        if (length < 0) {
            throw new IllegalArgumentException(String.valueOf(length));
        } else {
            this.expectedLineLength = length;
        }
    }

    /**
     * 检查输入流是否关闭
     */
    protected void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException(this.getClass().getSimpleName() + " closed");
        }
    }

    public int read() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            for (; ; ) {
                if (this.nextChar >= this.count) {
                    this.fill();
                    if (this.nextChar >= this.count) {
                        return -1;
                    }
                }

                if (this.skipLF) {
                    this.skipLF = false;
                    if (this.buffer[this.nextChar] == '\n') {
                        this.nextChar++;
                        continue;
                    }
                }

                // 读取一个字符
                int c = this.buffer[this.nextChar++];
                switch (c) {
                    case '\n':
                        this.lineNumber++;
                        this.lineSeparator = "\n";
                        continue;

                    case '\r':
                        this.lineNumber++;
                        this.skipLF = true;
                        if (this.nextChar >= this.count) {
                            this.fill();
                        }
                        if (this.nextChar < this.count && this.buffer[this.nextChar] == '\n') {
                            this.lineSeparator = "\r\n";
                        } else {
                            this.lineSeparator = "\r";
                        }
                        continue;
                }
                return c;
            }
        }
    }

    /**
     * 填充缓冲区，如果标记有效，则将其考虑在内。
     */
    protected void fill() throws IOException {
        int dst;
        if (this.markedChar <= UNMARKED) {
            // 当前未发生标记
            dst = 0;
        } else {
            /* 当前存在标记 */
            int len = this.nextChar - this.markedChar; // 当前位置与标记位置之间的间隔字符是否超过限制
            if (len >= this.readAheadLimit) { // 已经超过限制, 则设置标记失效
                this.markedChar = INVALIDATED;
                this.readAheadLimit = 0;
                dst = 0;
            } else {
                if (this.readAheadLimit <= this.buffer.length) {
                    // 将缓冲区数据重新洗牌
                    System.arraycopy(this.buffer, this.markedChar, this.buffer, 0, len);
                    this.markedChar = 0;
                    dst = len;
                } else {
                    // 重新分配缓冲区以适应预读限制
                    char[] array = new char[this.readAheadLimit];
                    System.arraycopy(this.buffer, this.markedChar, array, 0, len);
                    this.buffer = array;
                    this.markedChar = 0;
                    dst = len;
                }
                this.nextChar = this.count = len;
            }
        }

        int len;
        do {
            len = this.in.read(this.buffer, dst, this.buffer.length - dst);
        } while (len == 0); // 等待阻塞进程

        if (len > 0) {
            this.count = dst + len;
            this.nextChar = dst;
        }
    }

    public int read(char[] array, int off, int len) throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if ((off < 0) || (off > array.length) || (len < 0) || ((off + len) > array.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            // 读取字符到数组 array 中
            int size = this.readBuffer(array, off, len);
            if (size <= 0) {
                return size;
            }

            // 如果读取的字符不够，则等待阻塞输入流可以读取后再次读取
            while ((size < len) && this.in.ready()) {
                int read = this.readBuffer(array, off + size, len - size);
                if (read <= 0) {
                    break;
                }
                size += read;
            }

            // 查询数组中的行间分隔符
            for (int i = off, limit = off + size; i < limit; i++) {
                int c = array[i];

                if (this.skipLF) {
                    this.skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }

                switch (c) {
                    case '\n':
                        this.lineNumber++;
                        this.lineSeparator = "\n";
                        continue;

                    case '\r':
                        this.lineNumber++;
                        this.skipLF = true;

                        if ((i + 1) >= limit) {
                            this.fill();
                        }
                        if (this.nextChar < this.count && this.buffer[this.nextChar] == '\n') {
                            this.lineSeparator = "\r\n";
                        } else {
                            this.lineSeparator = "\r";
                        }
                        continue;
                }
            }

            return size;
        }
    }

    /**
     * 从缓冲区中读取字符到参数 array 中，如果缓冲区中字符不够则从输入流中读取新的字符到缓冲区，再从缓冲区中读取字符写入到字符数组参数 array 中
     *
     * @param array 字符数组
     * @param off   起始位置
     * @param len   字符长度
     * @return
     * @throws IOException
     */
    protected int readBuffer(char[] array, int off, int len) throws IOException {
        if (this.nextChar >= this.count) {
            // 如果请求的长度至少与缓冲区一样大，并且没有标记/重置活动，并且没有跳过换行符，则不要费心将字符复制到本地缓冲区。这样缓冲流将无害地级联。
            if (len >= this.buffer.length && this.markedChar <= UNMARKED && !this.skipLF) {
                return this.in.read(array, off, len);
            }
            this.fill();
        }

        if (this.nextChar >= this.count) {
            return -1;
        }

        if (this.skipLF) {
            this.skipLF = false;
            if (this.buffer[this.nextChar] == '\n') {
                this.nextChar++;
                if (this.nextChar >= this.count) {
                    this.fill();
                }
                if (this.nextChar >= this.count) {
                    return -1;
                }
            }
        }

        int size = Math.min(len, this.count - this.nextChar);
        System.arraycopy(this.buffer, this.nextChar, array, off, size);
        this.nextChar += size;
        return size;
    }

    /**
     * 如果迭代包含更多行内容，则返回true (即下一次执行 {@linkplain #next()} 方法返回行内容而不是 null 值，则返回true。）
     */
    public boolean hasNext() {
        synchronized (this.lock) {
            if (this.nextline == null) {
                if (this.isClosed()) {
                    return false;
                }

                String line = this.readLineEasy();
                if (line == null) {
                    if (!this.isClosed()) {
                        IO.close(this);
                    }
                    return false;
                } else {
                    this.nextline = line;
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    /**
     * 返回迭代的下一行内容
     *
     * @return 返回 null 表示输入流当前已是 EOF 状态, 同时迭代器会执行 {@linkplain #close()} 方法关闭当前输入流（即：迭代器默认下无需手动执行 {@linkplain #close()} 方法）
     */
    public String next() {
        if (this.nextline == null) {
            if (this.hasNext()) {
                return this.next();
            } else {
                if (!this.isClosed()) {
                    IO.close(this);
                }
                return null;
            }
        } else {
            String line = this.nextline;
            this.nextline = null;
            return line;
        }
    }

    public String readLine() throws IOException {
        return this.readLine(false);
    }

    /**
     * 读取下一行内容, 不需要显示的抛出异常信息
     *
     * @return
     */
    public String readLineEasy() {
        try {
            return this.readLine();
        } catch (IOException e) {
            throw new RuntimeException("readLine()", e);
        }
    }

    /**
     * 从输入流当前位置开始读取一行字符串
     *
     * @param ignoreLF true表示忽略 \r 字符后面的 \n 字符
     * @return
     * @throws IOException
     */
    protected String readLine(boolean ignoreLF) throws IOException {
        StringBuilder line = null;
        this.lineSeparator = ""; // 读取行之前先删除换行符

        synchronized (this.lock) {
            this.ensureOpen();
            boolean omitLF = ignoreLF || this.skipLF;
            int incOffset = 1;

            for (; ; ) {
                // 判断是否需要从输入流中读取字符到缓冲区
                if (this.nextChar >= this.count) {
                    this.fill();
                }

                // 判断是否已读取到行末
                if (this.nextChar >= this.count) {
                    if (line != null && line.length() > 0) { // 已读取到最后一行
                        this.lineNumber++;
                        return line.toString();
                    } else { // 已读取到行末
                        return null;
                    }
                }

                // 跳过剩余的换行符
                if (omitLF && (this.buffer[this.nextChar] == '\n')) {
                    this.nextChar++;
                }
                this.skipLF = false;
                omitLF = false;

                // 从缓冲区中当前字符位置开始搜索换行符
                int i;
                char c = 0;
                boolean eol = false;
                for (i = this.nextChar; i < this.count; ) {
                    c = this.buffer[i];

                    if (c == '\n') {
                        this.lineNumber++;
                        this.lineSeparator = "\n";
                        eol = true;
                        break;
                    } else if (c == '\r') {
                        this.lineNumber++;
                        int next = i + 1;
                        if (next < this.count) { // 下一个字符是 \n
                            if (this.buffer[next] == '\n') {
                                this.lineSeparator = "\r\n";
                                incOffset = 2;
                            } else {
                                this.lineSeparator = "\r";
                            }
                        } else {
                            this.lineSeparator = "\r";
                            omitLF = true;
                        }
                        eol = true;
                        break;
                    } else {
                        i++;
                    }
                }

                int startChar = this.nextChar; // 当前行字符串的起始位置
                this.nextChar = i; // 换行符位置

                // 已读取一行字符串
                if (eol) {
                    String str;
                    if (line == null) {
                        str = new String(this.buffer, startChar, i - startChar);
                    } else {
                        line.append(this.buffer, startChar, i - startChar);
                        str = line.toString();
                    }

                    this.nextChar += incOffset; // 将当前位置移动到换行符的下一个字符
                    if (omitLF) { // 表示已读取到 \r 字符且缓冲区中无字符可读，需要从输入流中读取部分字符
                        omitLF = false;
                        this.fill();
                        if (this.nextChar < this.count && this.buffer[this.nextChar] == '\n') {
                            this.nextChar++;
                            this.lineSeparator = "\r\n";
                        }
                    }
                    return str;
                }

                if (line == null) {
                    line = new StringBuilder(this.expectedLineLength);
                }
                line.append(this.buffer, startChar, i - startChar);
            }
        }
    }

    /**
     * 从输入流的当前位置开始读取 {@code n} 个字符
     *
     * @param n 字符个数
     * @return 实际跳过的字符数
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }

        int size = (int) Math.min(n, 8196); // 缓冲区大小不能超过 8196 个字节
        synchronized (this.lock) {
            if (this.skipBuffer == null || this.skipBuffer.length < size) {
                this.skipBuffer = new char[size];
            }

            int lastSkip = -1; // 最后一次读取的字符数
            long total = n; // 当前还需要越过的字节总数
            while (total > 0) {
                int len = (int) Math.min(total, size); // 本次读取的字节总数
                int skip = this.read(this.skipBuffer, 0, len);
                if (skip == -1) {
                    break;
                } else {
                    total -= skip; // 剩余需要越过的字节总数 = 当前剩余字节总数 - 本次越过的字节总数
                    lastSkip = skip;
                }
            }

            // 如果最后一个字符是非换行字符，需要将行号自增一
            if (lastSkip >= 0 && this.skipBuffer.length > 0) {
                char lc = this.skipBuffer[lastSkip - 1];
                if (lc != '\n' && lc != '\r') {
                    this.lineNumber++;
                }
            }

            return n - total;
        }
    }

    /**
     * 告诉此流是否已准备好被读取
     */
    public boolean ready() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if (this.skipLF) { // 如果需要跳过换行符，而下一个要读取的字符是换行符，那么就直接跳过它。
                if (this.nextChar >= this.count && this.in.ready()) { // 请注意in.ready（）如果并且仅当流上的下一次读取不会阻塞时，才会返回true
                    this.fill();
                }

                if (this.nextChar < this.count) {
                    if (this.buffer[this.nextChar] == '\n') {
                        this.nextChar++;
                    }
                    this.skipLF = false;
                }
            }
            return (this.nextChar < this.count) || this.in.ready();
        }
    }

    public boolean markSupported() {
        return true;
    }

    /**
     * 标记输入流的当前位置，之后再执行 {@linkplain #reset()} 方法时，会尝试将输入流重新定位到此点
     *
     * @param limit 限制在保留标记的同时可以读取的字符数。<br>
     *              在读取字符达到或超过此限制后，尝试重置流可能会失败。<br>
     *              大于输入缓冲区大小的限制值将导致分配一个不小于限制的新缓冲区。<br>
     *              因此，应小心使用大值。
     */
    public void mark(int limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException(String.valueOf(limit));
        }

        synchronized (this.lock) {
            this.ensureOpen();
            this.readAheadLimit = limit;
            this.markedChar = this.nextChar;
            this.markedSkipLF = this.skipLF;
            this.markedLineNumber = this.lineNumber;
        }
    }

    /**
     * 将输入流恢复到 {@linkplain #mark(int)} 方法标记时的状态
     */
    public void reset() throws IOException {
        synchronized (this.lock) {
            this.ensureOpen();
            if (this.markedChar < 0) {
                throw new IOException((this.markedChar == INVALIDATED) ? "Mark invalid" : "Stream not marked");
            }

            this.lineNumber = this.markedLineNumber;
            this.nextChar = this.markedChar;
            this.skipLF = this.markedSkipLF;
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            try {
                if (this.in != null) {
                    this.in.close();
                }
            } finally {
                this.in = null;
                this.buffer = null;
                this.skipBuffer = null;
                this.nextline = null;
            }
        }
    }

    /**
     * 返回 true 表示输入流已关闭， 即已执行 {@linkplain #close()} 方法
     *
     * @return
     */
    public boolean isClosed() {
        return this.in == null;
    }

    /**
     * 设置当前行号
     *
     * @param n
     */
    public void setLineNumber(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        } else {
            this.lineNumber = n;
        }
    }

    public long getLineNumber() {
        return this.lineNumber;
    }

    public String getLineSeparator() {
        return this.lineSeparator;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}