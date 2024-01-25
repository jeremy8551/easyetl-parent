package icu.etl.script.internal;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.pool.SimpleDatasource;
import icu.etl.os.OSConnectCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎数据库操作类
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-04-19
 */
public class ScriptDataSource {

    /**
     * 返回可用的数据库连接
     *
     * @param context 脚本引擎上下文信息
     * @return 脚本引擎数据库操作类
     */
    public static ScriptDataSource get(UniversalScriptContext context) {
        boolean global = false;
        String key = "ScriptDataSource";
        ScriptDataSource dao = context.getProgram(key, global);
        if (dao == null) {
            dao = new ScriptDataSource(context);
            context.addProgram(key, dao, global);
        }
        return dao;
    }

    /** 当前数据库连接的数据库编目名 */
    protected String catalog;

    /** 脚本引擎上下文信息 */
    protected UniversalScriptContext context;

    /** 当前数据库连接的DAO对象 */
    protected JdbcDao dao;

    /** 内部数据库编目名和数据库连接池之间的映射 */
    protected Map<String, DataSource> map;

    /**
     * 初始化
     */
    public ScriptDataSource(UniversalScriptContext context) {
        this.map = new Hashtable<String, DataSource>();
        this.dao = new JdbcDao(context.getContainer());
        this.catalog = null;
        this.context = context;
    }

    /**
     * 返回当前数据库连接的DAO对象
     *
     * @return 数据库操作接口
     */
    public JdbcDao getDao() {
        return this.dao;
    }

    /**
     * 关闭数据库连接，并清空所有数据库编目信息
     */
    public void close() {
        this.release();
        DataSource[] array = new DataSource[this.map.size()];
        Jdbc.closeDataSource(this.map.values().toArray(array)); // 按顺序关闭数据库连接池
        this.map.clear();
    }

    /**
     * 提交当前数据库连接上的事务，并关闭数据库连接
     */
    private void release() {
        if (this.dao.isConnected()) {
            this.dao.commit();
            this.dao.close();
        }

        this.dao.setConnection(null, true);
        this.catalog = null;
    }

    /**
     * 使用指定数据库编目建立数据库连接池 <br>
     * 优先使用数据库编目名从外部设置的数据库连接池中查找 <br>
     * 再次从用户定义的数据库编目信息中查找，如果存在则自动建立一个数据库连接池 <br>
     *
     * @param name 数据库编目名
     * @return 数据库连接池（不可能是 null）
     * @throws UniversalScriptException 如果数据库编目名对应的数据库编目信息不存在!
     */
    public DataSource getPool(String name) {
        Ensure.notBlank(name);
        String key = name.toUpperCase();
        DataSource pool = this.map.get(key); // 优先从外部设置的数据库连接池中查找
        if (pool != null) {
            return pool;
        }

        Properties catalog = this.context.getCatalog(key); // 查找数据库编目信息
        if (catalog == null) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr008", this.map.keySet(), name));
        }

        if (!catalog.containsKey(Jdbc.url)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("database.standard.output.msg025", Jdbc.driverClassName, Jdbc.url, OSConnectCommand.username, OSConnectCommand.password, StringUtils.toString(catalog)));
        }

        SimpleDatasource dataSource = new SimpleDatasource(this.context.getContainer(), catalog); // 创建一个数据库连接池
        DataSource dataSourceProxy = Jdbc.getDataSourceLogger(this.context.getContainer(), dataSource);
        this.map.put(key, dataSourceProxy); // 保存到用户自定义数据库连接池集合中
        return dataSourceProxy;
    }

    /**
     * 返回当前使用的数据库连接所在的数据库连接池
     *
     * @return 数据库连接池
     */
    public DataSource getPool() {
        return this.getPool(this.catalog);
    }

    /**
     * 返回当前数据库连接的数据库编目名
     *
     * @return 数据库编目名
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * 设置数据库编目
     *
     * @param name 数据库编目名
     */
    public void setCatalog(String name) {
        this.catalog = Ensure.notBlank(name).toUpperCase();
    }

}