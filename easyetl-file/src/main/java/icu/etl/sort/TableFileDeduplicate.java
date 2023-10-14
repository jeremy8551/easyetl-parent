package icu.etl.sort;

import java.io.IOException;
import java.util.Arrays;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Terminate;

/**
 * 工具类：用于检查表格型数据文件中是否有重复行
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-22
 */
public class TableFileDeduplicate implements Terminate {

    private static Log log = LogFactory.getLog(TableFileDeduplicate.class);

    /** 默认模式：全量检查所有重复数据 */
    public static final int DEFAULT_MODE = 0;

    /** 探索模式：检查到有重复数据就退出 */
    public static final int EXPLORE_MODE = 1;

    /** 读取表格型文件的缓冲区 */
    private int readbuf;

    /** true表示检查全量 */
    private int mode;

    /** 中断标志 */
    private volatile boolean terminate;

    public TableFileDeduplicate() {
        this(IO.FILE_BYTES_BUFFER_SIZE, DEFAULT_MODE);
    }

    public TableFileDeduplicate(int readbuf, int mode) {
        this.readbuf = readbuf;
        this.mode = mode;
    }

    /**
     * @param tablefile
     * @param indexs
     * @param logfilepath
     * @param append
     * @return 返回重复行数
     */
    public int remove(TextTableFile tablefile, int[] indexs, String logfilepath, boolean append) throws IOException {
        String[] lastRecordIndexs = new String[indexs.length];
        Arrays.fill(lastRecordIndexs, "");
        String lastRecordContent = "";

        TextTableFileReader in = tablefile.getReader(this.readbuf);
        try {
            TextTableLine line;
            boolean equals;
            while ((line = in.readLine()) != null) {
                if (this.terminate) {
                    return 0;
                }

                if (lastRecordContent.length() == 0) {
                    equals = false;
                } else {
                    equals = true;
                    for (int i = 0; i < indexs.length; i++) { // 与上一行的唯一索引字段进行比较
                        if (!lastRecordIndexs[i].equals(line.getColumn(indexs[i]))) {
                            equals = false;
                        }
                    }
                }

                if (equals) { // 如果连续2行的唯一索引字段相等，则存在重复数据
                    StringBuilder buf = new StringBuilder(100);
                    buf.append("数据文件存在重复数据：唯一索引相同!").append(FileUtils.lineSeparator);
                    buf.append("数据文件：").append(tablefile.getAbsolutePath()).append(FileUtils.lineSeparator);
                    buf.append("唯一索引：").append(FileUtils.lineSeparator);
                    for (int i = 0, last = indexs.length - 1; i <= last; i++) {
                        buf.append("第").append(indexs[i]).append("个字段").append(":").append(lastRecordIndexs[i]);
                        if (i < last) {
                            buf.append(", ");
                        }
                    }
                    buf.append(FileUtils.lineSeparator);
                    buf.append("重复数据：").append(FileUtils.lineSeparator);
                    buf.append(lastRecordContent).append(FileUtils.lineSeparator); // 上一行的内容
                    buf.append(line.getContent()); // 当前行的内容
                    log.info(buf.toString());
                } else {
                    // 将当前行的内容与唯一索引字段保存到变量中
                    lastRecordContent = line.getContent();
                    for (int i = 0; i < indexs.length; i++) {
                        lastRecordIndexs[i] = line.getColumn(indexs[i]);
                    }
                }
            }
            return 0;
        } finally {
            in.close();
        }
    }

    /**
     * 检查排序后文件内容，是否有重复唯一索引数据
     *
     * @param tablefile 表格文件
     * @param indexs    唯一索引信息
     * @throws IOException
     */
    public boolean detect(TextTableFile tablefile, int[] indexs) throws IOException {
        String[] lastRecordIndexs = new String[indexs.length];
        Arrays.fill(lastRecordIndexs, "");
        String lastRecordContent = "";

        TextTableFileReader in = tablefile.getReader(this.readbuf);
        try {
            TextTableLine line;
            boolean equals;
            while ((line = in.readLine()) != null) {
                if (this.terminate) {
                    return false;
                }

                equals = true;
                for (int i = 0; i < indexs.length; i++) { // 与上一行的唯一索引字段进行比较
                    if (!lastRecordIndexs[i].equals(line.getColumn(indexs[i]))) {
                        equals = false;
                    }
                }

                if (equals) { // 如果连续2行的唯一索引字段相等，则存在重复数据
                    StringBuilder buf = new StringBuilder(100);
                    buf.append("数据文件存在重复数据：唯一索引相同!").append(FileUtils.lineSeparator);
                    buf.append("数据文件：").append(tablefile.getAbsolutePath()).append(FileUtils.lineSeparator);
                    buf.append("唯一索引：").append(FileUtils.lineSeparator);
                    for (int i = 0, last = indexs.length - 1; i <= last; i++) {
                        buf.append("第").append(indexs[i]).append("个字段").append(":").append(lastRecordIndexs[i]);
                        if (i < last) {
                            buf.append(", ");
                        }
                    }
                    buf.append(FileUtils.lineSeparator);
                    buf.append("重复数据：").append(FileUtils.lineSeparator);
                    buf.append(lastRecordContent).append(FileUtils.lineSeparator); // 上一行的内容
                    buf.append(line.getContent()); // 当前行的内容
                    log.info(buf.toString());
                    return true;
                } else {
                    // 将当前行的内容与唯一索引字段保存到变量中
                    lastRecordContent = line.getContent();
                    for (int i = 0; i < indexs.length; i++) {
                        lastRecordIndexs[i] = line.getColumn(indexs[i]);
                    }
                }
            }
            return false;
        } finally {
            in.close();
        }
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void terminate() {
        this.terminate = true;
    }

}
