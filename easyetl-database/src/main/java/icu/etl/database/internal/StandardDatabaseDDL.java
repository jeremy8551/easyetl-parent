package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;

import icu.etl.database.DatabaseDDL;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

public class StandardDatabaseDDL extends ArrayList<String> implements DatabaseDDL {
    private final static long serialVersionUID = 1L;

    public StandardDatabaseDDL() {
        super();
    }

    public StandardDatabaseDDL(Collection<? extends String> c) {
        super(c);
    }

    public StandardDatabaseDDL(int initialCapacity) {
        super(initialCapacity);
    }

    public DatabaseDDL clone() {
        return new StandardDatabaseDDL(this);
    }

    public String toString() {
        return StringUtils.join(this, String.valueOf(FileUtils.lineSeparator));
    }

}
