package icu.etl.script;

import javax.script.Bindings;

/**
 * 脚本引擎变量接口
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptVariable extends Bindings {

    /** 内置变量: 局部变量，用于查看当前数据库编目名 */
    public final static String VARNAME_CATALOG = "catalog";

    /** 内置变量: 脚本语句的字符集 */
    public final static String VARNAME_CHARSET = "charset";

    /** 内置变量: 异常信息 */
    public final static String VARNAME_EXCEPTION = "exception";

    /** 内置变量: sqlstate 值 */
    public final static String VARNAME_SQLSTATE = "sqlstate";

    /** 内置变量: errorcode 值 */
    public final static String VARNAME_ERRORCODE = "errorcode";

    /** 内置变量: exitcode 值 */
    public final static String VARNAME_EXITCODE = "exitcode";

    /** 内置变量: 发生错误的脚本命令 */
    public final static String VARNAME_ERRORSCRIPT = "errorscript";

    /** 内置变量: 最后一个sql语句的执行影响的记录数 */
    public final static String VARNAME_UPDATEROWS = "updateRows";

    /** 内置变量: 后台命令编号 */
    public final static String VARNAME_PID = "pid";

    /** 内置变量: 最后一个setp命令的参数值 */
    public final static String SESSION_VARNAME_STEP = "step";

    /** 内置变量: jump命令的标示变量 */
    public final static String SESSION_VARNAME_JUMP = "jump";

    /** 内置变量: 脚本语句中的行间分隔符 */
    public final static String SESSION_VARNAME_LINESEPARATOR = "lineSeparator";

    /** 内置变量: 脚本文件的绝对路径 */
    public final static String SESSION_VARNAME_SCRIPTFILE = "scriptFile";

    /** 内置变量: 脚本引擎名 */
    public final static String SESSION_VARNAME_SCRIPTNAME = "scriptName";

    /** 内置变量: 脚本引擎默认目录 */
    public final static String SESSION_VARNAME_PWD = "pwd";

    /** 内置变量: 临时文件存储目录 */
    public final static String SESSION_VARNAME_TEMP = "temp";

    /** 用户会话信息中的变量名：当前用户名 */
    public final static String SESSION_VARNAME_USER = "USER";

    /** 用户会话信息中的变量名：当前用户的根目录 */
    public final static String SESSION_VARNAME_HOME = "HOME";

    /** 用户会话最后一次发生的异常信息 */
    public final static String SESSION_VARNAME_LASTEXCEPTION = "VARNAME_LAST_EXCEPTION";

    /**
     * 添加变量集合
     *
     * @param bindings
     */
    void addAll(Bindings bindings);

}
