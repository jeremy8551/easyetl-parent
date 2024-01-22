package icu.etl.database;

/**
 * 数据库表信息
 *
 * @author jeremy8551@qq.com
 */
public interface DatabaseTable extends Cloneable {

    /**
     * 表名
     *
     * @return 表名
     */
    String getName();

    /**
     * 返回编目信息
     *
     * @return 编目
     */
    String getCatalog();

    /**
     * 数据库表的模式名
     *
     * @return 模式名
     */
    String getSchema();

    /**
     * 返回表的完全限定名
     *
     * @return 限定名
     */
    String getFullName();

    /**
     * 返回表说明信息
     *
     * @return 表的说明信息
     */
    String getRemark();

    /**
     * 返回表类型: "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *
     * @return 类类型
     */
    String getType();

    /**
     * 字段个数
     *
     * @return 字段个数
     */
    int columns();

    /**
     * 表的所有索引
     *
     * @return 索引集合
     */
    DatabaseIndexList getIndexs();

    /**
     * 表的所有字段信息
     *
     * @return 字段集合
     */
    DatabaseTableColumnList getColumns();

    /**
     * 表空间
     *
     * @return 表空间
     */
    DatabaseSpaceList getTableSpaces();

    /**
     * 索引空间
     *
     * @return 索引空间
     */
    DatabaseSpaceList getIndexSpaces();

    /**
     * 表的主键信息
     *
     * @return 主键信息
     */
    DatabaseIndexList getPrimaryIndexs();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseTable clone();

}