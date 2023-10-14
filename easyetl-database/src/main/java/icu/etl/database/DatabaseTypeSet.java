package icu.etl.database;

import java.sql.Types;

public interface DatabaseTypeSet {

    /**
     * 返回数据库类型信息
     *
     * @param name 类型名，如：char decimal
     * @return
     */
    DatabaseType get(String name);

    /**
     * 返回数据库类型信息
     *
     * @param sqltype 详见 {@linkplain Types}
     * @return
     */
    DatabaseType get(int sqltype);

}
