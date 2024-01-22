package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;

import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.DatabaseProcedureParameterList;

public class StandardDatabaseProcedureParameterList extends ArrayList<DatabaseProcedureParameter> implements DatabaseProcedureParameterList {
    private final static long serialVersionUID = 1L;

    public StandardDatabaseProcedureParameterList() {
        super();
    }

    public StandardDatabaseProcedureParameterList(Collection<? extends DatabaseProcedureParameter> c) {
        super(c);
    }

    public StandardDatabaseProcedureParameterList(int initialCapacity) {
        super(initialCapacity);
    }

    public DatabaseProcedureParameterList clone() {
        int size = this.size();
        StandardDatabaseProcedureParameterList list = new StandardDatabaseProcedureParameterList(size);
        for (int i = 0; i < size; i++) {
            list.add(list.get(i).clone());
        }
        return list;
    }

}
