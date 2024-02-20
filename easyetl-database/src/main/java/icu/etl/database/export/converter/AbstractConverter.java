package icu.etl.database.export.converter;

import java.sql.ResultSet;
import java.util.Map;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcObjectConverter;

public abstract class AbstractConverter implements JdbcObjectConverter {

    /** 对应的字段位置，从1开始 */
    public final static String PARAM_COLUMN = "position";

    /** 缓冲区大小 */
    public final static String PARAM_BUFFER = "buffer";

    /** 数据库查询结果集对象 */
    public final static String PARAM_RESULT = "resultSet";

    /** 数据库操作对象 */
    public final static String PARAM_JDBCDAO = "jdbcDao";

    /** 对应的字段名 */
    public final static String PARAM_COLNAME = "columnName";

    /** 字段间的分隔符 */
    public final static String PARAM_COLDEL = "columnDelimiter";

    /** 字符串二端的限定符 */
    public final static String PARAM_CHARDEL = "chardel";

    /** 字符串的转义字符 */
    public final static String PARAM_ESCAPE = "escape";

    /** 字符串中需要进行转义的所有字符集合 */
    public final static String PARAM_ESCAPES = "escapes";

    /** 字符串中需要进行过滤的所有字符集合 */
    public final static String PARAM_CHARHIDE = "charhide";

    /** 字符串的字符集 */
    public final static String PARAM_CHARSET = "charset";

    /** 日期格式 */
    public final static String PARAM_DATEFORMAT = "dateformat";

    /** 时间格式 */
    public final static String PARAM_TIMEFORMAT = "timeformat";

    /** 时间撮格式 */
    public final static String PARAM_TIMESTAMPFORMAT = "timestampformat";

    /** 处理字符串中的乱码 */
    public final static String PARAM_MESSY = "messy";

    /** 参数名 */
    private Map<String, Object> attributes;

    /** 处理结果集的列数 */
    protected int column;

    /** 字段间分隔符 */
    protected char coldel;

    /** 字段名 */
    protected String columnName;

    /** 查询结果集 */
    protected ResultSet resultSet;

    /** 流输出流字符缓冲区 */
    protected String[] array;

    /** 数据库操作工具 */
    protected JdbcDao dao;

    /**
     * 初始化
     */
    public AbstractConverter() {
        this.attributes = new CaseSensitivMap<Object>();
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);

        if (AbstractConverter.PARAM_COLUMN.equalsIgnoreCase(key) && (value instanceof Integer)) {
            this.column = ((Integer) value).intValue();
        } else if (AbstractConverter.PARAM_BUFFER.equalsIgnoreCase(key) && (value instanceof String[])) {
            this.array = (String[]) value;
        } else if (AbstractConverter.PARAM_RESULT.equalsIgnoreCase(key) && (value instanceof ResultSet)) {
            this.resultSet = (ResultSet) value;
        } else if (AbstractConverter.PARAM_COLDEL.equalsIgnoreCase(key) && (value instanceof String)) {
            this.coldel = ((String) value).charAt(0);
        } else if (AbstractConverter.PARAM_COLNAME.equalsIgnoreCase(key) && (value instanceof String)) {
            this.columnName = (String) value;
        } else if (AbstractConverter.PARAM_JDBCDAO.equalsIgnoreCase(key) && (value instanceof JdbcDao)) {
            this.dao = ((JdbcDao) value);
        }
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public boolean contains(String key) {
        return this.attributes.containsKey(key);
    }

    public abstract void init() throws Exception;

    public abstract void execute() throws Exception;

}
