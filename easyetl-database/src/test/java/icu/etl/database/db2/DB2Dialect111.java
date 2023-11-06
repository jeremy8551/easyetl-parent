package icu.etl.database.db2;

import icu.etl.annotation.EasyBean;
import icu.etl.database.DatabaseDialect;

/**
 * 关于DB2数据库的 icu.etl.db.dialect.DatabaseDialect 数据库方言接口实现类
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "db2")
public class DB2Dialect111 extends DB2Dialect implements DatabaseDialect {

    public String getDatabaseMajorVersion() {
        return "";
    }

    public String getDatabaseMinorVersion() {
        return "1";
    }
}