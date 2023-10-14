package icu.etl.database;

public interface DatabaseSpace extends Cloneable {

    /**
     * 表空间名
     *
     * @return
     */
    String getName();

    /**
     * 复制一个表空间
     *
     * @return
     */
    DatabaseSpace clone();

}