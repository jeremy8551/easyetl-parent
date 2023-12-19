package icu.etl.database.load.serial;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.DatabaseTableDDL;
import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadTable;
import icu.etl.io.TextTableFile;

public class RebuildTableExceptionProcessor {

    /**
     * 扫描数据文件并与目标表中字段类型进行比较，并自动扩容数据库表中字段长度
     *
     * @param dao    数据库接口
     * @param file   数据文件
     * @param target 目标表
     * @throws IOException
     * @throws SQLException
     */
    public void execute(JdbcDao dao, TextTableFile file, LoadTable target) throws Exception {
        DatabaseTableDDL tableDDL = target.getTableDDL();
        dao.dropTable(target.getTable());
        dao.createTable(tableDDL);
        dao.commit();
    }

}
