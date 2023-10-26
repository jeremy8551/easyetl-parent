package icu.etl.database.export.inernal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import icu.etl.annotation.EasyBean;
import icu.etl.database.export.ExtractMessage;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.ExtracterContext;
import icu.etl.io.TableLine;
import icu.etl.io.TableWriter;
import icu.etl.io.TextTable;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.os.OSFtpCommand;
import icu.etl.util.Ensure;

@EasyBean(name = "sftp", description = "卸载数据到远程sftp服务器")
public class SftpFileWriter implements ExtractWriter, EasyetlContextAware {

    protected String target;

    protected OSFtpCommand ftp;

    protected String remotepath;

    /** 文件输出流 */
    protected TableWriter writer;

    protected long lineNumber;

    protected ExtractMessage message;

    protected EasyetlContext context;

    public void setContext(EasyetlContext context) {
        this.context = context;
    }

    /**
     * 初始化
     *
     * @param context
     * @param message
     * @param host
     * @param port
     * @param username
     * @param password
     * @param remotepath
     * @throws IOException
     */
    public SftpFileWriter(ExtracterContext context, ExtractMessage message, String host, String port, String username, String password, String remotepath) throws IOException {
        this.message = message;
        this.remotepath = remotepath;

        this.open(host, port, username, password, remotepath);

        TextTable template = context.getFormat();
        InputStreamImpl in = new InputStreamImpl();
        WriterImpl out = new WriterImpl(new Object(), template.getCharsetName());

        in.setOut(out);
        out.setIn(in);

        this.writer = template.getWriter(out, context.getCacheLines());
        this.ftp.upload(in, this.remotepath);
    }

    /**
     * 打开一个文件输入流接口
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param remotepath
     */
    protected void open(String host, String port, String username, String password, String remotepath) {
        this.ftp = this.context.getBean(OSFtpCommand.class, "sftp");
        Ensure.isTrue(this.ftp.connect(host, Integer.parseInt(port), username, password), host, port, username, password);
        this.target = "sftp://" + username + "@" + host + ":" + port + "?password=" + password;
    }

    public void write(TableLine line) throws IOException {
        this.lineNumber++;
        this.writer.addLine(line);
    }

    public boolean rewrite() throws IOException {
        return false;
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
        if (this.ftp != null) {
            this.ftp.close();
            this.ftp = null;
        }

        this.message.setRows(this.lineNumber);
        this.message.setBytes(0);
        this.message.setTarget(this.target);
    }

    class InputStreamImpl extends InputStream {

        private byte[] bytes;

        private int index;

        private WriterImpl out;

        protected final Lock lock = new ReentrantLock();

        public InputStreamImpl() {
            super();
            this.index = 0;
        }

        public void setOut(WriterImpl out) {
            this.out = out;
        }

        public synchronized int read(byte[] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            try {
                this.lock.lockInterruptibly();

                while (!this.lock.tryLock()) {
                }

                int read = 0, left = 0;
                do {
                    left = this.bytes.length - this.index;
                    read = Math.min(left, len);
                    System.arraycopy(this.bytes, this.index, b, off, read);
                    this.index += read;
                } while (read <= 0 && !this.out.isClose());
                return this.out.isClose() ? -1 : read;
            } catch (InterruptedException e) {
                throw new IOException(off + ", " + len, e);
            } finally {
                this.lock.unlock();
            }
        }

        public synchronized int read() throws IOException {
            return this.bytes[this.index++];
        }

        public synchronized void setBytes(byte[] bytes) {
            this.bytes = bytes;
            this.index = 0;
        }

        public boolean empty() {
            return this.bytes == null || this.index >= this.bytes.length;
        }

    }

    class WriterImpl extends Writer {

        private InputStreamImpl in;

        private String charsetName;

        private volatile boolean close;

        public WriterImpl(Object lock, String charsetName) {
            super(lock);
            this.close = false;
            this.charsetName = charsetName;
        }

        public void setIn(InputStreamImpl in) {
            this.in = in;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            synchronized (this.lock) {
                String str = new String(cbuf, off, len);
                byte[] bytes = str.getBytes(this.charsetName);
                while (true) {
                    if (this.in.empty()) {
                        this.in.setBytes(bytes);
                        break;
                    }
                }
            }
        }

        public void flush() throws IOException {
        }

        public void close() throws IOException {
            this.close = true;
        }

        public boolean isClose() {
            return close;
        }

    }

}
