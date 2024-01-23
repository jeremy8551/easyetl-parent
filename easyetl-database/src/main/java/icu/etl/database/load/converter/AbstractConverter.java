package icu.etl.database.load.converter;

import java.sql.PreparedStatement;
import java.util.Map;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.JdbcStringConverter;

public abstract class AbstractConverter implements JdbcStringConverter {

    /** 表示字段处理程序 */
    public final static String STATEMENT = "STATEMENTNAME";

    /** 表示字段的位置信息 */
    public final static String POSITION = "POSITION";

    /** 表示字段名 */
    public final static String COLUMNNAME = "name";

    /** 表示字段个数 */
    public final static String COLUMNSIZE = "columnSize";

    /** 表示字段是否可以不为null */
    public final static String ISNOTNULL = "notNull";

    /** 表示是否有固定值 */
    public final static String fixValue = "fixValue";

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

    /** 属性集合 */
    private Map<String, Object> attributes;

    /** 数据库处理 */
    protected PreparedStatement statement;

    /** 数据库表字段是否可以为null，true-不能为Null */
    protected boolean notNull;

    /** 插入字段的序号, 从1开始 */
    protected int position;

    /** 字段名 */
    protected String name;

    /** 固定值, 忽略实际值直接使用固定值更新到数据库 */
    protected String fixedValue;

    /** true表示使用固定值 */
    protected boolean useFixedValue;

    /**
     * 初始化
     */
    public AbstractConverter() {
        this.attributes = new CaseSensitivMap<Object>();
        this.position = 0;
        this.notNull = false;
        this.name = "";
        this.useFixedValue = false;
        this.fixedValue = "";
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);

        if (value instanceof PreparedStatement) {
            this.statement = (PreparedStatement) value;
        } else if (AbstractConverter.COLUMNNAME.equalsIgnoreCase(key)) {
            this.name = (String) value;
        } else if (AbstractConverter.POSITION.equalsIgnoreCase(key)) {
            this.position = ((Integer) value).intValue();
        } else if (AbstractConverter.ISNOTNULL.equalsIgnoreCase(key)) {
            this.notNull = true;
        } else if (AbstractConverter.fixValue.equalsIgnoreCase(key)) {
            this.fixedValue = (String) value;
            this.useFixedValue = true;
        }
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public boolean contains(String key) {
        return this.attributes.containsKey(key);
    }

    /**
     * 判断字符串是否为空白字符串
     *
     * @param str 字符串
     * @return 返回true表示字符串参数是空白或null
     */
    protected boolean isBlank(String str) {
        for (int i = 0, length = str.length(); i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
