package icu.etl.database;

public interface DatabaseSpace extends Cloneable {

    /**
     * 表空间名
     *
     * @return 表空间名
     */
    String getName();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseSpace clone();

}