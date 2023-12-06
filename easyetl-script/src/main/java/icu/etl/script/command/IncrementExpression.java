package icu.etl.script.command;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.concurrent.ThreadSource;
import icu.etl.database.DatabaseConfigurationContainer;
import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.internal.StandardDatabaseConfiguration;
import icu.etl.expression.WordIterator;
import icu.etl.increment.IncrementReplace;
import icu.etl.io.TextTableFile;
import icu.etl.os.OSConnectCommand;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.sort.TableFileSortContext;
import icu.etl.util.Attribute;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 剥离增量命令语句中的文件信息
 */
public class IncrementExpression implements Attribute<String> {

    private UniversalScriptContext context;
    private UniversalScriptAnalysis analysis;
    private Map<String, String> attributes;
    private Set<String> kinds;
    private String filepath;
    private String filetype;
    private int[] indexPosition;
    private int[] comparePosition;
    private List<IncrementReplace> newchg, updchg, delchg;
    private boolean isLogExpr;
    private DatabaseTable table;

    /**
     * 文件配置表达式: <br>
     * filepath of del ... <br>
     * into filepath of del [modified by key=value] <br>
     * new and upd into filepath of del [modified by key=value] <br>
     * del into filepath of del [modified by key=value] <br>
     * log into filepath <br>
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param str     表达式
     * @throws IOException
     * @throws SQLException
     */
    public IncrementExpression(UniversalScriptSession session, UniversalScriptContext context, String str) throws IOException, SQLException {
        this.analysis = session.getAnalysis();
        this.context = context;
        this.attributes = new CaseSensitivMap<String>();
        this.newchg = new ArrayList<IncrementReplace>();
        this.updchg = new ArrayList<IncrementReplace>();
        this.delchg = new ArrayList<IncrementReplace>();
        this.kinds = new CaseSensitivSet();
        this.isLogExpr = false;

        // 使用 into 关键字分隔字符串
        WordIterator it = this.analysis.parse(str);
        while (it.isNext(new String[]{"and", "new", "upd", "del", "log", "into"})) {
            if (it.isNext(new String[]{"new", "upd", "del", "log"})) {
                String word = it.next();
                if (this.kinds.contains(word)) {
                    throw new IOException(ResourcesUtils.getIncrementMessage(57, word));
                } else {
                    this.kinds.add(word);
                }
            } else if (it.isNext("into")) {
                it.assertNext("into");
                break;
            } else if (it.isNext("and")) {
                it.assertNext("and");
                if (!it.isNext(new String[]{"new", "upd", "del", "log"})) {
                    throw new IOException(ResourcesUtils.getIncrementMessage(58));
                }
            }
        }

        this.filepath = it.next(); // 解析文件路径
        if (it.isNext("of")) { // 解析 of del 语句
            it.assertNext("of");
            this.filetype = it.next();

            if (it.isNext("modified")) { // 解析 modified by 修饰符属性
                it.assertNext("modified");
                it.assertNext("by");

                while (it.hasNext()) { // 解析属性修饰符
                    String expr = it.next();
                    String[] array = StringUtils.splitProperty(expr);
                    if (array == null) {
                        this.attributes.put(expr, "");
                    } else {
                        this.attributes.put(array[0], array[1]);
                    }
                }

                if (this.attributes.isEmpty()) {
                    throw new IOException(ResourcesUtils.getIncrementMessage(64));
                }
            }
        } else {
            this.filetype = this.isLogExpr ? "txt" : null; // log 日志文件只有文件路径，没有其他修饰符
        }

        if (it.hasNext()) {
            throw new IOException(ResourcesUtils.getIncrementMessage(65, it.readOther()));
        } else {
            this.parseAttribute(session, context);
        }
    }

    /**
     * 解析属性信息
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @throws IOException
     * @throws SQLException
     */
    private void parseAttribute(UniversalScriptSession session, UniversalScriptContext context) throws IOException, SQLException {
        if (this.kinds.contains("log") && (this.attributes.containsKey("newchg") || this.attributes.containsKey("updchg") || this.attributes.containsKey("delchg"))) {
            throw new IOException(ResourcesUtils.getIncrementMessage(66));
        }
        if (this.attributes.containsKey("newchg") && (!this.kinds.contains("new") && this.kinds.size() > 0)) {
            throw new IOException(ResourcesUtils.getIncrementMessage(67));
        }
        if (this.attributes.containsKey("updchg") && (!this.kinds.contains("upd") && this.kinds.size() > 0)) {
            throw new IOException(ResourcesUtils.getIncrementMessage(68));
        }
        if (this.attributes.containsKey("delchg") && (!this.kinds.contains("del") && this.kinds.size() > 0)) {
            throw new IOException(ResourcesUtils.getIncrementMessage(69));
        }
        if (this.attributes.containsKey("charset") && this.attributes.containsKey("codepage")) {
            throw new IOException(ResourcesUtils.getIncrementMessage(70));
        }

        this.isLogExpr = this.kinds.contains("log");
        if (this.isLogExpr && this.kinds.size() > 1) {
            throw new IOException(ResourcesUtils.getIncrementMessage(60));
        }

        String[] indexs = StringUtils.removeBlank(StringUtils.split(this.attributes.get("index"), this.analysis.getSegment()));
        String[] compares = StringUtils.removeBlank(StringUtils.split(this.attributes.get("compare"), this.analysis.getSegment()));
        String[] newchg = StringUtils.removeBlank(StringUtils.split(this.attributes.get("newchg"), this.analysis.getSegment()));
        String[] updchg = StringUtils.removeBlank(StringUtils.split(this.attributes.get("updchg"), this.analysis.getSegment()));
        String[] delchg = StringUtils.removeBlank(StringUtils.split(this.attributes.get("delchg"), this.analysis.getSegment()));

        // 如果设置了数据库名表
        if (this.attributes.containsKey("table")) {
            ScriptDataSource dataSource = ScriptDataSource.get(context);
            JdbcDao dao = new JdbcDao(context.getFactory().getContext());
            try {
                if (this.attributes.containsKey("catalog")) { // 使用指定数据库连接
                    Properties p = context.getCatalog(this.attributes.get("catalog"));
                    String driver = p.getProperty(Jdbc.driverClassName);
                    String url = p.getProperty(Jdbc.url);
                    String username = p.getProperty(OSConnectCommand.username);
                    String password = p.getProperty(OSConnectCommand.password);

                    if (StringUtils.isBlank(driver)) {
                        Connection conn = Jdbc.getConnection(url, username, password);
                        dao.setConnection(conn, true);
                    } else {
                        ClassUtils.loadClass(driver);
                        Connection conn = Jdbc.getConnection(url, username, password);
                        DatabaseConfigurationContainer container = context.getContainer().getBean(DatabaseConfigurationContainer.class);
                        container.add(new StandardDatabaseConfiguration(context.getFactory().getContext(), null, driver, url, username, password, null, null, null, null, null));
                        dao.setConnection(conn, true);
                    }
                } else { // 使用默认数据库连接
                    dao.setConnection(dataSource.getDao().getConnection(), false);
                }

                // 查询数据库表信息
                String fulltableName = this.attributes.get("table");
                String tableName = Jdbc.removeSchema(fulltableName);
                String schema = Jdbc.getSchema(fulltableName);
                this.table = dao.getTable(dataSource.getDao().getCatalog(), schema, tableName);
                DatabaseTableColumnList columns = this.table.getColumns();

                // 设置索引字段的位置信息
                this.indexPosition = new int[indexs.length];
                for (int i = 0; i < indexs.length; i++) {
                    DatabaseTableColumn column = null;
                    if ((column = columns.getColumn(indexs[i])) == null) {
                        this.indexPosition[i] = Integer.parseInt(indexs[i]);
                    } else {
                        this.indexPosition[i] = column.getPosition();
                    }
                }

                // 设置比较字段的位置信息
                this.comparePosition = new int[compares.length];
                for (int i = 0; i < compares.length; i++) {
                    DatabaseTableColumn column = null;
                    if ((column = columns.getColumn(compares[i])) != null) {
                        this.comparePosition[i] = column.getPosition();
                    } else {
                        this.comparePosition[i] = Integer.parseInt(compares[i]);
                    }
                }

                // 替换字段
                for (int i = 0; i < newchg.length; i++) {
                    this.newchg.add(context.getContainer().getBean(IncrementReplace.class, columns, newchg[i], this.analysis));
                }

                // 替换字段
                for (int i = 0; i < updchg.length; i++) {
                    this.updchg.add(context.getContainer().getBean(IncrementReplace.class, columns, updchg[i], this.analysis));
                }

                // 替换字段
                for (int i = 0; i < delchg.length; i++) {
                    this.delchg.add(context.getContainer().getBean(IncrementReplace.class, columns, delchg[i], this.analysis));
                }
            } finally {
                dao.rollback();
                dao.close();
            }
        } else {
            // 设置索引字段的位置信息
            this.indexPosition = new int[indexs.length];
            for (int i = 0; i < indexs.length; i++) {
                this.indexPosition[i] = Integer.parseInt(indexs[i]);
            }

            // 设置比较字段的位置信息
            this.comparePosition = new int[compares.length];
            for (int i = 0; i < compares.length; i++) {
                this.comparePosition[i] = Integer.parseInt(compares[i]);
            }

            // 替换字段
            for (int i = 0; i < newchg.length; i++) {
                this.newchg.add(this.context.getContainer().getBean(IncrementReplace.class, newchg[i], this.analysis));
            }

            // 替换字段
            for (int i = 0; i < updchg.length; i++) {
                this.updchg.add(this.context.getContainer().getBean(IncrementReplace.class, updchg[i], this.analysis));
            }

            // 替换字段
            for (int i = 0; i < delchg.length; i++) {
                this.delchg.add(this.context.getContainer().getBean(IncrementReplace.class, delchg[i], this.analysis));
            }
        }
    }

    /**
     * 解析排序参数
     */
    public TableFileSortContext createSortContext() {
        TableFileSortContext cxt = new TableFileSortContext();
        if (this.attributes.containsKey("sortcache")) {
            cxt.setWriterBuffer(this.getIntAttribute("sortcache"));
        }
        if (this.attributes.containsKey("sortrows")) {
            cxt.setMaxRows(this.getIntAttribute("sortrows"));
        }
        if (this.attributes.containsKey("sortThread")) {
            cxt.setThreadNumber(this.getIntAttribute("sortThread"));
        }
        if (this.attributes.containsKey("sortReadBuf")) {
            cxt.setReaderBuffer(this.getIntAttribute("sortReadBuf"));
        }
        if (this.attributes.containsKey("maxfile")) {
            cxt.setFileCount(this.getIntAttribute("maxfile"));
        }
        if (this.attributes.containsKey("keeptemp")) {
            cxt.setDeleteFile(false);
        }
        cxt.setKeepSource(!this.attributes.containsKey("covsrc"));
        cxt.setThreadSource(this.context.getContainer().getBean(ThreadSource.class));
        return cxt;
    }

    /**
     * 创建一个表格型文件
     *
     * @return
     */
    public TextTableFile createTableFile() {
        return this.createTableFile(this.filetype);
    }

    /**
     * 创建一个表格型文件
     *
     * @return
     */
    public TextTableFile createTableFile(String filetype) {
        TextTableFile file = this.context.getContainer().getBean(TextTableFile.class, filetype, this);
        if (file == null) {
            throw new UnsupportedOperationException(filetype);
        } else {
            file.setAbsolutePath(this.filepath);
        }

        // 使用数据库中的字段名作为表格的列名
        if (this.table != null) {
            DatabaseTableColumnList list = this.table.getColumns();
            for (DatabaseTableColumn column : list) {
                int position = column.getPosition();
                if (StringUtils.isBlank(file.getColumnName(position))) {
                    file.setColumnName(position, column.getName());
                }
            }
        }
        return file;
    }

    public boolean contains(String key) {
        return this.attributes.containsKey(key);
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * 返回整数型属性值
     *
     * @param key 属性名（大小写不敏感）
     * @return
     */
    public int getIntAttribute(String key) {
        return Integer.parseInt(this.attributes.get(key));
    }

    /**
     * 返回索引字段的位置信息
     *
     * @return
     */
    public int[] getIndexPosition() {
        return indexPosition;
    }

    /**
     * 返回比较字段的位置信息
     *
     * @return
     */
    public int[] getComparePosition() {
        return comparePosition;
    }

    /**
     * 返回将记录写入输出流之前的字段处理逻辑
     *
     * @return
     */
    public List<IncrementReplace> getNewChg() {
        return newchg;
    }

    /**
     * 返回将记录写入输出流之前的字段处理逻辑
     *
     * @return
     */
    public List<IncrementReplace> getUpdChg() {
        return updchg;
    }

    /**
     * 返回将记录写入输出流之前的字段处理逻辑
     *
     * @return
     */
    public List<IncrementReplace> getDelChg() {
        return delchg;
    }

    /**
     * 返回输出流的种类: <br>
     * <br>
     * 空字符表示输出全部增量数据 <br>
     * new 表示只输出新增数据 <br>
     * upd 表示只输出修改数据 <br>
     * del 表示只输出删除数据 <br>
     * log 表示只删除日志数据 <br>
     *
     * @return
     */
    public Set<String> getKinds() {
        return kinds;
    }

    /**
     * 返回数据文件类型
     *
     * @return
     */
    public String getFiletype() {
        return filetype;
    }

    /**
     * 返回数据文件路径表达式
     *
     * @return
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * 返回 true 表示是日志输出流表达式
     *
     * @return
     */
    public boolean isLogWriter() {
        return isLogExpr;
    }

}
