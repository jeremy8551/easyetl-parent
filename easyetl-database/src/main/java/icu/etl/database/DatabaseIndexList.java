package icu.etl.database;

import java.util.List;

public interface DatabaseIndexList extends Cloneable, List<DatabaseIndex> {

    /**
     * 判断数据库表中是否存在指定索引index
     *
     * @param index           数据库索引信息
     * @param ignoreIndexName true表示忽略字段名大小写不同
     * @param ignoreIndexSort true表示忽略字段排序方式不同
     * @return 返回true表示存在索引 false表示不存在索引
     */
    boolean contains(DatabaseIndex index, boolean ignoreIndexName, boolean ignoreIndexSort);

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseIndexList clone();

}
