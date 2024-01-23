package icu.etl.database.load.serial;

import icu.etl.database.DatabaseTableDDL;
import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadTable;

public class RebuildTableExceptionProcessor {

    /**
     * 扫描数据文件并与目标表中字段类型进行比较，并自动扩容数据库表中字段长度
     *
     * @param dao    数据库接口
     * @param target 目标表
     * @throws Exception 重建数据库表发生错误
     */
    public void execute(JdbcDao dao, LoadTable target) throws Exception {
        DatabaseTableDDL tableDDL = target.getTableDDL();
        dao.dropTable(target.getTable());
        dao.createTable(tableDDL);
        dao.commit();
    }

}
