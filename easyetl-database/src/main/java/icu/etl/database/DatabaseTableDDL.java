package icu.etl.database;

public interface DatabaseTableDDL {

    /**
     * 返回数据库表的 DDL 语句
     *
     * @return DDL语句
     */
    String getTable();

    /**
     * 返回数据库表的注释信息
     *
     * @return 注释信息
     */
    DatabaseDDL getComment();

    /**
     * 返回数据库表上索引的 DDL 语句
     *
     * @return 索引上的DDL语句
     */
    DatabaseDDL getIndex();

    /**
     * 返回数据库表上主键的 DDL 语句
     *
     * @return 主键上的DDL语句
     */
    DatabaseDDL getPrimaryKey();

}
