package icu.etl.database;

import java.util.Date;

public interface DatabaseProcedure extends Cloneable {

    /** 表示参数为外部输入 */
    int PARAM_IN_MODE = 0;

    /** 表示参数为输出型 */
    int PARAM_OUT_MODE = 1;

    /** 表示参数既可以做输入型也可做输出型 */
    int PARAM_INOUT_MODE = 2;

    /**
     * 存储过程id
     *
     * @return 存储过程id
     */
    String getId();

    /**
     * 返回存储过程全名
     *
     * @return 存储过程全名
     */
    String getFullName();

    /**
     * 存储过程的编目
     *
     * @return 编目
     */
    String getCatalog();

    /**
     * 存储过程归属schema
     *
     * @return 模式
     */
    String getSchema();

    /**
     * 存储过程名
     *
     * @return 存储过程名
     */
    String getName();

    /**
     * 存储过程语言
     *
     * @return 存储过程语言
     */
    String getLanguage();

    /**
     * 存储过程创建用户名
     *
     * @return 存储过程创建用户名
     */
    String getCreator();

    /**
     * 存储过程创建时间
     *
     * @return 存储过程创建时间
     */
    Date getCreateTime();

    /**
     * 存储过程参数集合, 按参数顺序排序
     *
     * @return 存储过程参数集合
     */
    DatabaseProcedureParameterList getParameters();

    /**
     * 返回调用存储过程的语句，例如：call procedure(?, ?)
     *
     * @return 调用存储过程的语句
     */
    String toCallProcedureSql();

    /**
     * 生成 call produrcName(?) 表达式字符串
     *
     * @return 表达式
     */
    String toCallProcedureString();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseProcedure clone();

}