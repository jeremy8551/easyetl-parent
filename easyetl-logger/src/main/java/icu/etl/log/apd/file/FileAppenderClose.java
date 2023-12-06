package icu.etl.log.apd.file;

/**
 * 挂钩线程任务，自动关闭文件输出流
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/2
 */
public class FileAppenderClose implements Runnable {

    private FileAppender appender;

    public FileAppenderClose(FileAppender appender) {
        this.appender = appender;
    }

    public void run() {
        if (this.appender != null) {
            this.appender.close();
        }
    }
}
