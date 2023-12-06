package icu.etl.log.apd.file;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import icu.etl.log.Appender;
import icu.etl.log.LogContext;
import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.LogEvent;
import icu.etl.util.Ensure;
import icu.etl.util.JUL;

/**
 * 文件记录器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/23
 */
public class FileAppender extends ConsoleAppender implements Appender {

    /** 日志文件 */
    private String file;

    /** 日志文件字符集 */
    private String charsetName;

    /** 输出流 */
    private FileAppenderWriter out;

    /** 提交事物 */
    private FileAppenderJob job;

    /** 缓冲区长度 */
    private int size;

    /** 线程池 */
    private Executor service;

    public FileAppender(String logfile, String charsetName, String pattern) {
        this(null, logfile, charsetName, pattern, 300);
    }

    public FileAppender(Executor service, String logfile, String charsetName, String pattern, int size) {
        super(pattern);
        this.file = logfile;
        this.charsetName = charsetName;
        this.service = service;
        this.size = Ensure.isFromOne(size);
    }

    public String getName() {
        return FileAppender.class.getSimpleName();
    }

    /**
     * 设置日志文件绝对路径
     *
     * @param logfile 日志文件绝对路径
     */
    public FileAppender logfile(String logfile) {
        this.file = logfile;
        return this;
    }

    /**
     * 设置文件的字符集
     *
     * @param charsetName 字符集
     */
    public FileAppender charsetName(String charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    /**
     * 返回文件绝对路径
     *
     * @return 文件路径
     */
    public String getFile() {
        return this.out.getFile();
    }

    public ConsoleAppender pattern(String pattern) {
        super.pattern(pattern);
        return this;
    }

    public Appender setup(LogContext context) {
        try {
            if (this.service == null) {
                this.out = new FileAppenderWriter(this.file, this.charsetName, new SimpleBlockingQueue<LogEvent>(), this.layout);
            } else {
                this.out = new FileAppenderWriter(this.file, this.charsetName, new LinkedBlockingQueue<LogEvent>(this.size), this.layout);
                this.job = new FileAppenderJob(this.out);
                this.service.execute(this.job);
            }

            // 注册挂钩线程
            Runtime.getRuntime().addShutdownHook(new Thread(new FileAppenderClose(this)));

            // 安装记录器
            context.addAppender(this);
            return this;
        } catch (Throwable e) {
            if (JUL.isErrorEnabled()) {
                JUL.error(this.file, e);
            }
            return null;
        }
    }

    public synchronized void append(LogEvent event) {
        if (this.out != null) {
            this.out.add(event);

            // 如果是单线程记录日志
            if (this.service == null) {
                try {
                    this.out.write();
                } catch (IOException e) {
                    if (JUL.isErrorEnabled()) {
                        JUL.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    public synchronized void close() {
        if (this.job != null) {
            this.job.terminate();
            this.job.waitFor();
            this.job = null;
        }

        if (this.out != null) {
            this.out.close();
            this.out = null;
        }
    }
}
