package icu.etl.database;

public interface DatabaseTableDDL {

    /**
     * 返回数据库表的 DDL 语句
     *
     * @return
     */
    String getTable();

    /**
     * 返回数据库表的注释信息
     *
     * @return
     */
    DatabaseDDL getComment();

    /**
     * 返回数据库表上索引的 DDL 语句
     *
     * @return
     */
    DatabaseDDL getIndex();

    /**
     * 返回数据库表上主键的 DDL 语句
     *
     * @return
     */
    DatabaseDDL getPrimaryKey();

}
