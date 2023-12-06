package icu.etl.database.parallel;

import icu.etl.concurrent.AbstractJob;
import icu.etl.database.load.inernal.DataWriter;
import icu.etl.database.load.inernal.DataWriterFactory;
import icu.etl.database.load.serial.LoadFileExecutorContext;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;

/**
 * 数据文件装载类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-09
 */
public class LoadFileExecutor extends AbstractJob {
    private final static Log log = LogFactory.getLog(LoadFileExecutor.class);

    /** 文件装载功能的上下文信息 */
    protected LoadFileExecutorContext context;

    /** 数据库输出流 */
    protected DataWriterFactory factory;

    /** 运算结果集 */
    protected ResultSet resultSet;

    /**
     * 创建一个文件加载器
     *
     * @param context   上下文信息
     * @param factory   数据库输出流工厂
     * @param resultSet 装数功能的结果集
     */
    public LoadFileExecutor(LoadFileExecutorContext context, DataWriterFactory factory, ResultSet resultSet) {
        super();

        if (context == null) {
            throw new NullPointerException();
        }
        if (factory == null) {
            throw new NullPointerException();
        }
        if (resultSet == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.factory = factory;
        this.resultSet = resultSet;
    }

    public int execute() throws Exception {
        if (log.isTraceEnabled()) {
            log.trace(ResourcesUtils.getLoadMessage(9, this.getName()));
        }

        // 创建一个表格型文件对象
        TextTableFile file = this.context.getFile();
        DataWriter out = null;
        TextTableFileReader in = file.getReader(this.context.getStartPointer(), this.context.length(), this.context.getReadBuffer());
        try {
            out = this.factory.create();

            // 读取文件中的记录并插入到数据库表中
            TextTableLine line = null;
            while ((line = in.readLine()) != null) {
                out.write(line);
            }
            out.commit();

            // 保存统计信息
            this.resultSet.addTotal(out.getCommitRecords(), out.getSkipRecords(), out.getCommitRecords(), out.getDeleteRecords(), out.getRejectedRecords());

            if (log.isTraceEnabled()) {
                log.trace(ResourcesUtils.getLoadMessage(10, this.getName(), out.getCommitRecords()));
            }
            return 0;
        } finally {
            IO.close(in, out);
        }
    }

}
