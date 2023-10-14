package icu.etl.database.pool;

import java.sql.Connection;
import java.util.Stack;

import icu.etl.database.Jdbc;
import icu.etl.log.STD;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;

public class PoolConnectionList extends Stack<PoolConnection> {
    private final static long serialVersionUID = 1L;

    public PoolConnectionList() {
        super();
    }

    /**
     * 清空所有连接并关闭所有连接
     */
    public synchronized void close() {
        Stack<PoolConnection> list = this;
        for (int i = 0; i < list.size(); i++) {
            PoolConnection proxy = list.get(i);
            if (proxy != null) {
                Connection conn = proxy.getConnection();
                if (Jdbc.canUseQuietly(conn)) {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug(ResourcesUtils.getDataSourceMessage(9, proxy));
                    }

                    try {
                        Jdbc.commit(conn);
                    } catch (Throwable e) {
                        Jdbc.rollbackQuietly(conn);
                    } finally {
                        IO.closeQuietly(conn, conn);
                    }
                }
            }
        }
        list.clear();
    }

}
