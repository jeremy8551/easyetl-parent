package icu.etl.database.internal;

import java.util.Map;
import java.util.Properties;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseURL;
import icu.etl.database.Jdbc;
import icu.etl.os.OSConnectCommand;
import icu.etl.util.StringUtils;

public class StandardDatabaseURL implements DatabaseURL {

    private String url;
    private String databaseType;
    private String databaseName;
    private String username;
    private String password;
    private String schema;
    private String hostname;
    private String port;
    private String serverName;
    private String sid;
    private String driverType;
    private Map<String, String> attributes;

    /**
     * 初始化
     *
     * @param url
     */
    public StandardDatabaseURL(String url) {
        super();

        if (url == null) {
            throw new NullPointerException();
        }

        this.attributes = new CaseSensitivMap<String>();
        this.url = url;
    }

    public String getType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getSID() {
        return sid;
    }

    public void setSID(String sID) {
        sid = sID;
    }

    public String getDriverType() {
        return driverType;
    }

    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String toString() {
        return this.url;
    }

    public Properties toProperties() {
        Properties p = new Properties();
        p.putAll(this.attributes);
        if (StringUtils.isNotBlank(this.schema)) {
            p.put(Jdbc.schema, this.schema);
        }
        if (StringUtils.isNotBlank(this.hostname)) {
            p.put(OSConnectCommand.host, this.hostname);
        }
        if (StringUtils.isNotBlank(this.port)) {
            p.put(OSConnectCommand.port, this.port);
        }
        if (StringUtils.isNotBlank(this.sid)) {
            p.put("sid", this.sid);
        }
        return p;
    }

    public StandardDatabaseURL clone() {
        StandardDatabaseURL obj = new StandardDatabaseURL(this.url);
        obj.hostname = this.hostname;
        obj.databaseType = this.databaseType;
        obj.databaseName = this.databaseName;
        obj.username = this.username;
        obj.password = this.password;
        obj.schema = this.schema;
        obj.port = this.port;
        obj.serverName = this.serverName;
        obj.sid = this.sid;
        obj.driverType = this.driverType;
        obj.attributes.putAll(this.attributes);
        return obj;
    }

}
