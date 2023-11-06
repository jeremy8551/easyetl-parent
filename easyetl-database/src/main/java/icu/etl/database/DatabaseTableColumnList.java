package icu.etl.database;

import java.util.List;

public interface DatabaseTableColumnList extends Cloneable, List<DatabaseTableColumn> {

    /**
     * 返回字段信息
     *
     * @param position 从1开始
     * @return
     */
    DatabaseTableColumn getColumn(int position);

    /**
     * 返回字段信息
     *
     * @param name 字段名（大小写敏感）
     * @return
     */
    DatabaseTableColumn getColumn(String name);

    /**
     * 返回字段名数组
     *
     * @return
     */
    String[] getColumnNames();

    /**
     * 返回字段索引位置(从1开始)数组
     *
     * @return
     */
    int[] getColumnPositions();

    /**
     * 搜索表列的信息
     *
     * @param name 列名
     * @return 列的信息，不存在返回null
     */
    DatabaseTableColumn indexOfColumn(String name);

    /**
     * 搜索表列的信息
     *
     * @param names 列名数组
     * @return
     */
    DatabaseTableColumnList indexOfColumns(String... names);

    /**
     * 对比字段
     *
     * @param list 数据库表字段集合
     * @return
     */
    int compareTo(DatabaseTableColumnList list);

    /**
     * 转为数组，其中元素都是副本
     */
    DatabaseTableColumn[] toArray();

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseTableColumnList clone();

}
