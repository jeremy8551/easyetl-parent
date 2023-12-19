package icu.etl.database;

import java.sql.PreparedStatement;

import icu.etl.util.Attribute;

/**
 * 将字符串转为数据库 JDBC 驱动支持的类型
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-12
 */
public interface JdbcStringConverter extends Attribute<Object> {

    /**
     * 在执行字符处理逻辑之前执行的准备工作逻辑
     *
     * @throws Exception 发生错误
     */
    void init() throws Exception;

    /**
     * 将字符串参数 value 转为数据库支持的类型 <br>
     * 将转换后的对象使用 {@linkplain PreparedStatement#setString(int, String)} 等 setXXX 接口发送到数据库端
     *
     * @param value 字符串
     * @throws Exception 发生错误
     */
    void execute(String value) throws Exception;

}