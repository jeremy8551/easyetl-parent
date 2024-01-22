package icu.etl.database;

import java.util.List;

/**
 * 数据库索引信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-10
 */
public interface DatabaseIndex extends Cloneable, Comparable<DatabaseIndex> {

    /** 正序排序 */
    int INDEX_ASC = 0;

    /** 倒序排序 */
    int INDEX_DESC = 1;

    /** 未明确排序 */
    int INDEX_UNKNOWN = 2;

    /**
     * 索引名字
     *
     * @return 索引名
     */
    String getName();

    /**
     * 返回索引全名
     *
     * @return 索引名
     */
    String getFullName();

    /**
     * 归属表名
     *
     * @return 表名
     */
    String getTableName();

    /**
     * 返回编目信息
     *
     * @return 编目信息
     */
    String getTableCatalog();

    /**
     * 表模式（可为 null）
     *
     * @return 表模式
     */
    String getTableSchema();

    /**
     * 返回表全名
     *
     * @return 表全名
     */
    String getTableFullName();

    /**
     * 索引schema
     *
     * @return 索引的模式名
     */
    String getSchema();

    /**
     * 是否是唯一索引
     *
     * @return true-唯一索引
     */
    boolean isUnique();

    /**
     * 索引中列名
     *
     * @return 列名
     */
    List<String> getColumnNames();

    /**
     * 返回索引中字段的位置信息（位置信息从 1 开始）
     *
     * @return 位置信息集合
     */
    List<Integer> getPositions();

    /**
     * 索引中列的排序方式
     *
     * @return {@linkplain #INDEX_ASC} 正序排序 <br>
     * {@linkplain #INDEX_DESC} 倒序排序 <br>
     * {@linkplain #INDEX_UNKNOWN} 未明确 <br>
     */
    List<Integer> getDirections();

    /**
     * 生成一个副本
     *
     * @return 副本
     */
    DatabaseIndex clone();

    /**
     * 判断索引索引内容是否相等
     *
     * @param index           索引
     * @param ignoreIndexName true表示忽略字段名大小写不同
     * @param ignoreIndexSort true表示忽略字段排序方式不同
     * @return 返回true表示相等 false表示不等
     */
    boolean equals(DatabaseIndex index, boolean ignoreIndexName, boolean ignoreIndexSort);

}