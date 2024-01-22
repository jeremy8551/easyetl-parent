package icu.etl.database;

import icu.etl.os.OSConfiguration;

/**
 * 操作系统配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-29
 */
public interface DatabaseConfiguration extends Cloneable, OSConfiguration {

    /**
     * 数据库驱动类名
     *
     * @return 驱动类名
     */
    String getDriverClass();

    /**
     * 数据库URL信息
     *
     * @return URL信息
     */
    String getUrl();

    /**
     * 返回一个 JDBC 配置信息副本
     *
     * @return 数据库配置信息
     */
    DatabaseConfiguration clone();

}
