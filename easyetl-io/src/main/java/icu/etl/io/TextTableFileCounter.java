package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import icu.etl.concurrent.AbstractJob;
import icu.etl.concurrent.EasyJobReaderImpl;
import icu.etl.concurrent.ThreadSource;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

public class TextTableFileCounter {
    private final static Log log = LogFactory.getLog(TextTableFileCounter.class);

    /** 单线程和多线程统计文本行数的阀值，小于阀值使用单线程统计，大于等于阀值使用多线程统计文本行数（阀值同时也作为每个临时文件的大小） */
    public static long UNIT = 1024 * 1024 * 1024; // 1GB

    /** 线程池 */
    private ThreadSource threadSource;

    /** 任务最大并发数 */
    private int concurrernt;

    public TextTableFileCounter(ThreadSource threadSource, int concurrernt) {
        this.threadSource = Ensure.notNull(threadSource);
        this.concurrernt = Ensure.isFromOne(concurrernt);
    }

    /**
     * 快速统计文本文件行数, 统计规则如下: <br>
     * 根据文件中回车换行符、换行符、回车符个数计算行数，即一个回车符、换行符或回车换行符算作一行
     *
     * @param file        文件
     * @param charsetName 文件字符集, 为空时取操作系统默认值
     * @return 文件中的行数
     * @throws Exception 错误
     */
    public long execute(File file, String charsetName) throws Exception {
        FileUtils.assertFile(file);
        if (file.length() < TextTableFileCounter.UNIT) { // 小于 1G 使用单线程
            return this.executeSerial(file, StringUtils.charset(charsetName));
        } else {
            return this.executeParallel(file);
        }
    }

    /**
     * 单线程计算文本行数
     *
     * @param file        文件信息
     * @param charsetName 字符集编码
     * @return 文件行数
     * @throws IOException 读取文件发生错误
     */
    protected long executeSerial(File file, String charsetName) throws IOException {
        return FileUtils.count(file, charsetName);
    }

    /**
     * 多线程并行计算文本行数
     *
     * @param file 文件信息
     * @return 文件行数
     * @throws Exception 并发任务发生错误
     */
    protected long executeParallel(File file) throws Exception {
        Long divide = Numbers.divide(file.length(), (long) 12);
        long partSize = Math.max(divide, 92160);

        int readBuffer = IO.FILE_BYTES_BUFFER_SIZE;
        if (readBuffer > partSize) {
            readBuffer = (int) partSize;
        }

        // 创建分段任务
        long partStartPointer = 0; // 分段任务起始位置
        long fileSize = file.length();
        List<ReadLineJob> list = new ArrayList<ReadLineJob>();
        do {
            long partEndPointer = partStartPointer + partSize + 1; // 文件读取的结束位置（包含）
            if (partEndPointer > fileSize) {
                partEndPointer = fileSize;
            }

            long pieceSize = partEndPointer - partStartPointer + 1; // 读取长度
            list.add(new ReadLineJob(file, readBuffer, partStartPointer, partEndPointer, pieceSize)); // 添加分段任务
            partStartPointer = partEndPointer + 1; // 下一个位置指针
        } while (partStartPointer <= fileSize);

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("io.standard.output.msg040", this.concurrernt, list.size(), new BigDecimal(partSize), new BigDecimal(readBuffer), fileSize));

            for (ReadLineJob job : list) {
                log.debug(job.getName());
            }
        }

        // 并发统计行数
        this.threadSource.getJobService(this.concurrernt).execute(new EasyJobReaderImpl(list));

        // 统计行数
        long total = 0;
        ReadLineJob last = null;
        for (ReadLineJob job : list) {
            if (last != null) { // 如果上一个片段的最后一个字符是 \r 下一个片段的第一个字符是 \n 需要减掉一个换行符
                if (last.getEndPointer() == '\r' && job.getStartPointer() == '\n') {
                    total--;
                }
            }
            last = job;
            total += job.getLineNumber();
        }

        // 最后一个字符不是换行符，需要自增一行
        if (last != null && (last.getEndPointer() != '\r' && last.getEndPointer() != '\n')) {
            total++;
        }
        return total;
    }

    private static class ReadLineJob extends AbstractJob {

        /** 文件内的位置指针 */
        private final long filePointer;

        /** 能读取地最大字节数 */
        private final long maxBytes;

        /** 文件 */
        private final File file;

        /** 已读取的文件总行数 */
        private long readLines;

        /** 缓冲区长度（读取文件时的缓冲区） */
        private final int bufferSize;

        /** 读取的第一个字节 */
        private byte firstChar;

        /** 读取的最后一个字节 */
        private byte lastChar;

        /**
         * 初始化
         *
         * @param file             文件
         * @param bufferSize       读取文件的缓冲区长度
         * @param partStartPointer 读取文件的起始位置
         * @param partEndPointer   读取文件的结束位置（包含）
         * @param maxByteSize      读取文件的最大字节数
         */
        public ReadLineJob(File file, int bufferSize, long partStartPointer, long partEndPointer, long maxByteSize) {
            this.file = Ensure.notNull(file);
            this.bufferSize = Ensure.isFromOne(bufferSize);
            this.filePointer = Ensure.isFromZero(partStartPointer);
            this.maxBytes = Ensure.isFromZero(maxByteSize);
            this.firstChar = ' ';
            this.lastChar = ' ';
            this.setName(ResourcesUtils.getMessage("io.standard.output.msg063", file.getAbsolutePath(), partStartPointer, partEndPointer));
        }

        public int execute() throws IOException {
            TimeWatch watch = new TimeWatch();
            RandomAccessFile in = new RandomAccessFile(this.file, "r");
            try {
                if (this.filePointer > 0) {
                    in.seek(this.filePointer);

                    if (log.isTraceEnabled()) {
                        log.trace(ResourcesUtils.getIoxMessage(43, this.getName(), this.filePointer));
                    }
                }

                // 循环读取文件中指定位置
                ByteBuffer buffer = ByteBuffer.allocate(this.bufferSize);
                FileChannel channel = in.getChannel();
                int length = channel.read(buffer);
                if (length == -1) {
                    return 0;
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

                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("io.standard.output.msg044", this.getName(), this.readLines));
                }
                return 0;
            } finally {
                in.close();
            }
        }

        /**
         * 返回文件
         *
         * @return 文件
         */
        public File getFile() {
            return file;
        }

        /**
         * 返回已读行数
         *
         * @return 已读行数
         */
        public long getLineNumber() {
            return readLines;
        }

        /**
         * 返回起始位置，从0开始
         *
         * @return 起始位置
         */
        public byte getStartPointer() {
            return this.firstChar;
        }

        /**
         * 返回结束位置
         *
         * @return 结束位置
         */
        public byte getEndPointer() {
            return this.lastChar;
        }
    }

}
