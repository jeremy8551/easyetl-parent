package icu.etl.database;

import java.util.Properties;

public interface DatabaseURL extends Cloneable {

    /**
     * 数据库厂家类型, 如: db2 oracle mysql
     *
     * @return 数据库厂家类型
     */
    String getType();

    /**
     * 返回数据库名
     *
     * @return 返回数据库名
     */
    String getDatabaseName();

    /**
     * 用户名
     *
     * @return 用户名
     */
    String getUsername();

    /**
     * 用户密码
     *
     * @return 用户密码
     */
    String getPassword();

    /**
     * 数据库当前schema
     *
     * @return 表模式
     */
    String getSchema();

    /**
     * 数据库host
     *
     * @return 数据库host
     */
    String getHostname();

    /**
     * 数据库访问端口
     *
     * @return 数据库访问端口
     */
    String getPort();

    /**
     * 数据库服务名
     *
     * @return 数据库服务名
     */
    String getServerName();

    /**
     * Oracle数据库的sid
     *
     * @return Oracle数据库的sid
     */
    String getSID();

    /**
     * oracle数据库驱动类型thin
     *
     * @return oracle数据库驱动类型thin
     */
    String getDriverType();

    /**
     * 返回属性值
     *
     * @param name 属性名
     * @return 返回属性值
     */
    String getAttribute(String name);

    /**
     * 保存属性值
     *
     * @param name  属性名
     * @param value 属性值
     */
    void setAttribute(String name, String value);

    /**
     * JDBC的URL值
     *
     * @return JDBC的URL值
     */
    String toString();

    /**
     * 转为属性集合
     *
     * @return 属性集合
     */
    Properties toProperties();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseURL clone();

}
