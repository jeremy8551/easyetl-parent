package icu.etl.database.db2;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.database.DatabaseDialect;

/**
 * 关于DB2数据库的 icu.etl.db.dialect.DatabaseDialect 数据库方言接口实现类
 *
 * @author jeremy8551@qq.com
 */
@EasyBeanClass(kind = "db2", mode = "", major = "11", minor = "5", description = "", type = DatabaseDialect.class)
public class DB2Dialect115 extends DB2Dialect implements DatabaseDialect {
}