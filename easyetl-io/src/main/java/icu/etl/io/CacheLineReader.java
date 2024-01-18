package icu.etl.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 表格型文件的输入流（可缓存多行内容）
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-01-31
 */
public class CacheLineReader extends Reader implements TextFileReader {

    /** 标准输入流接口 */
    private TextFileReader in;

    /** 字符输入流 */
    private BufferedLineReader reader;

    /** 缓存行的内容 */
    private CacheLine cacher;

    /** true表示已打开数据文件 */
    private boolean open;

    /**
     * 初始化
     */
    protected CacheLineReader() {
        this.cacher = new CacheLine(this);
        this.in = this.cacher;
        this.open = false;
    }

    /**
     * 打开输入流
     *
     * @param str
     */
    public CacheLineReader(CharSequence str) {
        this();
        this.reader = new BufferedLineReader(str);
        this.in = this.reader;
        this.open = true;
    }

    /**
     * 打开输入流
     *
     * @param in 输入流
     */
    public CacheLineReader(Reader in) {
        this(in, 0);
    }

    /**
     * 打开输入流
     *
     * @param in   输入流
     * @param size 缓冲区长度，单位：字符，小于等于零时会使用默认值
     */
    public CacheLineReader(Reader in, int size) {
        this();
        this.reader = new BufferedLineReader(in, size, 0);
        this.in = this.reader;
        this.open = true;
    }

    /**
     * 打开输入流
     *
     * @param file        文件
     * @param charsetName 文件的字符集
     * @param size        缓冲区长度，单位：字符
     * @throws IOException
     */
    public CacheLineReader(File file, String charsetName, int size) throws IOException {
        this(new InputStreamReader(new FileInputStream(file), charsetName), size);
    }

    /**
     * 从输入流中读取 chars 个字符
     *
     * @param chars 字符个数
     * @param rows  跳转后的行号,从 1 开始
     * @return 返回null表示跳转成功 第一位表示实际跳转的行数，第二位表示实际跳转的字节数
     * @throws IOException
     */
    public synchronized long[] skip(long chars, long rows) throws IOException {
        long real = this.cacher.skip(chars);
        long left = Math.max(chars - real, 0); // 剩余应跳转的字符总数
        if (left > 0) {
            long skip = this.reader.skip(left);
            long lineNumber = this.reader.getLineNumber();
            long total = skip + real; // 实际读取字符数量
            return (total != chars || lineNumber != rows) ? new long[]{lineNumber, total} : null; // 判断跳转后的字节数与行数是否与预期相等
        } else {
            return (real == chars) ? null : new long[]{this.in.getLineNumber(), real};
        }
    }

    /**
     * 设置起始行号
     *
     * @param n
     */
    public void setLineNumber(long n) {
        this.cacher.setLineNumber(n);
        if (this.reader != null) {
            this.reader.setLineNumber(n);
        }
    }

    /**
     * 是否已打开文件
     *
     * @return true表示已打开数据文件
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * 缓存 {@code n} 行数据
     *
     * @param n 缓存行数（从 1 开始）
     * @throws IOException
     */
    public synchronized int cacheLine(int n) throws IOException {
        if (this.open) {
            return this.cacher.cacheLine(n);
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg034"));
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        if (off < 0 || len < 0) {
            throw new IllegalArgumentException(off + ", " + len);
        }
        if (len == 0) {
            return 0;
        }

        String line = this.readLine();
        int strlen = line.length();
        if (strlen <= len) {
            line.getChars(0, strlen, cbuf, off);
            return strlen;
        } else {
            line.getChars(0, len, cbuf, off);
            this.setCurrentLine(line.substring(len)); // 保存未用的行内容到缓存中
            return len;
        }
    }

    /**
     * 将字符串参数 {@code line} 作为当前行内容保存
     *
     * @param line 字符串
     * @throws IOException
     */
    public synchronized void setCurrentLine(String line) throws IOException {
        if (this.open) {
            if (line == null) {
                throw new NullPointerException();
            } else {
                this.cacher.setCurrentLine(line, null); // 添加到缓存中
            }
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg034"));
        }
    }

    /**
     * 返回缓存区中的行，按行号从小到大排序
     *
     * @return
     * @throws IOException
     */
    public synchronized ArrayList<TextFileLine> getCacheLines() throws IOException {
        if (this.open) {
            return this.cacher.getCacheLine();
        } else {
            throw new IOException(ResourcesUtils.getMessage("io.standard.output.msg034"));
        }
    }

    public String readLine() throws IOException {
        return this.in.readLine();
    }

    public long getLineNumber() {
        return this.in.getLineNumber();
    }

    /**
     * 返回标准输入流接口
     *
     * @return
     */
    protected TextFileReader getReader() {
        return this.in;
    }

    /**
     * 设置标准输入流接口
     *
     * @param reader
     */
    protected void setReader(TextFileReader reader) {
        this.in = reader;
    }

    /**
     * 返回当前已读取行的行间分隔符 <br>
     * 如果还未执行 {@link #readLine()} 方法，则返回字符序列中第一行最后的行间分隔符<br>
     * 如果文本（为空 即长度为零）无行间分隔符，返回空字符
     *
     * @return
     */
    public String getLineSeparator() {
        return this.in.getLineSeparator();
    }

    public synchronized void close() throws IOException {
        if (this.open) {
            try {
                this.in.close();
            } finally {
                this.cacher.close();
                this.open = false;
            }
        }
    }

    class CacheLine implements TextFileReader {

        /** 父对象 */
        private CacheLineReader main;

        /** 被代理类 */
        private TextFileReader lineReader;

        /** 缓存行内容 */
        private ArrayList<TextFileLine> lines;

        /** 表示已读取行的行号, -1表示还未开始读取 0表示当前已读取第一行 */
        private int index;

        /** 当前行号 */
        private long lineNumber;

        /** 行分隔符 */
        private String lineSeparator;

        /**
         * 初始化
         *
         * @param main
         */
        public CacheLine(CacheLineReader main) {
            if (main == null) {
                throw new NullPointerException();
            }

            this.main = main;
            this.lineReader = null;
            this.lineNumber = 0;
            this.lines = new ArrayList<TextFileLine>();
            this.clear();
        }

        /**
         * 读取 n 个字符
         *
         * @param n 需要读取的字符总数
         * @return 实际越过的字符总数
         */
        public long skip(long n) {
            ArrayList<TextFileLine> cache = this.getCacheLine();
            if (cache.size() == 0) {
                return 0;
            } else {
                long read = 0;
                for (int i = 0; i < cache.size(); i++) {
                    TextFileLine record = cache.get(i);
                    String line = record.getContent();
                    String ls = record.getLineSeparator();
                    read += line.length() + ls.length();
                    if (read < n) {
                        this.index++; // 位置信息向下移动一行
                        continue;
                    } else if (read == n) {
                        this.index++; // 位置信息向下移动一行
                        return n;
                    } else { // read > n
                        String str = line + ls;
                        String newLine = StringUtils.rtrim(StringUtils.right(str, (int) (read - n)));
                        Line newRec = new Line(newLine, ls);
                        this.lines.set(this.index + 1, newRec);
                        return n;
                    }
                }
                return read;
            }
        }

        /**
         * 设置行号
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

        /**
         * 查询换行符
         *
         * @return
         */
        public String findLineSeparator() {
            for (int i = this.index; i >= 0 && i < this.lines.size(); i++) {
                TextFileLine obj = this.lines.get(i);
                if (obj.getLineSeparator().length() != 0) {
                    return obj.getLineSeparator();
                }
            }

            for (int i = 0; i < this.index; i++) {
                TextFileLine obj = this.lines.get(i);
                if (obj.getLineSeparator().length() != 0) {
                    return obj.getLineSeparator();
                }
            }

            if (this.lineReader.getLineSeparator().length() != 0) {
                return this.lineReader.getLineSeparator();
            }

            return FileUtils.lineSeparator;
        }

        /**
         * 添加缓存行
         *
         * @param line
         * @param lineSeparator
         * @throws IOException
         */
        public void setCurrentLine(String line, String lineSeparator) throws IOException {
            if (this.main.getReader() == null && this.lineReader == null) {
                throw new IOException(line);
            }

            if (this.main.getReader() != this) {
                this.lineReader = this.main.getReader();
                this.main.setReader(this);
            }

            if (lineSeparator == null) {
                lineSeparator = this.findLineSeparator();
            }

            this.gc();

            // 保存缓存内容
            if (this.index >= 0 && this.index < this.lines.size()) {
                this.lines.set(this.index, new Line(line, lineSeparator));
                this.index--;
                this.lineNumber = this.lineReader.getLineNumber();
            } else {
                this.lines.add(0, new Line(line, lineSeparator));
                if (this.index >= 0) {
                    this.index--;
                }
                this.lineNumber = this.lineReader.getLineNumber();
            }
        }

        /**
         * 从当前位置开始缓存指定行数据到缓存中
         *
         * @param n 缓存行数, 从1开始（小于等于零时默认只缓存1行）
         * @return 返回实际缓存行数
         * @throws IOException
         */
        public int cacheLine(int n) throws IOException {
            if (this.main.getReader() == null && this.lineReader == null) {
                throw new IOException(String.valueOf(n));
            }

            if (this.main.getReader() != this) {
                this.lineReader = this.main.getReader();
                this.main.setReader(this);
            }

            if (this.lineReader == null) {
                return 0;
            }

            if (this.index == -1 || this.lines.isEmpty()) {
                this.lineSeparator = this.lineReader.getLineSeparator();
            }

            this.gc();

            // 缓冲行为空时, 更新当前行号
            if (this.lines.isEmpty()) {
                this.lineNumber = this.lineReader.getLineNumber();
            }

            // 将行内容和换行符保存到缓存中
            int count = 0;
            for (int size = (n <= 0 ? 1 : n); count < size; count++) {
                String line = this.lineReader.readLine();
                if (line == null) {
                    break;
                } else {
                    this.lines.add(new Line(line, this.lineReader.getLineSeparator()));
                }
            }
            return count;
        }

        /**
         * 返回未读的缓存行
         *
         * @return 副本
         */
        public ArrayList<TextFileLine> getCacheLine() {
            ArrayList<TextFileLine> list = new ArrayList<TextFileLine>(this.lines.size());
            for (int i = this.index + 1, size = this.lines.size(); i < size; i++) {
                TextFileLine obj = this.lines.get(i);
                list.add(new Line(obj.getContent(), obj.getLineSeparator()));
            }
            return list;
        }

        public String readLine() throws IOException {
            if (!this.main.isOpen()) {
                throw new IOException();
            } else if (this.lineReader == null || this.lines.size() == 0) {
                return null;
            } else if (++this.index < this.lines.size()) {
                this.lineNumber++;
                return this.lines.get(this.index).getContent();
            } else {
                this.main.setReader(this.lineReader);
                return this.lineReader.readLine();
            }
        }

        public long getLineNumber() {
            if (this.main.isOpen()) {
                return this.lineNumber;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        public String getLineSeparator() {
            if (!this.main.isOpen()) {
                throw new UnsupportedOperationException();
            } else if (this.lineReader == null || this.lines.size() == 0) {
                return "";
            } else if (this.index == -1) {
                return this.lineSeparator;
            } else if (this.index < this.lines.size()) {
                return this.lines.get(this.index).getLineSeparator();
            } else {
                return this.lineReader.getLineSeparator();
            }
        }

        /**
         * 垃圾回收: 删除已读的缓存内容
         */
        public void gc() {
            if (this.index >= 0) {
                if (this.index < this.lines.size()) {
                    this.lineSeparator = this.lines.get(this.index).getLineSeparator();
                }

                ArrayList<TextFileLine> list = new ArrayList<TextFileLine>(); // 保存未读的行内容
                for (int i = this.index + 1; i < this.lines.size(); i++) {
                    list.add(this.lines.get(i));
                }
                this.lines.clear();
                this.lines.addAll(list);
                this.index = -1;
            }
        }

        public void close() {
            this.clear();
            if (this.lineReader != null) {
                this.main.setReader(this.lineReader);
                this.lineReader = null;
            }
        }

        /**
         * 清空所有缓存数据
         */
        protected void clear() {
            this.lines.clear();
            this.index = -1;
        }

    }

    private class Line implements TextFileLine {

        private String lineSeparator;

        private String line;

        public Line(String line, String lineSeparator) {
            this.line = line;
            this.lineSeparator = lineSeparator;
        }

        public String getLineSeparator() {
            return lineSeparator;
        }

        public String getContent() {
            return line;
        }

        public void setContext(String line) {
            this.line = line;
        }

    }

}
