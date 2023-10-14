package icu.etl.database.load;

import java.sql.SQLException;

import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;
import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.util.ResourcesUtils;

/**
 * 数据库表索引的处理逻辑
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-16
 */
public class LoadIndex {

    /** 数据库表信息 */
    private DatabaseTable table;

    /** true 表示需要重建索引 */
    private boolean rebuild;

    /**
     * 初始化
     */
    public LoadIndex(DatabaseTable table) {
        if (table == null) {
            throw new NullPointerException();
        } else {
            this.table = table;
        }
    }

    /**
     * 删除索引与主键
     *
     * @param context
     * @param dao
     * @throws SQLException
     */
    public void before(LoadEngineContext context, JdbcDao dao) throws SQLException {
        this.rebuild = false;
        IndexMode mode = context.getIndexMode();
        if (mode == IndexMode.REBUILD || mode == IndexMode.AUTOSELECT) {
            this.rebuild = true;

            // 删除表的主键
            DatabaseIndexList list = this.table.getPrimaryIndexs();
            for (DatabaseIndex index : list) {
                String sql = dao.dropPrimaryKey(index);
                if (LoadEngine.out.isDebugEnabled()) {
                    LoadEngine.out.debug(ResourcesUtils.getLoadMessage(13, context.getId(), sql));
                }
            }

            // 删除表的索引
            DatabaseIndexList indexs = this.table.getIndexs();
            for (DatabaseIndex index : indexs) {
                String sql = dao.dropIndex(index);
                if (LoadEngine.out.isDebugEnabled()) {
                    LoadEngine.out.debug(ResourcesUtils.getLoadMessage(13, context.getId(), sql));
                }
            }
        } else {
//			System.out.println("不需要建索引 ");
        }
    }

    /**
     * 重建索引与主键，生成统计信息
     *
     * @param context
     * @param dao
     * @throws SQLException
     */
    public void after(LoadEngineContext context, JdbcDao dao) throws SQLException {
        if (this.rebuild) {
            // 创建主键
            DatabaseIndexList list = this.table.getPrimaryIndexs();
            for (DatabaseIndex index : list) {
                DatabaseDDL ddl = dao.toDDL(index, true);
                for (String sql : ddl) {
                    if (LoadEngine.out.isDebugEnabled()) {
                        LoadEngine.out.debug(ResourcesUtils.getLoadMessage(14, context.getId(), sql));
                    }
                    dao.executeByJdbc(sql);
                }
            }

            // 创建索引
            DatabaseIndexList indexs = this.table.getIndexs();
            for (DatabaseIndex index : indexs) {
                DatabaseDDL ddl = dao.toDDL(index, false);
                for (String sql : ddl) {
                    if (LoadEngine.out.isDebugEnabled()) {
                        LoadEngine.out.debug(ResourcesUtils.getLoadMessage(14, context.getId(), sql));
                    }
                    dao.executeByJdbc(sql);
                }
            }

            dao.commit();
        }

        // 重组索引并生成索引统计信息
        if (context.isStatistics() || this.rebuild) { // 强制生成统计信息或重建索引时执行
            dao.getDialect().reorgRunstatsIndexs(dao.getConnection(), this.table.getIndexs());
            dao.commit();
        }
    }

}