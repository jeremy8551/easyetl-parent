package icu.etl.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import icu.etl.annotation.EasyBean;
import icu.etl.database.internal.StandardDatabaseConfigurationContainer;
import icu.etl.os.OSConnectCommand;

@EasyBean(builder = StandardDatabaseConfigurationContainer.class)
public interface DatabaseConfigurationContainer {

    /**
     * 注册数据库 JDBC 连接信息
     *
     * @param config 数据库连接配置
     */
    void add(DatabaseConfiguration config);

    /**
     * 注册数据库 JDBC 连接信息 <br>
     * 属性中必须包括: <br>
     * {@linkplain Jdbc#driverClassName} <br>
     * {@linkplain Jdbc#url} <br>
     * {@linkplain OSConnectCommand#username} <br>
     * {@linkplain OSConnectCommand#password} <br>
     * <br>
     * 可选: <br>
     * {@linkplain Jdbc#admin} <br>
     * {@linkplain Jdbc#adminPw} <br>
     *
     * @param config JDBC 配置信息
     * @return 转换后的 JDBC 配置信息
     */
    DatabaseConfiguration add(Properties config);

    /**
     * 查询数据库的用户信息
     *
     * @param hostname 数据库域名
     * @param port     端口
     * @param database 数据库名
     * @return 数据库配置信息
     */
    DatabaseConfiguration get(String hostname, String port, String database);

    /**
     * 查询数据库连接对应的 JDBC 配置信息
     *
     * @param conn 数据库连接
     * @return 数据库配置信息
     * @throws SQLException 数据库错误
     */
    DatabaseConfiguration get(Connection conn) throws SQLException;

}