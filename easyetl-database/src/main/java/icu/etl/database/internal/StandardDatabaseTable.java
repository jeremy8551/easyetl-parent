package icu.etl.database.internal;

import java.util.List;

import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;
import icu.etl.database.DatabaseSpace;
import icu.etl.database.DatabaseSpaceList;
import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;

/**
 * 数据库表信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-06
 */
public class StandardDatabaseTable implements DatabaseTable {

    /** 表全名 */
    private String fullName;

    /** 数据库表名 */
    private String name;

    /** 类别信息 */
    private String catalog;

    /** 表类型 */
    private String type;

    /** 说明 */
    private String remark;

    /** 数据库表 schema */
    private String schema;

    /** 数据库表索引 */
    private StandardDatabaseIndexList indexs;

    /** 表主键 */
    private StandardDatabaseIndexList primarys;

    /** 表中字段信息 */
    private StandardDatabaseColumnList columns;

    /** 数据库表归属表空间信息 */
    private StandardDatabaseSpaceList tableSpace;

    /** 数据库表索引空间信息 */
    private StandardDatabaseSpaceList indexSpace;

    /**
     * 初始化
     */
    public StandardDatabaseTable() {
        this.indexs = new StandardDatabaseIndexList();
        this.primarys = new StandardDatabaseIndexList();
        this.columns = new StandardDatabaseColumnList();
        this.tableSpace = new StandardDatabaseSpaceList();
        this.indexSpace = new StandardDatabaseSpaceList();
    }

    /**
     * 初始化
     *
     * @param table 数据库表信息
     */
    public StandardDatabaseTable(DatabaseTable table) {
        this();
        this.setName(table.getName());
        this.setSchema(table.getSchema());
        this.setCatalog(table.getCatalog());
        this.setFullName(table.getFullName());
        this.setRemark(table.getRemark());
        this.setType(table.getType());
        this.setColumns(table.getColumns().clone());
        this.setIndexs(table.getIndexs().clone());
        this.setPrimaryIndexs(table.getPrimaryIndexs().clone());
        this.setTableSpace(table.getTableSpaces().clone());
        this.setIndexSpace(table.getIndexSpaces().clone());
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public DatabaseSpaceList getTableSpaces() {
        return this.tableSpace;
    }

    /**
     * 表空间
     *
     * @param tableSpace
     */
    public void setTableSpace(List<DatabaseSpace> tableSpace) {
        this.tableSpace.clear();
        if (tableSpace != null) {
            this.tableSpace.addAll(tableSpace);
        }
    }

    /**
     * 增加表空间
     *
     * @param spaces
     */
    public void addTableSpace(DatabaseSpace... spaces) {
        for (DatabaseSpace space : spaces) {
            if (space != null) {
                this.tableSpace.add(space);
            }
        }
    }

    public DatabaseSpaceList getIndexSpaces() {
        return this.indexSpace;
    }

    /**
     * 删除所有索引空间
     */
    public void clearIndexSpace() {
        this.indexSpace.clear();
    }

    /**
     * 索引空间
     *
     * @param indexSpace
     */
    public void setIndexSpace(List<DatabaseSpace> indexSpace) {
        this.indexSpace.clear();
        if (indexSpace != null) {
            this.indexSpace.addAll(indexSpace);
        }
    }

    /**
     * 增加索引空间
     *
     * @param spaces
     */
    public void addIndexSpace(DatabaseSpace... spaces) {
        for (DatabaseSpace space : spaces) {
            if (space != null) {
                this.indexSpace.add(space);
            }
        }
    }

    /**
     * 数据库表名
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 数据库名归属的SCHEMA
     *
     * @param schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCatalog() {
        return this.catalog;
    }

    /**
     * 数据库表索引信息
     *
     * @param list 索引信息
     */
    public void setIndexs(List<DatabaseIndex> list) {
        this.indexs.clear();
        if (list != null) {
            this.indexs.addAll(list);
        }
    }

    public DatabaseIndexList getIndexs() {
        return this.indexs;
    }

    /**
     * 表的列信息
     *
     * @param list
     */
    public void setColumns(List<DatabaseTableColumn> list) {
        this.columns.clear();
        if (list != null) {
            this.columns.addAll(list);
        }
    }

    public DatabaseTableColumnList getColumns() {
        return this.columns;
    }

    public int columns() {
        return columns.size();
    }

    public DatabaseIndexList getPrimaryIndexs() {
        return this.primarys;
    }

    /**
     * 数据库表的主键信息
     *
     * @param list
     */
    public void setPrimaryIndexs(List<DatabaseIndex> list) {
        this.primarys.clear();
        if (list != null) {
            this.primarys.addAll(list);
        }
    }

    public DatabaseTable clone() {
        return new StandardDatabaseTable(this);
    }

    public String toString() {
        return this.fullName;
    }

}
