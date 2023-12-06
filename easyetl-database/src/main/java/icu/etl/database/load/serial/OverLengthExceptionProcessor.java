package icu.etl.database.load.serial;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icu.etl.concurrent.AbstractJob;
import icu.etl.concurrent.EasyJob;
import icu.etl.concurrent.EasyJobReader;
import icu.etl.concurrent.EasyJobReaderImpl;
import icu.etl.concurrent.ThreadSource;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadFileRange;
import icu.etl.database.load.LoadTable;
import icu.etl.expression.DataUnitExpression;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;

/**
 * 处理数据文件中字段值大于数据库表中字段长度的错误
 *
 * @author jeremy8551@qq.com
 */
public class OverLengthExceptionProcessor {
    private final static Log log = LogFactory.getLog(OverLengthExceptionProcessor.class);

    /** 最大并发任务数 */
    private int concurrent;

    public OverLengthExceptionProcessor(int concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * 扫描数据文件并与目标表中字段类型进行比较，并自动扩容数据库表中字段长度
     *
     * @param context 容器上下文信息
     * @param dao     数据库接口
     * @param file    数据文件
     * @param target  目标表
     * @return 返回已修改字段个数
     * @throws IOException  文件访问错误
     * @throws SQLException 数据库错误
     */
    public int execute(EasyContext context, JdbcDao dao, TextTableFile file, LoadTable target) throws IOException, SQLException {
        // 扫描数据文件中的长度字段
        ExpandLengthJobReader in = new ExpandLengthJobReader(file, target, DataUnitExpression.parse("100M").longValue());
        context.getBean(ThreadSource.class).getJobService(this.concurrent).executeForce(new EasyJobReaderImpl(in));
        List<DatabaseTableColumn> columns = in.getColumns();
        in.close();

        // 修改数据库表中超长字段的长度
        for (DatabaseTableColumn column : columns) {
            DatabaseTableColumn old = target.getTable().getColumns().getColumn(column.getPosition());
            if (!old.equals(column)) {
                List<String> list = dao.getDialect().alterTableColumn(dao.getConnection(), old, column);
                if (log.isDebugEnabled()) {
                    for (String sql : list) {
                        log.debug(sql);
                    }
                }
            }
        }
        dao.commit();
        return columns.size();
    }

    /**
     * 文件分段识别输入流
     */
    static class ExpandLengthJobReader implements EasyJobReader {

        /** 数据文件 */
        private TextTableFile file;

        /** 目标数据库表 */
        private LoadTable target;

        /** true表示已终止 */
        private volatile boolean terminate;

        /** 当前读取文件的位置，从0开始 */
        private long index;

        /** 文件的长度，单位字节 */
        private long length;

        /** 每次读取文件的长度，单位字节 */
        private long size;

        /** 发生变化（比如长度已扩展）字段的集合 */
        private Set<DatabaseTableColumn> set;

        /**
         * 初始化
         *
         * @param file   数据文件
         * @param target 目标表信息
         * @param size   每次读取文件的长度
         */
        public ExpandLengthJobReader(TextTableFile file, LoadTable target, long size) {
            super();
            this.terminate = false;
            this.index = 0;
            this.file = Ensure.notNull(file);
            this.target = Ensure.notNull(target);
            this.length = this.file.getFile().length();
            this.size = Ensure.isFromOne(size);
            this.set = Collections.synchronizedSet(new HashSet<DatabaseTableColumn>());
        }

        public boolean isTerminate() {
            return this.terminate;
        }

        public void terminate() {
            this.terminate = true;
        }

        public boolean hasNext() {
            return !this.terminate && this.index < this.length;
        }

        public EasyJob next() throws Exception {
            long begin = this.index; // 起始位置
            long end = begin + this.size; // 结束位置
            if (end > this.length) {
                end = this.length;
            }

            LoadFileExecutorContext contex = new LoadFileExecutorContext();
            contex.setFile(this.file);
            contex.setReadBuffer(IO.FILE_BYTES_BUFFER_SIZE);
            contex.setRange(new LoadFileRange(begin, end, -1));

            this.index = end + 1; // 下一次读取位置
            return new ExpandLengthJob(contex, this.target, this.set);
        }

        public void close() throws IOException {
            this.set.clear();
        }

        /**
         * 发生变化（比如长度已扩展）字段的集合
         *
         * @return 字段集合
         */
        public List<DatabaseTableColumn> getColumns() {
            return new ArrayList<DatabaseTableColumn>(this.set);
        }
    }

    /**
     * 分段扫描数据文件中的字段长度是否超过限制
     */
    static class ExpandLengthJob extends AbstractJob {

        /** 目标表信息 */
        private LoadTable target;

        /** 上下文信息 */
        private LoadFileExecutorContext context;

        /** 发生变化字段的集合 */
        private Set<DatabaseTableColumn> set;

        public ExpandLengthJob(LoadFileExecutorContext context, LoadTable target, Set<DatabaseTableColumn> set) {
            this.context = Ensure.notNull(context);
            this.target = Ensure.notNull(target);
            this.set = Ensure.notNull(set);
        }

        public int execute() throws Exception {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getLoadMessage(9, this.getName()));
            }

            // 创建一个表格型文件对象
            TextTableFile file = this.context.getFile();
            TextTableFileReader in = file.getReader(this.context.getStartPointer(), this.context.length(), this.context.getReadBuffer());
            try {
                int loop = this.target.getColumn();
                int[] positions = this.target.getFilePositions();
                List<DatabaseTableColumn> list = this.target.getTableColumns();
                String charsetName = this.context.getFile().getCharsetName();

                // 读取文件中的记录并插入到数据库表中
                TextTableLine line;
                while ((line = in.readLine()) != null) {
                    for (int i = 0; i < loop; i++) {
                        int position = positions[i]; // 位置信息
                        String value = line.getColumn(position);

                        DatabaseTableColumn column = list.get(i);
                        if (column.expandLength(value, charsetName)) {
                            this.set.add(column);
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getLoadMessage(10, this.getName(), in.getLineNumber()));
                }
                return 0;
            } finally {
                in.close();
            }
        }
    }

}
