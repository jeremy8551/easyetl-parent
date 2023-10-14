package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;

import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;

public class StandardDatabaseColumnList extends ArrayList<DatabaseTableColumn> implements DatabaseTableColumnList {

    private final static long serialVersionUID = 1L;

    public StandardDatabaseColumnList() {
        super();
    }

    public StandardDatabaseColumnList(Collection<? extends DatabaseTableColumn> c) {
        super(c);
    }

    public StandardDatabaseColumnList(int initialCapacity) {
        super(initialCapacity);
    }

    public DatabaseTableColumn getColumn(int position) {
        for (int i = 0; i < this.size(); i++) {
            DatabaseTableColumn column = this.get(i);
            if (column != null && column.getPosition() == position) {
                return column;
            }
        }
        return null;
    }

    public DatabaseTableColumn getColumn(String name) {
        for (int i = 0; i < this.size(); i++) {
            DatabaseTableColumn column = this.get(i);
            if (column != null && column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    public DatabaseTableColumnList clone() {
        StandardDatabaseColumnList list = new StandardDatabaseColumnList(this.size());
        for (int i = 0; i < this.size(); i++) {
            DatabaseTableColumn obj = this.get(i);
            list.add(obj == null ? null : obj.clone());
        }
        return list;
    }

    public String[] getColumnNames() {
        int size = this.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            DatabaseTableColumn column = this.get(i);
            array[i] = column == null ? null : column.getName();
        }
        return array;
    }

    public int[] getColumnPositions() {
        int size = this.size();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            DatabaseTableColumn column = this.get(i);
            array[i] = column == null ? -1 : column.getPosition();
        }
        return array;
    }

    public DatabaseTableColumn[] toArray() {
        DatabaseTableColumn[] array = new DatabaseTableColumn[this.size()];
        for (int i = 0; i < array.length; i++) {
            DatabaseTableColumn obj = this.get(i);
            array[i] = (obj == null) ? null : obj.clone();
        }
        return array;
    }

    public int compareTo(DatabaseTableColumnList columns) {
        if (columns == null) {
            throw new NullPointerException();
        }
        if (this.size() != columns.size()) {
            return this.size() - columns.size();
        }

        for (int i = 0; i < this.size(); i++) {
            int c = this.get(i).compareTo(columns.get(i));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    public DatabaseTableColumn indexOfColumn(String name) {
        for (DatabaseTableColumn column : this) {
            if (column != null && column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    public DatabaseTableColumnList indexOfColumns(String... names) {
        StandardDatabaseColumnList list = new StandardDatabaseColumnList(names.length);
        for (String name : names) {
            DatabaseTableColumn column = this.indexOfColumn(name);
            if (column != null) {
                list.add(column);
            }
        }
        return list;
    }

}
