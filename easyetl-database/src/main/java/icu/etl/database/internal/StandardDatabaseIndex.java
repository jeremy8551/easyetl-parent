package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.List;

import icu.etl.database.DB;
import icu.etl.database.DatabaseIndex;
import icu.etl.util.ResourcesUtils;

public class StandardDatabaseIndex implements DatabaseIndex {

    private String fullName;
    private String indexSchema;
    private String indexName;
    private String tableName;
    private String tableSchema;
    private String tableCatalog;
    private String tableFullName;
    private boolean unique;
    private List<Integer> indexPosition;
    private List<String> indexColumnName;
    private List<Integer> indexColumnSort;

    /**
     * 初始化
     */
    public StandardDatabaseIndex() {
        this.indexPosition = new ArrayList<Integer>();
        this.indexColumnName = new ArrayList<String>();
        this.indexColumnSort = new ArrayList<Integer>();
    }

    /**
     * 复制一个索引
     *
     * @param index
     */
    public StandardDatabaseIndex(DatabaseIndex index) {
        this();
        this.setName(index.getName());
        this.setTableName(index.getTableName());
        this.setTableSchema(index.getTableSchema());
        this.setTableCatalog(index.getTableCatalog());
        this.setTableFullName(index.getTableFullName());
        this.setUnique(index.isUnique());
        this.setFullName(index.getFullName());
        this.setSchema(index.getSchema());
        this.setColumnNames(index.getColumnNames());
        this.setPositions(index.getPositions());
        this.setSort(index.getDirections());
    }

    public String getTableFullName() {
        return tableFullName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSchema() {
        return indexSchema;
    }

    public List<Integer> getPositions() {
        return indexPosition;
    }

    public List<String> getColumnNames() {
        return this.indexColumnName;
    }

    public List<Integer> getDirections() {
        return this.indexColumnSort;
    }

    public String getName() {
        return this.indexName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getTableCatalog() {
        return tableCatalog;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public boolean isUnique() {
        return this.unique;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public void setTableFullName(String tableFullName) {
        this.tableFullName = tableFullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSchema(String indexSchema) {
        this.indexSchema = indexSchema;
    }

    public void setTableCatalog(String tableCatalog) {
        this.tableCatalog = tableCatalog;
    }

    public void setName(String indexName) {
        this.indexName = indexName;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * 设置索引中字段的位置信息（从1开始）
     *
     * @param list
     */
    public void setPositions(List<Integer> list) {
        this.indexPosition.clear();
        this.indexPosition.addAll(list);
    }

    /**
     * 设置索引中字段名集合
     *
     * @param list
     */
    public void setColumnNames(List<String> list) {
        this.indexColumnName.clear();
        this.indexColumnName.addAll(list);
    }

    /**
     * 设置索引中字段排序方式 <br>
     *
     * @param list
     */
    public void setSort(List<Integer> list) {
        this.indexColumnSort.clear();
        this.indexColumnSort.addAll(list);
    }

    public StandardDatabaseIndex clone() {
        return new StandardDatabaseIndex(this);
    }

    public int compareTo(DatabaseIndex o) {
        return this.compare(this, o);
    }

    /**
     * 判断2个索引字段是否相同
     *
     * @param index1 索引1
     * @param index2 索引2
     * @return 0-索引字段相同 小于零表示索引1小于索引2 大于表示索引1大于索引2
     */
    public int compare(DatabaseIndex index1, DatabaseIndex index2) {
        if (index1 == null && index2 == null) {
            return 0;
        }
        if (index1 == null || index2 == null) {
            if (DB.out.isDebugEnabled()) {
                DB.out.debug((index1 == null) + " != " + (index2 == null));
            }
            return index1 == null ? -1 : 1;
        }

        List<String> nameList1 = index1.getColumnNames(); // 索引字段
        List<String> nameList2 = index2.getColumnNames();
        List<Integer> icsList1 = index1.getDirections(); // 索引字段排序方式
        List<Integer> icsList2 = index2.getDirections();

        if (nameList1.size() != nameList2.size()) { // 判断字段数
            if (DB.out.isDebugEnabled()) {
                DB.out.debug(ResourcesUtils.getDatabaseMessage(39, index1.getName(), index2.getName(), nameList1.size(), nameList2.size()));
            }
            return nameList1.size() - nameList2.size();
        }
        if (icsList1.size() != icsList2.size()) { // 判断字段数
            if (DB.out.isDebugEnabled()) {
                DB.out.debug(ResourcesUtils.getDatabaseMessage(39, index1.getName(), index2.getName(), icsList1.size(), icsList2.size()));
            }
            return icsList1.size() - icsList2.size();
        }

        for (int i = 0; i < nameList1.size(); i++) { // 判断字段名
            int c = nameList1.get(i).compareTo(nameList2.get(i));
            if (c != 0) {
                if (DB.out.isDebugEnabled()) {
                    DB.out.debug(ResourcesUtils.getDatabaseMessage(40, index1.getName(), index2.getName(), (i + 1), nameList1.get(i), nameList2.get(i)));
                }
                return c;
            }
        }

        for (int i = 0; i < icsList1.size(); i++) { // 判断字段排序方式
            int c = icsList1.get(i).compareTo(icsList2.get(i));
            if (c != 0) {
                if (DB.out.isDebugEnabled()) {
                    DB.out.debug(ResourcesUtils.getDatabaseMessage(41, index1.getName(), index2.getName(), (i + 1), icsList1.get(i), icsList2.get(i)));
                }
                return c;
            }
        }

        return 0;
    }

    /**
     * 判断索引索引内容是否相等
     *
     * @param index           索引
     * @param ignoreIndexName true表示忽略字段名大小写不同
     * @param ignoreIndexSort true表示忽略字段排序方式不同
     * @return
     */
    public boolean equals(DatabaseIndex index, boolean ignoreIndexName, boolean ignoreIndexSort) {
        if (index == null) {
            throw new NullPointerException("equals(" + index + ", " + ignoreIndexName + ", " + ignoreIndexSort + ")");
        }

        List<String> names1 = this.getColumnNames();
        List<String> names2 = index.getColumnNames();
        List<Integer> sort1 = this.getDirections();
        List<Integer> sort2 = index.getDirections();

        if (names1.size() == names2.size() && sort1.size() == sort2.size()) {
            for (int i = 0; i < names1.size(); i++) {
                String name1 = names1.get(i);
                String name2 = names2.get(i);

                if (ignoreIndexName) {
                    if (!name1.equalsIgnoreCase(name2)) {
                        return false;
                    }
                } else {
                    if (!name1.equals(name2)) {
                        return false;
                    }
                }

                if (!ignoreIndexSort) {
                    Integer s1 = sort1.get(i);
                    Integer s2 = sort2.get(i);
                    if (s1.intValue() != s2.intValue()) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    public String toString() {
        return this.fullName;
    }

}
