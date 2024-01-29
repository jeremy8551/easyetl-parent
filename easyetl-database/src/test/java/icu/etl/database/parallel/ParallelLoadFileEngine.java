package icu.etl.database.parallel;

import java.util.Date;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.concurrent.EasyJobReaderImpl;
import icu.etl.concurrent.EasyJobService;
import icu.etl.concurrent.ThreadSource;
import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadFileMessage;
import icu.etl.database.load.LoadIndex;
import icu.etl.database.load.LoadTable;
import icu.etl.database.load.Loader;
import icu.etl.database.load.inernal.DataWriterFactory;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.util.StringUtils;

@EasyBean(name = "replace")
public class ParallelLoadFileEngine implements Loader, EasyContextAware {

    /** 上下文信息 */
    private LoadEngineContext context;

    /** 容器上下文信息 */
    protected EasyContext ioc;

    /**
     * 初始化
     */
    public ParallelLoadFileEngine() {
        super();
    }

    public void setContext(EasyContext context) {
        this.ioc = context;
    }

    public void execute(LoadEngineContext context) throws Exception {
        if (context == null) {
            throw new NullPointerException();
        } else {
            this.context = context;
        }

        JdbcDao dao = new JdbcDao(this.ioc);
        try {
            dao.connect(this.context.getDataSource());
            LoadTable target = new LoadTable(dao, null);

            // 前置操作
            LoadIndex listener = new LoadIndex(target.getTable());
            listener.before(context, dao);

            // 将大数据文件分成四十个任务，并行将数据插入到数据库表中
            DataWriterFactory factory = new DataWriterFactory(dao, context, target);
            try {
                List<String> sources = context.getFiles();
                for (String filepath : sources) {
                    TextTableFile file = this.ioc.getBean(TextTableFile.class, context.getFiletype(), context); // CommandAttribute.tofile(context, filepath, context.getFiletype());
                    file.setAbsolutePath(filepath);
                    this.execute(dao, factory, file);
                }
            } finally {
                factory.close();
            }

            // 后置操作
            listener.after(context, dao);
        } finally {
            dao.close();
        }
    }

    /**
     * 执行数据文件装载任务
     *
     * @param dao     数据库操作接口
     * @param factory 数据库批量插入工厂
     * @param txtfile 文本文件
     * @return 数据装载结果集
     * @throws Exception
     */
    public StandardResultSet execute(JdbcDao dao, DataWriterFactory factory, TextTableFile txtfile) throws Exception {
        // 消息文件相关
        LoadFileMessage msg = new LoadFileMessage(this.context, txtfile);
        msg.setFile(txtfile);
        msg.setFileModified(txtfile.getFile().lastModified());
        msg.setCharsetName(txtfile.getCharsetName());
        msg.setColumn(txtfile.getColumn());
        msg.setTableName(this.context.getTableName());
        msg.setTableSchema(this.context.getTableSchema());
        msg.setTableCatalog(this.context.getTableCatalog());

        try {
            StandardResultSet result = new StandardResultSet();
            int thread = StringUtils.parseInt(this.context.getAttributes().getAttribute("thread"), 2); // 并发任务数
            int readBuffer = this.context.getReadBuffer(); // 读取输入流缓存大小

            // 创建并行任务输入与输出设备
            LoadFileExecutorReader in = new LoadFileExecutorReader(factory, txtfile, readBuffer, result, msg.getFileFailRanage());

            // 将任务添加到容齐中并行执行装数任务
            EasyJobService container = this.ioc.getBean(ThreadSource.class).getJobService(thread);
            container.execute(new EasyJobReaderImpl(in));

            // 并行同时执行多个任务
            msg.setEndTime(new Date());
            msg.setReadRows(result.getReadCount());
            msg.setCommitRows(result.getCommitCount());
            msg.setDeleteRows(result.getDeleteCount());
            msg.setErrorRows(result.getErrorCount());
            msg.setSkipRows(result.getSkipCount());

//				for (LoaderListener listener : this.listeners) {
//					listener.catchExecption(dao, txtfile, out);
//				}

            msg.setEndTime(new Date());
            msg.setFileRange(null);
            msg.setReadRows(result.getReadCount());
            msg.setCommitRows(result.getCommitCount());
            msg.setDeleteRows(result.getDeleteCount());
            msg.setErrorRows(result.getErrorCount());
            msg.setSkipRows(result.getSkipCount());
            return result;
        } finally {
            msg.store();
        }
    }

    public void terminate() {
        // TODO Auto-generated method stub

    }

}
