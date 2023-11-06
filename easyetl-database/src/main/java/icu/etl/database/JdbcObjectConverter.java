package icu.etl.database;

import java.io.IOException;
import java.sql.SQLException;

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
     * @throws IOException
     * @throws SQLException
     */
    void init() throws IOException, SQLException;

    /**
     * 读取字段值，对字段值执行数据清洗操作，将字段值保存到缓存中
     *
     * @throws IOException
     * @throws SQLException
     */
    void execute() throws IOException, SQLException;

}