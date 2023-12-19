package icu.etl.database;

import icu.etl.util.Attribute;

/**
 * 将 JDBC 字段类型转为字符串
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-12
 */
public interface JdbcObjectConverter extends Attribute<Object> {

    /**
     * 在执行字符处理逻辑之前执行的准备工作逻辑
     *
     * @throws Exception 发生错误
     */
    void init() throws Exception;

    /**
     * 读取字段值，对字段值执行数据清洗操作，将字段值保存到缓存中
     *
     * @throws Exception 发生错误
     */
    void execute() throws Exception;

}