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
     * @return
     */
    String getId();

    /**
     * 返回存储过程全名
     *
     * @return
     */
    String getFullName();

    /**
     * 存储过程归属 catalog
     *
     * @return
     */
    String getCatalog();

    /**
     * 存储过程归属schema
     *
     * @return
     */
    String getSchema();

    /**
     * 存储过程名
     *
     * @return
     */
    String getName();

    /**
     * 存储过程语言
     *
     * @return
     */
    String getLanguage();

    /**
     * 存储过程创建用户名
     *
     * @return
     */
    String getCreator();

    /**
     * 存储过程创建时间
     *
     * @return
     */
    Date getCreateTime();

    /**
     * 存储过程参数集合, 按参数顺序排序
     *
     * @return
     */
    DatabaseProcedureParameterList getParameters();

    /**
     * 返回调用存储过程的语句，例如：call procedure(?, ?)
     *
     * @return
     */
    String toCallProcedureSql();

    /**
     * 生成 call produrcName(?) 表达式字符串
     *
     * @return
     */
    String toCallProcedureString();

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseProcedure clone();

}