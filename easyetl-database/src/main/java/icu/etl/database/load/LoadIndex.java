package icu.etl.database.load;

import java.sql.SQLException;

import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;
import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 数据库表索引的处理逻辑
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-16
 */
public class LoadIndex {
    private final static Log log = LogFactory.getLog(LoadIndex.class);

    /** 数据库表信息 */
    private DatabaseTable table;

    /** true 表示需要重建索引 */
    private boolean rebuild;

    /**
     * 初始化
     */
    public LoadIndex(DatabaseTable table) {
        this.table = Ensure.notNull(table);
    }

    /**
     * 删除索引与主键
     *
     * @param context 装数引擎上下文信息
     * @param dao     数据库操作接口
     * @throws SQLException 数据库错误
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
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("load.standard.output.msg013", context.getId(), sql));
                }
            }

            // 删除表的索引
            DatabaseIndexList indexs = this.table.getIndexs();
            for (DatabaseIndex index : indexs) {
                String sql = dao.dropIndex(index);
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("load.standard.output.msg013", context.getId(), sql));
                }
            }
        }
    }

    /**
     * 重建索引与主键，生成统计信息
     *
     * @param context 装数引擎上下文信息
     * @param dao     数据库操作接口
     * @throws SQLException 数据库错误
     */
    public void after(LoadEngineContext context, JdbcDao dao) throws SQLException {
        if (this.rebuild) {
            // 创建主键
            DatabaseIndexList list = this.table.getPrimaryIndexs();
            for (DatabaseIndex index : list) {
                DatabaseDDL ddl = dao.toDDL(index, true);
                for (String sql : ddl) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("load.standard.output.msg014", context.getId(), sql));
                    }
                    dao.execute(sql);
                }
            }

            // 创建索引
            DatabaseIndexList indexs = this.table.getIndexs();
            for (DatabaseIndex index : indexs) {
                DatabaseDDL ddl = dao.toDDL(index, false);
                for (String sql : ddl) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMessage("load.standard.output.msg014", context.getId(), sql));
                    }
                    dao.execute(sql);
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
