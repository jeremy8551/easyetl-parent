package icu.etl.database.pool;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.EasyContext;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;

/**
 * 数据库连接设置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-13
 */
public class ConnectionAttributes implements Cloneable {
    private final static Log log = LogFactory.getLog(ConnectionAttributes.class);

    private Map<String, Class<?>> types;
    private Properties clientInfo;
    private boolean autoCommit;
    private String catalog;
    private int holdability;
    private int networkTimeout;
    private boolean readOnly;
    private String schema;
    private int transactionIsolation;

    private boolean hasAutoCommit;
    private boolean hasCatalog;
    private boolean hasHoldability;
    private boolean hasNetworkTimeout;
    private boolean hasReadOnly;
    private boolean hasSchema;
    private boolean hasTransactionIsolation;
    private boolean hasClientInfo;
    private boolean hasTypeMap;

    private EasyContext context;

    private ConnectionAttributes() {
        this.types = new HashMap<String, Class<?>>();
        this.clientInfo = new Properties();
    }

    public ConnectionAttributes(EasyContext context, Connection conn) {
        this();
        this.context = Ensure.notNull(context);

        try {
            this.autoCommit = conn.getAutoCommit();
            this.hasAutoCommit = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.catalog = conn.getCatalog();
            this.hasCatalog = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            DatabaseDialect dialect = this.context.getBean(DatabaseDialect.class, conn);
            this.schema = dialect.getSchema(conn);
            this.hasSchema = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.readOnly = conn.isReadOnly();
            this.hasReadOnly = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.holdability = conn.getHoldability();
            this.hasHoldability = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.networkTimeout = JavaDialectFactory.getDialect().getNetworkTimeout(conn);
            this.hasNetworkTimeout = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.transactionIsolation = conn.getTransactionIsolation();
            this.hasTransactionIsolation = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.clientInfo.putAll(JavaDialectFactory.getDialect().getClientInfo(conn));
            this.hasClientInfo = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            this.types.putAll(conn.getTypeMap());
            this.hasTypeMap = true;
        } catch (Throwable e) {
            if (log.isWarnEnabled()) {
                log.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * 将属性设置到数据库连接参数 conn 中
     *
     * @param conn 数据库连接
     */
    public void reset(Connection conn) {
        try {
            if (this.hasAutoCommit) {
                conn.setAutoCommit(this.autoCommit);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasCatalog) {
                conn.setCatalog(this.catalog);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasSchema) {
                DatabaseDialect dialect = this.context.getBean(DatabaseDialect.class, conn);
                dialect.setSchema(conn, this.schema);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasReadOnly) {
                conn.setReadOnly(this.readOnly);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasTransactionIsolation) {
                conn.setTransactionIsolation(this.transactionIsolation);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasClientInfo) {
                JavaDialectFactory.getDialect().setClientInfo(conn, this.clientInfo);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

        try {
            if (this.hasHoldability) {
                conn.setHoldability(this.holdability);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }

//        try {
//            if (this.hasNetworkTimeout) {
//                conn.setNetworkTimeout(null, this.networkTimeout);
//            }
//        } catch (Throwable e) {
//            if (log.isDebugEnabled()) {
//                log.debug(e.getLocalizedMessage(), e);
//            }
//        }

        try {
            if (this.hasTypeMap) {
                conn.setTypeMap(this.types);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * 返回一个副本
     */
    public ConnectionAttributes clone() {
        ConnectionAttributes newobj = new ConnectionAttributes();
        newobj.autoCommit = this.autoCommit;
        newobj.catalog = this.catalog;
        newobj.schema = this.schema;
        newobj.readOnly = this.readOnly;
        newobj.holdability = this.holdability;
        newobj.networkTimeout = this.networkTimeout;
        newobj.transactionIsolation = this.transactionIsolation;
        newobj.clientInfo.putAll(this.clientInfo);
        newobj.types.putAll(this.types);
        newobj.hasAutoCommit = this.hasAutoCommit;
        newobj.hasCatalog = this.hasCatalog;
        newobj.hasHoldability = this.hasHoldability;
        newobj.hasNetworkTimeout = this.hasNetworkTimeout;
        newobj.hasReadOnly = this.hasReadOnly;
        newobj.hasSchema = this.hasSchema;
        newobj.hasTransactionIsolation = this.hasTransactionIsolation;
        newobj.hasClientInfo = this.hasClientInfo;
        newobj.hasTypeMap = this.hasTypeMap;
        newobj.context = this.context;
        return newobj;
    }

}
