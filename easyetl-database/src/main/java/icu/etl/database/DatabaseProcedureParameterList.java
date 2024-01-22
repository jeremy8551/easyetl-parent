package icu.etl.database;

import java.util.List;

public interface DatabaseProcedureParameterList extends Cloneable, List<DatabaseProcedureParameter> {

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    DatabaseProcedureParameterList clone();

}
