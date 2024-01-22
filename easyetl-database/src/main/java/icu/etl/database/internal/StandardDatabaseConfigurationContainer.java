package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import icu.etl.annotation.EasyBean;
import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseConfiguration;
import icu.etl.database.DatabaseConfigurationContainer;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseURL;
import icu.etl.database.Jdbc;
import icu.etl.ioc.EasyContext;
import icu.etl.os.OSAccount;
import icu.etl.os.OSConnectCommand;
import icu.etl.os.OSShellCommand;
import icu.etl.util.Ensure;

/**
 * 数据库连接信息集合
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(singleton = true)
public class StandardDatabaseConfigurationContainer implements DatabaseConfigurationContainer {

    private CaseSensitivMap<DatabaseConfiguration> map;

    private EasyContext context;

    public StandardDatabaseConfigurationContainer(EasyContext context) {
        this.map = new CaseSensitivMap<DatabaseConfiguration>();
        this.context = Ensure.notNull(context);
    }

    public DatabaseConfiguration add(Properties p) {
        String host = p.getProperty(OSConnectCommand.host);
        String driverClassName = p.getProperty(Jdbc.driverClassName);
        String url = p.getProperty(Jdbc.url);
        String username = p.getProperty(OSConnectCommand.username);
        String password = p.getProperty(OSConnectCommand.password);
        String adminUsername = p.getProperty(Jdbc.admin);
        String adminPassword = p.getProperty(Jdbc.adminPw);
        String sshUser = p.getProperty(OSShellCommand.sshUser);
        String sshUserPw = p.getProperty(OSShellCommand.sshUserPw);
        String sshPort = p.getProperty(OSShellCommand.sshPort);

        StandardDatabaseConfiguration config = new StandardDatabaseConfiguration(this.context, host, driverClassName, url, username, password, adminUsername, adminPassword, sshUser, sshUserPw, sshPort);
        this.add(config);
        return config;
    }

    public void add(DatabaseConfiguration config) {
        DatabaseDialect dialect = this.context.getBean(DatabaseDialect.class, config.getUrl());
        List<DatabaseURL> list = dialect.parseJdbcUrl(config.getUrl());
        for (DatabaseURL url : list) {
            String key = this.toKey(url.getHostname(), url.getPort(), url.getDatabaseName());
            if (this.map.containsKey(key)) {
                DatabaseConfiguration obj = this.map.get(key);
                Collection<String> names = config.getAccountNames();
                for (String name : names) {
                    OSAccount account = config.getAccount(name);
                    obj.addAccount(account.getUsername(), account.getPassword(), account.isAdmin());
                }
            } else {
                this.map.put(key, config);
            }
        }
    }

    public DatabaseConfiguration get(Connection conn) throws SQLException {
        String url = conn.getMetaData().getURL();
        DatabaseDialect dialect = this.context.getBean(DatabaseDialect.class, url);
        List<DatabaseURL> list = dialect.parseJdbcUrl(url);
        for (DatabaseURL obj : list) {
            DatabaseConfiguration config = this.get(obj.getHostname(), obj.getPort(), obj.getDatabaseName());
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    public DatabaseConfiguration get(String hostname, String port, String database) {
        String key = this.toKey(hostname, port, database);
        return this.map.get(key);
    }

    /**
     * 转为数据库配置信息编号
     *
     * @param hostname 数据库服务器host
     * @param port     端口
     * @param database 数据库名
     * @return 编号
     */
    protected String toKey(String hostname, String port, String database) {
        StringBuilder buf = new StringBuilder();
        buf.append(hostname);
        buf.append('-');
        buf.append(port);
        buf.append('-');
        buf.append(database);
        return buf.toString();
    }

}
