package icu.etl.database;

import java.util.Properties;

public interface DatabaseURL extends Cloneable {

    /**
     * 数据库厂家类型, 如: db2 oracle mysql
     *
     * @return
     */
    String getType();

    /**
     * 返回数据库名, 如: UDSFDB 等
     *
     * @return
     */
    String getDatabaseName();

    /**
     * 用户名
     *
     * @return
     */
    String getUsername();

    /**
     * 用户密码
     *
     * @return
     */
    String getPassword();

    /**
     * 数据库当前schema
     *
     * @return
     */
    String getSchema();

    /**
     * 数据库host
     *
     * @return
     */
    String getHostname();

    /**
     * 数据库访问端口
     *
     * @return
     */
    String getPort();

    /**
     * 数据库服务名
     *
     * @return
     */
    String getServerName();

    /**
     * Oracle数据库的 sid
     *
     * @return
     */
    String getSID();

    /**
     * oracle数据库驱动类型 thin
     *
     * @return
     */
    String getDriverType();

    /**
     * 返回属性值
     *
     * @param name 属性名
     * @return
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
     * JDBC URL 值
     *
     * @return
     */
    String toString();

    /**
     * 转为属性集合
     *
     * @return
     */
    Properties toProperties();

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseURL clone();

}
