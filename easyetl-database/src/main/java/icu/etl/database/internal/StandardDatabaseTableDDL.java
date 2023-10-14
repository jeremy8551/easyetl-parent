package icu.etl.database.internal;

import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseTableDDL;
import icu.etl.util.FileUtils;

public class StandardDatabaseTableDDL implements DatabaseTableDDL {

    private String table;
    private StandardDatabaseDDL comment;
    private StandardDatabaseDDL index;
    private StandardDatabaseDDL primarykey;

    /**
     * 初始化
     */
    public StandardDatabaseTableDDL() {
        this.comment = new StandardDatabaseDDL();
        this.index = new StandardDatabaseDDL();
        this.primarykey = new StandardDatabaseDDL();
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTable() {
        return this.table;
    }

    public DatabaseDDL getComment() {
        return this.comment;
    }

    public DatabaseDDL getIndex() {
        return this.index;
    }

    public DatabaseDDL getPrimaryKey() {
        return this.primarykey;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.table).append(';').append(FileUtils.lineSeparator);

        for (String ddl : this.primarykey) {
            buf.append(ddl).append(';').append(FileUtils.lineSeparator);
        }

        for (String ddl : this.index) {
            buf.append(ddl).append(';').append(FileUtils.lineSeparator);
        }

        for (String ddl : this.comment) {
            buf.append(ddl).append(';').append(FileUtils.lineSeparator);
        }

        return buf.toString();
    }

}
