package icu.etl.database.export.inernal;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import icu.etl.concurrent.ExecutorLogger;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcObjectConverter;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.database.SQL;
import icu.etl.database.export.ExtractReader;
import icu.etl.database.export.ExtracterContext;
import icu.etl.database.export.converter.AbstractConverter;
import icu.etl.io.TextTable;
import icu.etl.printer.Progress;
import icu.etl.util.CharTable;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class DatabaseReader implements ExtractReader {

    /** 数据库操作类 */
    private JdbcDao dao;

    /** 数据库查询结果集 */
    private ResultSet resultSet;

    /** 数据库操作类 */
    private int index;

    /** 列数 */
    private int column;

    /** 返回总行数 */
    private long totalRow;

    /** false 表示已读取完最后一个记录 */
    private boolean hasNext;

    /** 字段的处理逻辑 */
    private JdbcObjectConverter[] columns;

    /** 读取进度输出接口 */
    private Progress progress;

    /** true 表示执行进度输出 */
    private boolean print;

    /** 字段数组 */
    private String[] values;

    /**
     * 初始化
     *
     * @throws IOException
     * @throws SQLException
     */
    public DatabaseReader(ExtracterContext context) throws SQLException, IOException {
        this.open(context);
    }

    /**
     * 打开输入流
     *
     * @param context
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private void open(ExtracterContext context) throws SQLException, IOException {
        this.dao = new JdbcDao();
        this.close();
        this.hasNext = true;
        this.index = 0;
        this.column = 0;
        this.dao.connect(context.getDataSource());

        TextTable format = context.getFormat();
        JdbcConverterMapper define = context.getConverters(); // 用户自定义映射关系
        JdbcConverterMapper database = this.dao.getDialect().getObjectConverters(); // 数据库默认映射关系

        // 计算总行数
        String countSQL = SQL.toCountSQL(context.getSource());
        this.totalRow = this.dao.queryCountByJdbc(countSQL);

        // 执行进度输出
        if (context.getProgress() != null) {
            this.print = true;
            this.progress = new Progress(context.getExtracter().getName(), context.getProgress().getPrinter(), context.getProgress().getMessage(), this.totalRow);
        } else {
            this.print = false;
        }

        // 执行查询
        JdbcQueryStatement query = this.dao.query(context.getSource(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        this.resultSet = query.getResultSet();
        String[] columnName = Jdbc.getColumnName(this.resultSet);
        String[] columnType = Jdbc.getColumnTypeName(this.resultSet); // 字段类型信息: char, integer, decimal

        // 将结果集与处理逻辑类进行映射
        this.column = columnName.length;
        this.columns = new JdbcObjectConverter[this.column];
        this.values = new String[this.columns.length + 1];
        for (int i = 0; i < this.column; i++) {
            String colName = columnName[i];
            String colType = columnType[i];

            // 用户自定义处理逻辑
            if (define != null && define.contains(colName)) {
                this.columns[i] = define.get(colName);
            }

            // 用户自定义处理逻辑
            else if (define != null && define.contains(colType)) {
                this.columns[i] = define.get(colType);
            }

            // 数据库中字段类型与处理逻辑之间的映射关系
            else if (database != null && database.contains(colType)) {
                this.columns[i] = database.get(colType);
            } else {
                throw new UnsupportedOperationException(colName + " " + colType);
            }

            // 设置属性
            JdbcObjectConverter converter = this.columns[i];
            converter.setAttribute(AbstractConverter.PARAM_COLUMN, new Integer(i + 1));
            converter.setAttribute(AbstractConverter.PARAM_BUFFER, this.values);
            converter.setAttribute(AbstractConverter.PARAM_RESULT, this.resultSet);
            converter.setAttribute(AbstractConverter.PARAM_JDBCDAO, this.dao);
            converter.setAttribute(AbstractConverter.PARAM_COLNAME, columnName[i]);
            converter.setAttribute(AbstractConverter.PARAM_COLDEL, format.getDelimiter());
            converter.setAttribute(AbstractConverter.PARAM_CHARDEL, format.getCharDelimiter());
            converter.setAttribute(AbstractConverter.PARAM_CHARSET, format.getCharsetName());
            converter.setAttribute(AbstractConverter.PARAM_CHARHIDE, context.getCharFilter());
            converter.setAttribute(AbstractConverter.PARAM_ESCAPES, context.getEscapes());

            if (StringUtils.isNotBlank(context.getDateformat())) {
                converter.setAttribute(AbstractConverter.PARAM_DATEFORMAT, context.getDateformat());
            }

            if (StringUtils.isNotBlank(context.getTimeformat())) {
                converter.setAttribute(AbstractConverter.PARAM_TIMEFORMAT, context.getTimeformat());
            }

            if (StringUtils.isNotBlank(context.getTimestampformat())) {
                converter.setAttribute(AbstractConverter.PARAM_TIMESTAMPFORMAT, context.getTimestampformat());
            }

            if (format.existsEscape()) {
                converter.setAttribute(AbstractConverter.PARAM_ESCAPE, format.getEscape());
            }

            converter.init();
        }

        ExecutorLogger log = context.getExtracter().getLogger();
        if (log.isDebugEnabled()) {
            log.debug(this.toDetailMessage(context, this.resultSet, this.columns));
        }
    }

    public boolean hasLine() throws IOException, SQLException {
        if (this.hasNext && this.resultSet.next()) {
            for (this.index = 0; this.index < this.column; this.index++) {
                this.columns[this.index].execute();
            }

            if (this.print) {
                this.progress.print();
            }
            return true;
        } else {
            this.hasNext = false;
            return false;
        }
    }

    public void close() {
        this.hasNext = false;
        if (this.dao.existsConnection()) {
            Statement statement = null;
            try {
                statement = this.resultSet.getStatement();
            } catch (Exception e) {
            } finally {
                IO.close(this.resultSet, statement);
            }

            this.dao.commitQuietly();
            this.dao.rollbackQuietly();
            this.dao.close();
        }
        this.columns = null;
    }

    /**
     * 返回详细错误信息
     *
     * @param context
     * @param resultSet
     * @param processors
     * @return
     * @throws SQLException
     */
    protected String toDetailMessage(ExtracterContext context, ResultSet resultSet, JdbcObjectConverter[] processors) throws SQLException {
        String source = context.getSource();
        String target = context.getTarget();

        String[] titles = StringUtils.split(ResourcesUtils.getExtractMessage(4), ',');
        CharTable table = new CharTable();
        table.addTitle(titles[0], CharTable.ALIGN_RIGHT);
        table.addTitle(titles[1], CharTable.ALIGN_LEFT);
        table.addTitle(titles[2], CharTable.ALIGN_LEFT);
        table.addTitle(titles[3], CharTable.ALIGN_LEFT);
        table.addTitle(titles[4], CharTable.ALIGN_RIGHT);
        table.addTitle(titles[5], CharTable.ALIGN_LEFT);

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 0; i < processors.length; i++) {
            int column = i + 1;
            table.addCell(Integer.toString(i + 1));
            table.addCell(metaData.getColumnClassName(column));
            table.addCell(metaData.getColumnTypeName(column));
            table.addCell(metaData.getColumnClassName(column));
            table.addCell(metaData.getColumnDisplaySize(column));
            table.addCell(processors[i].getClass().getName());
        }

        CharTable cb = new CharTable();
        cb.addTitle("");
        cb.addCell(ResourcesUtils.getExtractMessage(5, source));
        cb.addCell(ResourcesUtils.getExtractMessage(6, target));
        cb.addCell(table.toStandardShape().ltrim().toString());
        return FileUtils.lineSeparator + cb.toSimpleShape().toString();
    }

    public boolean isColumnBlank(int position) {
        return StringUtils.isBlank(this.values[position]);
    }

    public String getColumn(int position) {
        return this.values[position];
    }

    public void setColumn(int position, String value) {
        this.values[position] = value;
    }

    public int getColumn() {
        return this.column;
    }

}