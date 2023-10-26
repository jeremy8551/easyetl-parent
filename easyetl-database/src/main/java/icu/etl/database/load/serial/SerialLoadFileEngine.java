package icu.etl.database.load.serial;

import java.io.File;
import java.util.Date;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadEngine;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadException;
import icu.etl.database.load.LoadFileMessage;
import icu.etl.database.load.LoadIndex;
import icu.etl.database.load.LoadListenerFactory;
import icu.etl.database.load.LoadMode;
import icu.etl.database.load.LoadTable;
import icu.etl.database.load.Loader;
import icu.etl.database.load.inernal.DataWriter;
import icu.etl.database.load.inernal.DataWriterFactory;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 按文件中出现顺序读取数据文件并装载到数据库表中
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "serial")
public class SerialLoadFileEngine implements Loader, EasyetlContextAware {

    /** true表示终止任务 */
    private volatile boolean running;

    protected EasyetlContext context;

    /**
     * 初始化
     */
    public SerialLoadFileEngine() {
        super();
        this.running = true;
    }

    public void setContext(EasyetlContext context) {
        this.context = context;
    }

    public void execute(LoadEngineContext context) throws Exception {
        JdbcDao dao = new JdbcDao(this.context);
        try {
            dao.connect(context.getDataSource());
            this.execute(dao, context);
            dao.commit();
        } finally {
            dao.close();
        }
    }

    /**
     * 装载数据文件
     *
     * @param dao     数据库操作接口
     * @param context 装数引擎上下文信息
     * @throws Exception
     */
    protected void execute(JdbcDao dao, LoadEngineContext context) throws Exception {
        String tableCatalog = StringUtils.toCase(context.getTableCatalog(), false, null);
        String tableSchema = StringUtils.toCase(context.getTableSchema(), false, null);
        String tableName = StringUtils.toCase(context.getTableName(), false, null);

        DatabaseTable table = dao.getTable(tableCatalog, tableSchema, tableName);
        if (table == null) {
            throw new Exception(tableCatalog + ", " + tableSchema + ", " + tableName);
        }

        LoadTable target = new LoadTable(dao, table);
        LoadIndex index = new LoadIndex(table);
        try {
            target.open(context);
            index.before(context, dao);

            this.execute(dao, context, target);
            target.close();
        } finally {
            try {
                index.after(context, dao); // 后置操作
                dao.commit();
            } finally {
                // TODO 处理主键冲突异常
            }
        }
    }

    /**
     * 装载数据文件
     *
     * @param dao     数据库操作接口
     * @param context
     * @param target
     * @throws Exception
     */
    protected void execute(JdbcDao dao, LoadEngineContext context, LoadTable target) throws Exception {
        DataWriterFactory factory = new DataWriterFactory(dao, context, target);
        try {
            DatabaseTable table = target.getTable();
            if (context.getLoadMode() == LoadMode.REPLACE) { // 先清空表在装入数据
                String sql = dao.deleteTableQuickly(table.getCatalog(), table.getSchema(), table.getName());
                if (LoadEngine.out.isDebugEnabled()) {
                    LoadEngine.out.debug(sql);
                }
                dao.commit();
            }

            // 执行批量插入，向数据库表中大批量插入数据
            DataWriter out = factory.create();
            List<String> files = context.getFiles(); // 按顺序逐个加载文件中的内容
            for (int i = 0; this.running && i < files.size(); i++) {
                String filepath = files.get(i);
                TextTableFile file = this.context.getBean(TextTableFile.class, context.getFiletype(), context); // CommandAttribute.tofile(context, filepath, context.getFiletype());
                file.setAbsolutePath(filepath);

                try {
                    this.execute(context, target, file, null, out);
                } catch (Throwable e) {
                    if (LoadEngine.out.isErrorEnabled()) { // 输出批量错误信息
                        LoadEngine.out.error(filepath, e);
                    }

                    // 需要重新建表
                    if (dao.getDialect().isRebuildTableException(e)) {
                        if (LoadEngine.out.isDebugEnabled()) {
                            LoadEngine.out.debug(ResourcesUtils.getLoadMessage(17, table.getFullName()));
                        }

                        RebuildTableExceptionProcessor obj = new RebuildTableExceptionProcessor();
                        obj.execute(dao, file, target);
                        this.execute(context, target, file, null, out);
                        continue;
                    }

                    // 自动处理文件中字段值超长问题
                    if (dao.getDialect().isOverLengthException(e)) {
                        if (LoadEngine.out.isDebugEnabled()) {
                            LoadEngine.out.debug(ResourcesUtils.getLoadMessage(18, table.getFullName()));
                        }

                        OverLengthExceptionProcessor obj = new OverLengthExceptionProcessor();
                        if (obj.execute(dao, file, target) > 0) { // 扩展字段完成后重新执行装数
                            this.execute(context, target, file, null, out);
                            continue;
                        }
                    }

                    // 自动处理主键冲突错误
                    if (dao.getDialect().isPrimaryRepeatException(e)) {
                        if (LoadEngine.out.isDebugEnabled()) {
                            LoadEngine.out.debug(ResourcesUtils.getLoadMessage(19, table.getFullName()));
                        }

                        PrimaryRepeatExceptionProcessor obj = new PrimaryRepeatExceptionProcessor(context);
                        if (obj.execute(dao, context, target.getTable(), file, out)) {
                            this.execute(context, target, file, obj.getReader(), out);
                            continue;
                        }
                    }

                    throw new LoadException(file.getAbsolutePath(), e);
                }
            }
        } finally {
            factory.close();
        }
    }

    /**
     * 装载数据文件
     *
     * @param context
     * @param target
     * @param file
     * @param reader
     * @param out
     * @throws Exception
     */
    protected synchronized void execute(LoadEngineContext context, LoadTable target, TextTableFile file, TextTableFileReader reader, DataWriter out) throws Exception {
        LoadFileMessage msgfile = new LoadFileMessage(context, file);

        // 不能重复加载
        if (context.isNorepeat() // 如果设置了 norepeat 属性，需要检查是否重复装载数据文件
                && msgfile.getStartTime() != null //
                && msgfile.getEndTime() != null //
                && msgfile.getFileModified() != null //
                && msgfile.getFileModified().equals(new Date(file.getFile().lastModified())) //
                && msgfile.getStartTime().compareTo(msgfile.getFileModified()) >= 0 //
                && msgfile.getEndTime().compareTo(msgfile.getStartTime()) >= 0 //
                && context.getLoadMode() == msgfile.getLoadMode() //
                && file.getFile().equals(new File(msgfile.getFilepath())) //
        ) {
            String fullName = target.getTable().getFullName();
            LoadEngine.out.warn(ResourcesUtils.getLoadMessage(20, file.getFile().getAbsolutePath(), fullName));
            return;
        } else {
            msgfile.setEndTime(null);
        }

        msgfile.setStartTime(new Date());
        msgfile.setFilepath(file);
        msgfile.setFileModified(file.getFile().lastModified());
        msgfile.setFileType(context.getFiletype());
        msgfile.setFileColumns(target.getFilePositions());
        msgfile.setCharsetName(file.getCharsetName());
        msgfile.setColumn(0);
        msgfile.setLoadMode(context.getLoadMode());
        msgfile.setTableCatalog(target.getTable().getCatalog());
        msgfile.setTableSchema(target.getTable().getSchema());
        msgfile.setTableName(target.getTable().getName());
        msgfile.setTableColumns(target.getTableColumns());

        TextTableFileReader in = null;
        if (reader == null) {
            in = file.getReader(context.getReadBuffer());
        } else {
            in = reader;
        }

        try {
            in.setListener(LoadListenerFactory.create(context));

            // 逐行从文件中读取数据
            TextTableLine line = null;
            while (this.running && (line = in.readLine()) != null) {
                out.write(line);
            }
            out.commit();

            // 并行同时执行多个任务
            msgfile.setColumn(file.getColumn()); // 设置文件字段个数
            msgfile.setReadRows(in.getLineNumber());
            msgfile.setCommitRows(out.getCommitRecords());
            msgfile.setDeleteRows(out.getDeleteRecords());
            msgfile.setSkipRows(out.getSkipRecords());
            msgfile.setErrorRows(0);
            msgfile.setEndTime(new Date());
            msgfile.store();

            // 打印消息文件内容
            if (LoadEngine.out.isDebugEnabled()) {
                LoadEngine.out.debug(FileUtils.lineSeparator + msgfile.toString());
            }
        } finally {
            in.close();
        }
    }

    public void terminate() {
        this.running = false;
    }

}
