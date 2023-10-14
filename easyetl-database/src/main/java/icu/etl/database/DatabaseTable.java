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
     * @return
     */
    String getName();

    /**
     * 返回类别信息
     *
     * @return
     */
    String getCatalog();

    /**
     * 数据库表的模式名
     *
     * @return
     */
    String getSchema();

    /**
     * 返回表的完全限定名
     *
     * @return
     */
    String getFullName();

    /**
     * 返回表说明信息
     *
     * @return
     */
    String getRemark();

    /**
     * 返回表类型: "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *
     * @return
     */
    String getType();

    /**
     * 字段个数
     *
     * @return
     */
    int columns();

    /**
     * 表的所有索引
     *
     * @return
     */
    DatabaseIndexList getIndexs();

    /**
     * 表的所有字段信息
     *
     * @return
     */
    DatabaseTableColumnList getColumns();

    /**
     * 表空间
     *
     * @return
     */
    DatabaseSpaceList getTableSpaces();

    /**
     * 索引空间
     *
     * @return
     */
    DatabaseSpaceList getIndexSpaces();

    /**
     * 表的主键信息
     *
     * @return
     */
    DatabaseIndexList getPrimaryIndexs();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseTable clone();

}