package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;

import icu.etl.database.DatabaseSpace;
import icu.etl.database.DatabaseSpaceList;

public class StandardDatabaseSpaceList extends ArrayList<DatabaseSpace> implements DatabaseSpaceList {
    private final static long serialVersionUID = 1L;

    public StandardDatabaseSpaceList() {
        super();
    }

    public StandardDatabaseSpaceList(Collection<? extends DatabaseSpace> c) {
        super(c);
    }

    public StandardDatabaseSpaceList(int initialCapacity) {
        super(initialCapacity);
    }

    public DatabaseSpaceList clone() {
        StandardDatabaseSpaceList list = new StandardDatabaseSpaceList(this.size());
        for (int i = 0; i < this.size(); i++) {
            DatabaseSpace obj = this.get(i);
            list.add(obj == null ? null : obj.clone());
        }
        return list;
    }

}
