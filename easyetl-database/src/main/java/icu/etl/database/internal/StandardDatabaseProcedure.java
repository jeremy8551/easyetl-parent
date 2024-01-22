package icu.etl.database.internal;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import icu.etl.database.DatabaseException;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.DatabaseProcedureParameterList;
import icu.etl.database.JdbcDao;
import icu.etl.database.SQL;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库存储过程
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-05-09
 */
public class StandardDatabaseProcedure implements DatabaseProcedure {

    /**
     * 初始化
     *
     * @param dao JDBCDao
     * @param sql call PROCEDURE(...) 语句 或 create PROCEDURE {...}
     * @throws SQLException 数据库错误
     */
    public static DatabaseProcedure toProcedure(JdbcDao dao, String sql) throws SQLException {
        String[] names = null;
        if (StringUtils.startsWith(sql, "call", 0, true, true) && (names = resolveDatabaseProcedureCallName(sql)) != null) { // 解析 call procedure(...) 表达式
            String catalog = dao.getDialect().getCatalog(dao.getConnection());
            DatabaseProcedure obj = dao.getDialect().getProcedureForceOne(dao.getConnection(), catalog, names[0], names[1]);
            if (obj == null) {
                throw new NullPointerException();
            }

            DatabaseProcedureParameterList list = obj.getParameters();
            String[] array = StandardDatabaseProcedure.resolveProcedureInputParameters(sql);
            if (array.length != list.size()) {
                throw new IllegalArgumentException(sql);
            }

            for (int i = 0; i < list.size(); i++) {
                list.get(i).setExpression(array[i]);
            }
            return obj;
        } else if ((names = resolveDatabaseProcedureDDLName(sql)) != null) {
            String catalog = dao.getDialect().getCatalog(dao.getConnection());
            return dao.getDialect().getProcedureForceOne(dao.getConnection(), catalog, names[0], names[1]);
        } else {
            throw new IllegalArgumentException(ResourcesUtils.getMessage("database.standard.output.msg011", sql));
        }
    }

    /** 存储过程id */
    private String id;

    /** 存储过程全名 */
    private String fullName;

    /** 存储过程名 */
    private String name;

    /** 类别信息 */
    private String catalog;

    /** 存储过程归属schema */
    private String schema;

    /** 存储过程语言 */
    private String language;

    /** 存储过程创建用户名 */
    private String creator;

    /** 存储过程创建时间 */
    private Date creatTime;

    /** 存储过程参数, 按参数顺序排序 */
    private DatabaseProcedureParameterList parameters;

    /**
     * 初始化
     */
    public StandardDatabaseProcedure() {
        super();
        this.parameters = new StandardDatabaseProcedureParameterList();
    }

    /**
     * 存储过程id
     *
     * @return 存储过程id
     */
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getLanguage() {
        return language;
    }

    public String getCreator() {
        return creator;
    }

    public Date getCreateTime() {
        return creatTime;
    }

    public DatabaseProcedureParameterList getParameters() {
        return this.parameters;
    }

    /**
     * 存储过程id
     *
     * @param id 存储过程id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 存储过程名
     *
     * @param name 存储过程名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 存储过程归属schema
     *
     * @param schema 存储过程归属schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 存储过程语言
     *
     * @param language 存储过程语言
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * 存储过程创建用户名
     *
     * @param creator 存储过程创建用户名
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 存储过程创建时间
     *
     * @param creatTime 存储过程创建时间
     */
    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    /**
     * 存储过程参数, 按参数顺序排序
     *
     * @param list 存储过程参数, 按参数顺序排序
     */
    public void setParameters(List<DatabaseProcedureParameter> list) {
        this.parameters.addAll(list);
    }

    public String toCallProcedureSql() {
        List<DatabaseProcedureParameter> list = this.getParameters();
        String str = "call " + this.fullName + "(";
        for (int i = 0, last = list.size() - 1, j = 0; i < list.size(); i++) {
            DatabaseProcedureParameter obj = list.get(i);
            if (obj.getMode() == DatabaseProcedure.PARAM_OUT_MODE) {
                str += "?";
                obj.setPlaceholder(++j);
            } else if (obj.getMode() == DatabaseProcedure.PARAM_INOUT_MODE) {
                str += "?";
                obj.setPlaceholder(++j);
            } else {
                str += obj.getExpression();
            }

            if (i < last) {
                str += ", ";
            }
        }
        str += ")";
        return str;
    }

    public String toCallProcedureString() {
        StringBuilder buf = new StringBuilder();
        buf.append("call ");
        buf.append(this.fullName);
        buf.append("(");
        Iterator<DatabaseProcedureParameter> it = this.parameters.iterator();
        while (it.hasNext()) {
            DatabaseProcedureParameter dpp = it.next();
            if (dpp.getExpression() == null) {
                buf.append(dpp.getName());
                buf.append(" ");
                if (dpp.getMode() == DatabaseProcedure.PARAM_IN_MODE) {
                    buf.append("IN");
                } else if (dpp.getMode() == DatabaseProcedure.PARAM_INOUT_MODE) {
                    buf.append("IN OUT");
                } else if (dpp.getMode() == DatabaseProcedure.PARAM_OUT_MODE) {
                    buf.append("OUT");
                } else {
                    buf.append(dpp.getMode());
                }
                buf.append(" ");
                buf.append(dpp.getFieldType());
                buf.append("");
                if (dpp.length() > 0) {
                    if (dpp.getScale() > 0) {
                        buf.append("(").append(dpp.length()).append(", ").append(dpp.getScale()).append(")");
                    } else {
                        buf.append("(").append(dpp.length()).append(")");
                    }
                }
            } else {
                buf.append(dpp.getExpression());
            }
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * 解析字符串内第一个大括号内用半角逗号分割的字段
     *
     * @param str 存储过程参数, 如: ('1997-07-01', (select val from table), 12)
     * @return 逗号分割的字段数组
     */
    private static String[] resolveProcedureInputParameters(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        int begin = str.indexOf('(');
        if (begin == -1) {
            throw new IllegalArgumentException(str);
        }

        int end = SQL.indexOfParenthes(str, begin);
        if (end == -1) {
            throw new IllegalArgumentException(str);
        }

        String parameters = str.substring(begin + 1, end);
        String[] array = SQL.split(parameters, ',');
        return StringUtils.trimBlank(array);
    }

    /**
     * 解析数据库存储过程ddl语句中的schema与name
     *
     * @param ddl 数据库存储过程ddl语句
     * @return 第一位表示schema 第二位表示存储过程名
     */
    public static String[] resolveDatabaseProcedureDDLName(String ddl) {
        if (StringUtils.isBlank(ddl)) {
            return null;
        }

        int end = ddl.indexOf('(');
        if (end == -1) {
            return null;
        }

        String prefix = "PROCEDURE";
        String str = ddl.toUpperCase();
        int begin = str.lastIndexOf(prefix, end);
        if (begin == -1) {
            return null;
        }

        String fullName = ddl.substring(begin + prefix.length(), end);
        String[] result = new String[2];
        String[] split = StringUtils.trimBlank(StringUtils.split(fullName, '.'));
        if (split.length == 1) {
            result[0] = null;
            result[1] = split[0];
        } else if (split.length == 2) {
            result[0] = split[0];
            result[1] = split[1];
        } else {
            throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg013", fullName));
        }
        return result;
    }

    /**
     * 解析call schema.name()语句中的schema与name
     *
     * @param call 数据库存储过程ddl语句
     * @return 第一位表示schema（如果不存在则为null） 第二位表示存储过程名
     */
    protected static String[] resolveDatabaseProcedureCallName(String call) {
        if (StringUtils.isBlank(call)) {
            return null;
        }

        int index = call.toLowerCase().indexOf("call");
        if (index == -1) {
            return null;
        }

        int end = call.indexOf('(', index);
        if (end == -1) {
            return null;
        }

        String fullName = call.substring(index + 4, end);
        String[] result = new String[2];
        String[] split = StringUtils.trimBlank(StringUtils.split(fullName, '.'));
        if (split.length == 1) {
            result[0] = null;
            result[1] = split[0];
        } else if (split.length == 2) {
            result[0] = split[0];
            result[1] = split[1];
        } else {
            throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg013", fullName));
        }
        return result;
    }

    public DatabaseProcedure clone() {
        StandardDatabaseProcedure obj = new StandardDatabaseProcedure();
        obj.id = this.id;
        obj.name = this.name;
        obj.fullName = this.fullName;
        obj.catalog = this.catalog;
        obj.schema = this.schema;
        obj.language = this.language;
        obj.creator = this.creator;
        obj.creatTime = this.creatTime;
        obj.parameters = this.parameters.clone();
        return obj;
    }

    public String toString() {
        return this.fullName;
    }

}
