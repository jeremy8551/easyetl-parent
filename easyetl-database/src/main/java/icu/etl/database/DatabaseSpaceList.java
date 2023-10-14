package icu.etl.database;

import java.util.List;

public interface DatabaseSpaceList extends Cloneable, List<DatabaseSpace> {

    /**
     * 返回一个副本
     *
     * @return
     */
    DatabaseSpaceList clone();

}
