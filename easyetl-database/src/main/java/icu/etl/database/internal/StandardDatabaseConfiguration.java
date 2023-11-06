package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseConfiguration;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseURL;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.os.OSAccount;
import icu.etl.util.StringUtils;

public class StandardDatabaseConfiguration implements DatabaseConfiguration, EasyContextAware {

    private String host;
    private String driverClassName;
    private String url;

    private CaseSensitivMap<OSAccount> users;
    private OSAccount user;

    private CaseSensitivMap<StandardOSAccount> sshUsers;
    private OSAccount sshUser;
    private int sshPort;

    protected EasyContext context;

    /**
     * 初始化
     */
    private StandardDatabaseConfiguration() {
        this.users = new CaseSensitivMap<OSAccount>();
        this.sshUsers = new CaseSensitivMap<StandardOSAccount>();
    }

    /**
     * 初始化
     *
     * @param context
     * @param ipAddress       数据库服务器IP地址
     * @param driverClassName JDBC驱动类名（不能是空白或null）
     * @param url             JDBC驱动路径（不能是空白或null）
     * @param username        用户名（如果不存在设置为null）
     * @param password        密码
     * @param adminUsername   管理员账号（如果不存在设置为null）
     * @param adminPassword   密码
     * @param sshUser         SSH远程登录用户名
     * @param sshUserPw       SSH远程登录用户密码
     * @param sshPort         SSH协议端口（设置为 null 时表示使用默认 22 端口）
     */
    public StandardDatabaseConfiguration(EasyContext context, String ipAddress, String driverClassName, String url, String username, String password, String adminUsername, String adminPassword, String sshUser, String sshUserPw, String sshPort) {
        this();
        this.setContext(context);
        this.add(ipAddress, driverClassName, url, username, password, adminUsername, adminPassword, sshUser, sshUserPw, sshPort);
    }

    public void setContext(EasyContext context) {
        this.context = context;
    }

    /**
     * 设置 JDBC 数据库相关配置信息
     *
     * @param ipAddress       数据库服务器ip地址
     * @param driverClassName JDBC驱动类名（不能是空白或null）
     * @param url             JDBC驱动路径（不能是空白或null）
     * @param username        用户名（如果不存在设置为null）
     * @param password        密码
     * @param adminUsername   管理员账号（如果不存在设置为null）
     * @param adminPassword   密码
     * @param sshUser         SSH用户名
     * @param sshUserPw       SSH用户密码
     * @param sshPort         SSH协议端口（设置为 null 时表示使用默认 22 端口）
     */
    private void add(String ipAddress, String driverClassName, String url, String username, String password, String adminUsername, String adminPassword, String sshUser, String sshUserPw, String sshPort) {
        if (StringUtils.isNotBlank(sshPort) && !StringUtils.isNumber(sshPort)) {
            throw new IllegalArgumentException(sshPort);
        }
//		if (StringUtils.isBlank(driverClassName)) {
//			throw new IllegalArgumentException(driverClassName);
//		}
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(url);
        }

        this.url = url;
        this.driverClassName = driverClassName;

        // 设置数据库服务器的IP地址或主机名
        if (StringUtils.isBlank(ipAddress)) { // 截取 URL 中的主机ip或主机名作为默认值
            DatabaseDialect dialect = this.context.getBean(DatabaseDialect.class, url);
            List<DatabaseURL> urls = dialect.parseJdbcUrl(url);
            if (urls.size() > 0) {
                this.host = urls.get(0).getHostname();
            }
        } else {
            this.host = ipAddress;
        }

        // 添加数据库用户
        if (username != null) {
            this.addAccount(username, new StandardOSAccount(username, password, false));
        }

        // 添加数据库管理员账号
        if (adminUsername != null) {
            this.addAccount(adminUsername, new StandardOSAccount(adminUsername, adminPassword, true));
        }

        // 添加数据库SSH协议端口号
        if (sshUser != null) {
            this.addSSHAccount(sshUser, sshUserPw);
        }
        this.sshPort = StringUtils.parseInt(sshPort, 22);
    }

    /**
     * 添加账号
     *
     * @param key
     * @param account
     * @return
     */
    private OSAccount addAccount(String key, OSAccount account) {
        if (account == null) {
            return null;
        }
        if (this.user == null) {
            this.user = account;
        }
        return this.users.put(key, account);
    }

    public String getHostname() {
        return this.host;
    }

    public void setHostname(String str) {
        this.host = str;
    }

    public String getDriverClass() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Collection<String> getAccountNames() {
        return this.users.keySet();
    }

    public OSAccount getAccount(String name) {
        return this.users.get(name);
    }

    public int getSSHPort() {
        return this.sshPort;
    }

    public void setSSHPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public OSAccount getSSHAccount() {
        return this.sshUser;
    }

    public synchronized boolean addSSHAccount(String username, String password) {
        StandardOSAccount user = new StandardOSAccount(username, password, false);
        if (this.sshUser == null) {
            this.sshUser = user; // 第一个账户作为默认账户
        }
        return this.sshUsers.put(username, user) == null;
    }

    public synchronized boolean addAccount(String username, String password, boolean admin) {
        StandardOSAccount account = new StandardOSAccount(username, password, admin);
        return this.addAccount(username, account) == null;
    }

    public List<OSAccount> getAccounts() {
        ArrayList<OSAccount> list = new ArrayList<OSAccount>(this.users.values());
        Collections.sort(list, new ComparatorImpl(false));
        return list;
    }

    private class ComparatorImpl implements Comparator<OSAccount> {
        private boolean ascOrdesc;

        public ComparatorImpl(boolean ascOrdesc) {
            super();
            this.ascOrdesc = ascOrdesc;
        }

        public int compare(OSAccount o1, OSAccount o2) {
            return this.ascOrdesc ? (o1.isAdmin() ? 1 : 0) : (o2.isAdmin() ? 0 : 1);
        }
    }

    public OSAccount getAccount() {
        return this.user;
    }

    /**
     * 返回一个 JDBC 配置信息副本
     *
     * @return
     */
    public synchronized DatabaseConfiguration clone() {
        StandardDatabaseConfiguration config = new StandardDatabaseConfiguration();
        config.host = this.host;
        config.driverClassName = this.driverClassName;
        config.url = this.url;

        // 复制数据库账户
        for (Iterator<String> it = this.users.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            OSAccount account = this.users.get(key).clone();
            config.addAccount(key, account);
        }

        if (this.user != null) {
            config.user = this.user.clone();
        }

        // 复制 SSH 用户
        for (Iterator<String> it = this.sshUsers.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            OSAccount account = this.sshUsers.get(key).clone();
            config.addAccount(key, account);
        }

        if (this.sshUser != null) {
            config.sshUser = this.sshUser.clone();
        }

        config.sshPort = this.sshPort;
        return config;
    }

    public String toString() {
        return "[driverClassName=" + driverClassName + ", url=" + url + ", sshPort=" + sshPort + ", map=" + StringUtils.toString(this.users) + "]";
    }

}
