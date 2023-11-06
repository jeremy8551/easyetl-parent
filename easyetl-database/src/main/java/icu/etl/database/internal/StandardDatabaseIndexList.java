package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;

import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;

public class StandardDatabaseIndexList extends ArrayList<DatabaseIndex> implements DatabaseIndexList {
    private final static long serialVersionUID = 1L;

    public StandardDatabaseIndexList() {
        super();
    }

    public StandardDatabaseIndexList(Collection<? extends DatabaseIndex> c) {
        super(c);
    }

    public StandardDatabaseIndexList(int initialCapacity) {
        super(initialCapacity);
    }

    public DatabaseIndexList clone() {
        StandardDatabaseIndexList list = new StandardDatabaseIndexList(this.size());
        for (int i = 0; i < this.size(); i++) {
            DatabaseIndex obj = this.get(i);
            list.add(obj == null ? null : obj.clone());
        }
        return list;
    }

    public boolean contains(DatabaseIndex index, boolean ignoreIndexName, boolean ignoreIndexSort) {
        if (index == null) {
            throw new NullPointerException();
        }

        for (DatabaseIndex idx : this) {
            if (idx.equals(index, ignoreIndexName, ignoreIndexSort)) {
                return true;
            }
        }
        return false;
    }

}
