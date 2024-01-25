package icu.etl.log.apd.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;

import icu.etl.ProjectPom;
import icu.etl.log.apd.Layout;
import icu.etl.log.apd.LogEvent;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.JUL;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

/**
 * 日志记录器，将日志记录到日志文件中
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/23
 */
public class FileAppenderWriter {

    /** 缓冲区 */
    private BlockingQueue<LogEvent> blockingQueue;

    /** 输出流 */
    private OutputStreamWriter out;

    /** 日志文件路径 */
    private String logfile;

    /** 日志文件字符集 */
    private String charsetName;

    /** 日志格式化工具 */
    private Layout layout;

    /** 连续发生IO异常的次数 */
    private int iotimes;

    /**
     * 日志记录器
     *
     * @param logfile     日志文件
     * @param charsetName 字符集
     * @param queue       阻塞队列
     * @param layout      日志格式化工具
     * @throws IOException 文件错误
     */
    public FileAppenderWriter(String logfile, String charsetName, BlockingQueue<LogEvent> queue, Layout layout) throws IOException {
        if (StringUtils.isBlank(logfile)) {
            logfile = FileUtils.joinPath(Settings.getUserHome().getAbsolutePath(), "." + ProjectPom.getArtifactID(), Settings.getUserName() + ".log");
        }

        this.charsetName = StringUtils.charset(charsetName);
        this.blockingQueue = Ensure.notNull(queue);
        this.logfile = Ensure.notNull(logfile);
        this.layout = Ensure.notNull(layout);
        this.open();
    }

    /**
     * 打开输出流
     *
     * @throws IOException 访问日志文件错误
     */
    public void open() throws IOException {
        File file = FileUtils.assertCreateFile(this.logfile);
        IO.flushQuiet(this.out);
        IO.closeQuiet(this.out);
        this.out = new OutputStreamWriter(new FileOutputStream(file, true), this.charsetName);
    }

    /**
     * 记录日志
     *
     * @param event 日志事件
     */
    public void add(LogEvent event) {
        try {
            this.blockingQueue.put(event);
        } catch (Throwable e) {
            if (JUL.isErrorEnabled()) {
                JUL.error(event.getMessage(), e);
            }
        }
    }

    /**
     * 从缓存中读取一个日志，并写入到输出流中
     *
     * @throws IOException 重新打开日志文件输出流错误
     */
    public void write() throws IOException {
        try {
            LogEvent event = this.blockingQueue.take();
            this.out.write(this.layout.format(event));
            this.out.flush();
            this.iotimes = 0;
        } catch (IOException ie) {
            if (JUL.isWarnEnabled()) {
                JUL.warn(ie.getLocalizedMessage(), ie);
            }

            // 连续发生3次IO异常，就重新打开日志文件输出流
            if (++this.iotimes >= 3) {
                this.open();
            }
        } catch (InterruptedException e) {
            if (JUL.isWarnEnabled()) {
                JUL.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        IO.closeQuiet(this.out);
    }

    /**
     * 将缓存写入到日志文件中
     *
     * @throws IOException 写入日志错误
     */
    public void flush() throws IOException {
        if (this.blockingQueue.size() > 0) {
            // 复制日志
            LogEvent[] array = new LogEvent[this.blockingQueue.size()];
            this.blockingQueue.toArray(array);
            this.blockingQueue.clear();

            // 写入日志
            for (LogEvent event : array) {
                if (event != null) {
                    this.out.write(this.layout.format(event));
                }
            }

            // 提交缓存
            this.out.flush();
        }
    }

    /**
     * 返回文件
     *
     * @return 文件绝对路径
     */
    public String getFile() {
        return logfile;
    }

}
