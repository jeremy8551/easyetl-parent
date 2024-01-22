package icu.etl.database.db2;

import java.util.Iterator;
import java.util.Properties;

import icu.etl.collection.UnmodifiableProperties;

/**
 * DB2数据库
 */
public class DB2Database {

    private String name;
    private String aliasName;
    private String home;
    private String memo;
    private int port;
    private boolean isRemote;
    private boolean isIndirect;
    private String remoteHost;
    private String nodeName;
    private String remotePort;
    private Properties properties;

    public DB2Database() {
        super();
        this.properties = new Properties();
    }

    /**
     * 返回数据库名
     *
     * @return 数据库名
     */
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * 返回数据库别名
     *
     * @return 数据库别名
     */
    public String getAliasName() {
        return aliasName;
    }

    protected void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    /**
     * 返回数据库的根目录
     *
     * @return 数据库的根目录
     */
    public String getHome() {
        return home;
    }

    protected void setHome(String databaseHome) {
        this.home = databaseHome;
    }

    /**
     * 返回数据库说明信息
     *
     * @return 数据库说明信息
     */
    public String getMemo() {
        return memo;
    }

    protected void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 返回数据库服务的端口号
     *
     * @return 数据库服务的端口号
     */
    public int getPort() {
        return port;
    }

    protected void setPort(int jdbcPort) {
        this.port = jdbcPort;
    }

    protected void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    /**
     * 返回数据库参数集合，但是参数不可修改
     *
     * @return 数据库参数集合，但是参数不可修改
     */
    public Properties getProperty() {
        return new UnmodifiableProperties(this.properties);
    }

    /**
     * 判断数据库是否是远程服务器
     *
     * @return 返回 true 表示数据库是远程服务器上安装的DB2数据库
     */
    public boolean isRemote() {
        return isRemote;
    }

    protected void setRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }

    /**
     * 如果数据库是远程服务器上的数据库，则返回远端服务器的 HOST 或 IP地址
     *
     * @return 远端服务器的 HOST 或 IP地址
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    protected void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * 返回数据库编目节点名
     *
     * @return 数据库编目节点名
     */
    public String getNodeName() {
        return nodeName;
    }

    protected void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * 如果数据库是远程服务器上的数据库，则返回远端服务器上DB2数据库的服务端口号
     *
     * @return 远端服务器上DB2数据库的服务端口号
     */
    public String getRemotePort() {
        return this.remotePort;
    }

    protected void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * 判断数据库是否是本地的数据库
     *
     * @return 返回true表示数据库是本地创建的数据库
     */
    public boolean isLocal() {
        return isIndirect;
    }

    protected void setLocal(boolean isIndirect) {
        this.isIndirect = isIndirect;
    }

    public String toString() {
        String str = "DB2Database [name=" + name + ", aliasName=" + aliasName + ", databaseHome=" + home + ", memo=" + memo + ", port=" + port + ", isRemote=" + isRemote + ", isIndirect=" + isIndirect + ", remoteHost=" + remoteHost + ", nodeName=" + nodeName + ", remotePort=" + remotePort + "\n";
        Iterator<Object> it = this.properties.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = this.properties.get(key);
            str += "db cfg " + key + " = " + value + "\n";
        }
        str += "]";
        return str;
    }

}
