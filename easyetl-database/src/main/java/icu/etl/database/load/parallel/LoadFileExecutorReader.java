package icu.etl.database.load.parallel;

import java.util.Iterator;
import java.util.List;

import icu.etl.concurrent.Executor;
import icu.etl.concurrent.ExecutorReader;
import icu.etl.database.load.LoadFileRange;
import icu.etl.database.load.inernal.DataWriterFactory;
import icu.etl.io.TextTableFile;

public class LoadFileExecutorReader implements ExecutorReader {

    /** 表格型文件 */
    private TextTableFile file;

    /** true表示已终止 */
    private volatile boolean terminate;

    /** 输入流缓冲区 */
    private int readBuffer;

    /** 数据库输出流 */
    private DataWriterFactory factory;

    /** 读取记录总数 */
    private ResultSet resultSet;

    /** 文件范围集合 */
    private List<LoadFileRange> ranges;

    /** 文件范围遍历器 */
    private Iterator<LoadFileRange> it;

    /**
     * 初始化
     *
     * @param factory    数据库输出流工厂
     * @param file       文件信息
     * @param readBuffer 文件输入流的缓冲区长度，单位字符
     * @param resultSet  结果集
     * @param ranges     文件范围
     * @throws Exception
     */
    public LoadFileExecutorReader(DataWriterFactory factory, TextTableFile file, int readBuffer, ResultSet resultSet, List<LoadFileRange> ranges) throws Exception {
        if (factory == null) {
            throw new NullPointerException();
        }
        if (file == null) {
            throw new NullPointerException();
        }
        if (resultSet == null) {
            throw new NullPointerException();
        }
        if (ranges == null) {
            throw new NullPointerException();
        }

        this.factory = factory;
        this.file = file;
        this.resultSet = resultSet;
        this.ranges = ranges;
        this.readBuffer = readBuffer;
        this.terminate = false;
        this.it = this.ranges.iterator();
    }

    public boolean hasNext() {
        return this.it.hasNext();
    }

    public Executor next() {
        if (this.hasNext()) {
            LoadFileExecutorContext context = new LoadFileExecutorContext();
            context.setFile(this.file);
            context.setReadBuffer(this.readBuffer);
            context.setRange(this.it.next());
            return new LoadFileExecutor(context, this.factory, this.resultSet);
        } else {
            return null;
        }
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    public void terminate() {
        this.terminate = true;
    }

    public void close() {
        this.terminate = false;
    }

}
