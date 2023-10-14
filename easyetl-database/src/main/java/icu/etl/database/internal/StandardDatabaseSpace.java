package icu.etl.database.internal;

import icu.etl.database.DatabaseSpace;

/**
 * 数据库表空间
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-10
 */
public class StandardDatabaseSpace implements DatabaseSpace {

    /**
     * 表空间名
     */
    private String name;

    /**
     * 初始化
     */
    public StandardDatabaseSpace() {
    }

    /**
     * 初始化
     *
     * @param name 表空间名
     */
    public StandardDatabaseSpace(String name) {
        this.name = name;
    }

    /**
     * 表空间名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 表空间名
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public StandardDatabaseSpace clone() {
        return new StandardDatabaseSpace(this.name);
    }

    public String toString() {
        return this.name;
    }

}
