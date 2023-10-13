package icu.etl.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icu.etl.concurrent.Executor;
import icu.etl.concurrent.ExecutorContainer;
import icu.etl.concurrent.ExecutorReader;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.log.STD;
import icu.etl.util.IO;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

public class TextTableFileCounter {

    /** 单线程和多线程统计文本行数的阀值，小于阀值使用单线程统计，大于等于阀值使用多线程统计文本行数（阀值同时也作为每个临时文件的大小） */
    public static long UNIT = 1024 * 1024 * 1024; // 1GB

    /** 文件序号模版 */
    private static volatile int threadNoTemplate = 0;

    /**
     * 快速统计文本文件行数, 统计规则如下: <br>
     * 根据文件中回车换行符、换行符、回车符个数计算行数，即一个回车符、换行符或回车换行符算作一行
     *
     * @param file        文件
     * @param charsetName 文件字符集, 为空时取操作系统默认值
     * @return 文件中的行数
     * @throws IOException
     */
    public long execute(File file, String charsetName) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath());
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        if (file.length() < TextTableFileCounter.UNIT) { // 小于 1G 使用单线程
            return this.executeSerial(file, charsetName);
        } else {
            return this.executeParallel(file, TextTableFileCounter.UNIT, IO.FILE_BYTES_BUFFER_SIZE);
        }
    }

    /**
     * 单线程计算文本行数
     *
     * @param file        文件信息
     * @param charsetName 字符集编码
     * @return
     * @throws IOException
     */
    public long executeSerial(File file, String charsetName) throws IOException {
        long count = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName), IO.FILE_BYTES_BUFFER_SIZE);
        try {
            while (in.readLine() != null) {
                count++;
            }
            return count;
        } finally {
            in.close();
        }
    }

    /**
     * 多线程并行计算文本行数
     *
     * @param file       文件信息
     * @param unit       临时文件大小的最大值
     * @param readBuffer 输入流缓冲区长度，单位字符
     * @return
     */
    public long executeParallel(File file, long unit, int readBuffer) {
        int thread = JavaDialectFactory.getDialect().getAvailableThreads();
        if (thread < 3) {
            thread = 3;
        }
        if (thread >= 7) {
            thread = 7;
        }

        Long divide = Numbers.divide(file.length(), (long) 12);
        long partSize = Math.max(divide.longValue(), 92160);
        if (partSize > unit) {
            partSize = unit;
        }

        if (readBuffer <= 0) {
            readBuffer = IO.FILE_BYTES_BUFFER_SIZE;
        }
        if (readBuffer > partSize) {
            readBuffer = (int) partSize;
        }

        // 创建分段任务
        long filePointer = 0;
        List<ReadLineExecutor> list = new ArrayList<ReadLineExecutor>();
        while (true) {
            long fileLength = file.length();
            long length = filePointer + partSize + 1;
            long pieceSize = partSize;
            if (length > fileLength) {
                pieceSize = fileLength - filePointer;
            }

            list.add(new ReadLineExecutor(file, readBuffer, filePointer, pieceSize)); // 添加分段任务
            filePointer += partSize + 1; // 下一个位置指针
            if (filePointer >= fileLength) {
                break;
            }
        }

        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getIoxMessage(40, thread, list.size(), new BigDecimal(partSize), new BigDecimal(readBuffer)));
        }

        // 创建线程池
        ExecutorContainer container = new ExecutorContainer(new ReadLineExecutorReader(list));
        container.execute(thread);

        // 统计行数
        long total = 0;
        ReadLineExecutor last = null;
        for (Iterator<ReadLineExecutor> it = list.iterator(); it.hasNext(); ) {
            ReadLineExecutor obj = it.next();
            if (last != null) { // 如果上一个片段的最后一个字符是 \r 下一个片段的第一个字符是 \n 需要减掉一个换行符
                if (last.getEndPointer() == '\r' && obj.getStartPointer() == '\n') {
                    total--;
                }
            }
            last = obj;
            total += obj.getLineNumber();
        }

        // 最后一个字符不是换行符，需要自增一行
        if (last != null && (last.getEndPointer() != '\r' && last.getEndPointer() != '\n')) {
            total++;
        }
        return total;
    }

    class ReadLineExecutor extends Executor {

        /** 文件内的位置指针 */
        private long filePointer;

        /** 能读取地最大字节数 */
        private long maxBytes;

        /** 文件 */
        private File file;

        /** 已读取的文件总行数 */
        private long readLines;

        /** 缓冲区长度（读取文件时的缓冲区） */
        private int bufferSize;

        /** 读取的第一个字节 */
        private byte firstChar;

        /** 读取的最后一个字节 */
        private byte lastChar;

        /**
         * 初始化
         *
         * @param file        文件
         * @param bufferSize  缓冲区长度（读取文件时的缓冲区）
         * @param filePointer 文件内的位置指针
         * @param maxByteSize 读取文件的最大字节数
         */
        public ReadLineExecutor(File file, int bufferSize, long filePointer, long maxByteSize) {
            if (file == null) {
                throw new NullPointerException();
            }
            if (bufferSize <= 0) {
                throw new IllegalArgumentException(String.valueOf(bufferSize));
            }
            if (filePointer < 0) {
                throw new IllegalArgumentException(String.valueOf(filePointer));
            }
            if (maxByteSize < 0) {
                throw new IllegalArgumentException(String.valueOf(maxByteSize));
            }

            this.firstChar = ' ';
            this.lastChar = ' ';
            this.setName(this.getClass().getSimpleName() + " " + (++TextTableFileCounter.threadNoTemplate));
            this.file = file;
            this.bufferSize = bufferSize;
            this.filePointer = filePointer;
            this.maxBytes = maxByteSize;

            if (STD.out.isTraceEnabled()) {
                STD.out.trace(ResourcesUtils.getIoxMessage(41, this.getName(), filePointer, (filePointer + maxByteSize)));
            }
        }

        public void execute() throws IOException {
            if (STD.out.isTraceEnabled()) {
                STD.out.trace(ResourcesUtils.getIoxMessage(42, this.getName()));
            }

            TimeWatch tk = new TimeWatch();
            RandomAccessFile in = new RandomAccessFile(this.file, "r");
            try {
                if (this.filePointer > 0) {
                    in.seek(this.filePointer);

                    if (STD.out.isTraceEnabled()) {
                        STD.out.trace(ResourcesUtils.getIoxMessage(43, this.getName(), this.filePointer));
                    }
                }

                // 循环读取文件中指定位置
                ByteBuffer buffer = ByteBuffer.allocate(this.bufferSize);
                FileChannel channel = in.getChannel();
                int length = channel.read(buffer);
                if (length == -1) {
                    return;
                }

                byte[] array = buffer.array();
                if (array.length > 0) {
                    this.firstChar = array[0];
                }

                long readByte = 0; // 已读取字节数
                boolean skipLF = false;
                while (true) { // 没有超长
                    if (length == -1) {
                        break;
                    }

                    for (int i = 0; i < length; i++) {
                        this.lastChar = array[i];
                        if (this.lastChar == '\n') { // 换行符标志
                            if (skipLF) { // \r\n
                                skipLF = false;
                            } else {
                                this.readLines++;
                            }
                        } else if (this.lastChar == '\r') {
                            this.readLines++;
                            skipLF = true;
                        }

                        // 已读字符数不能超过总限制
                        if (++readByte >= this.maxBytes) {
                            break;
                        } else if (skipLF) { // 换行符是 \r\n
                            int next = i + 1;
                            long total = readByte + 1;
                            if (next < length && total <= this.maxBytes) {
                                if (array[next] == '\n') {
                                    i = next;
                                    readByte = total;
                                }
                                skipLF = false;
                            }
                        }
                    }

                    // 已达到最大字节数
                    if (readByte >= this.maxBytes) {
                        break;
                    } else { // 继续读取字节
                        buffer.clear();
                        length = channel.read(buffer);
                        array = buffer.array();
                    }
                }

                if (STD.out.isDebugEnabled()) {
                    STD.out.debug(ResourcesUtils.getIoxMessage(44, this.getName(), this.readLines, tk.useTime()));
                }
            } finally {
                in.close();
            }
        }

        /**
         * 返回文件内部位置的指针
         *
         * @return
         */
        public long getFilePointer() {
            return filePointer;
        }

        /**
         * 返回已读取的最大字节数
         *
         * @return
         */
        public long getMaxBytes() {
            return maxBytes;
        }

        /**
         * 返回文件
         *
         * @return
         */
        public File getFile() {
            return file;
        }

        /**
         * 返回已读行数
         *
         * @return
         */
        public long getLineNumber() {
            return readLines;
        }

        /**
         * 返回起始位置信息，从0开始
         *
         * @return
         */
        public byte getStartPointer() {
            return this.firstChar;
        }

        /**
         * 返回结束位置信息
         *
         * @return
         */
        public byte getEndPointer() {
            return this.lastChar;
        }

        public int getPRI() {
            return 0;
        }
    }

    class ReadLineExecutorReader implements ExecutorReader {

        private Iterator<ReadLineExecutor> it;

        private volatile boolean terminate;

        public ReadLineExecutorReader(List<ReadLineExecutor> list) {
            this.it = list.iterator();
        }

        public void terminate() {
            this.terminate = true;
        }

        public boolean isTerminate() {
            return terminate;
        }

        public Executor next() {
            return this.it.next();
        }

        public boolean hasNext() {
            return this.it.hasNext();
        }

        public void close() {
            this.terminate = false;
        }
    }

}
