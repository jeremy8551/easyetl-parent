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
    public final static int INDEX_ASC = 0;

    /** 倒序排序 */
    public final static int INDEX_DESC = 1;

    /** 未明确排序 */
    public final static int INDEX_UNKNOWN = 2;

    /**
     * 索引名字
     *
     * @return
     */
    String getName();

    /**
     * 返回索引全名
     *
     * @return
     */
    String getFullName();

    /**
     * 归属表名
     *
     * @return
     */
    String getTableName();

    /**
     * 返回类型信息
     *
     * @return
     */
    String getTableCatalog();

    /**
     * 表模式（可为 null）
     *
     * @return
     */
    String getTableSchema();

    /**
     * 返回表全名
     *
     * @return
     */
    String getTableFullName();

    /**
     * 索引schema
     *
     * @return
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
     * @return
     */
    List<String> getColumnNames();

    /**
     * 返回索引中字段的位置信息（位置信息从 1 开始）
     *
     * @return
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
     * @return
     */
    DatabaseIndex clone();

    /**
     * 判断索引索引内容是否相等
     *
     * @param index           索引
     * @param ignoreIndexName true表示忽略字段名大小写不同
     * @param ignoreIndexSort true表示忽略字段排序方式不同
     * @return
     */
    boolean equals(DatabaseIndex index, boolean ignoreIndexName, boolean ignoreIndexSort);

}