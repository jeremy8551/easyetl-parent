package icu.etl.database.pool;

import java.sql.Connection;

/**
 * 数据库连接代理接口
 *
 * @author jeremy8551@qq.com
 */
public interface ConnectionProxy extends Connection {

    /**
     * 返回被代理的数据库连接对象
     *
     * @return 数据库连接
     */
    Connection getOrignalConnection();

}
