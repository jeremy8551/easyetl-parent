package icu.etl.database.db2;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import icu.etl.database.Jdbc;
import icu.etl.io.BufferedLineReader;
import icu.etl.log.STD;
import icu.etl.os.OS;
import icu.etl.os.OSCommand;
import icu.etl.os.OSCommandException;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSService;
import icu.etl.os.OSUser;
import icu.etl.os.OSUserGroup;
import icu.etl.util.CollUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.NetUtils;
import icu.etl.util.Property;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * DB2数据库实例
 */
public class DB2Instance {

    /**
     * 返回操作系统中DB2数据库实例信息
     *
     * @param os 操作系统接口
     * @return 如果未安装DB2返回null
     */
    public static List<DB2Instance> get(OS os) {
        boolean need0 = !os.isEnableOSCommand();
        boolean need1 = !os.isEnableOSFileCommand();
        try {
            if (need0) {
                os.enableOSCommand();
            }
            if (need1) {
                os.enableOSFileCommand();
            }

            List<DB2Instance> list = new ArrayList<DB2Instance>();
            list.addAll(DB2Instance.detect(os));
            return Collections.unmodifiableList(list);
        } finally {
            try {
                if (need0) {
                    os.disableOSCommand();
                }
            } finally {
                if (need1) {
                    os.disableOSFileCommand();
                }
            }
        }
    }

    /**
     * 根据访问端口和数据库名查找对应的DB2数据库实例
     *
     * @param os           操作系统接口
     * @param port         DB2数据库服务端口
     * @param databaseName 数据库名
     * @return 返回null表示数据库不存在
     */
    public static DB2Instance get(OS os, int port, String databaseName) {
        List<DB2Instance> list = get(os);
        for (DB2Instance inst : list) {
            if (inst.getPort() == port) {
                return inst;
            }

            String[] databaseNames = inst.getDatabaseNames();
            for (String name : databaseNames) {
                DB2Database database = inst.getDatabase(name);
                if (database.getPort() == port || database.getName().equalsIgnoreCase(databaseName)) {
                    return inst;
                }
            }
        }
        return null;
    }

    private OS os;
    private String name;
    private int port;
    private String db2dir;
    private OSUser user;
    private String db2profile;
    private Properties config;
    private Map<String, DB2Database> databases;

    /**
     * 初始化
     */
    private DB2Instance() {
        this.config = new Properties();
        this.databases = new HashMap<String, DB2Database>();
    }

    /**
     * 在操作系统上搜索 DB2 数据库实例
     *
     * @param os
     * @return
     */
    public static List<DB2Instance> detect(OS os) {
        return new DB2Instance().detectDB2Instance(os);
    }

    /**
     * 在操作系统上搜索 DB2 数据库实例
     *
     * @param os
     * @return
     */
    private List<DB2Instance> detectDB2Instance(OS os) {
        if (os == null) {
            throw new NullPointerException();
        }
        Ensure.isTrue(os.supportOSCommand() && os.enableOSCommand(), os);

        OSUser currentUser = os.getUser();
        this.os = os;
        List<DB2Instance> list = new ArrayList<DB2Instance>();
        // 检查操作系统中已安装的DB2数据库
        List<OSUser> allUsers = os.getUsers();
        OSCommand cmd = os.getOSCommand();

        String template = "find ${HOME} -name db2profile -type f ;";
        HashSet<String> set = new HashSet<String>();
        if (currentUser.isRoot()) {
            set.add(StringUtils.replaceVariable(template, "HOME", "/"));

            List<OSUserGroup> groups = os.getGroups();
            for (OSUserGroup group : groups) {
                String str = group.getName().toLowerCase();
                if (str.startsWith("dasadm")) {
                    List<String> users = group.getUsers();
                    for (String name : users) {
                        OSUser user = os.getUser(name);
                        if (user == null) {
                            throw new NullPointerException();
                        }
                        set.add(StringUtils.replaceVariable(template, "HOME", user.getHome()));
                    }
                }
            }

            // db2iadm group user
            for (OSUserGroup group : groups) {
                String str = group.getName().toLowerCase();
                if (str.startsWith("db2iadm")) {
                    List<String> usernames = group.getUsers();
                    for (String name : usernames) {
                        OSUser user = os.getUser(name);
                        if (user == null) {
                            throw new NullPointerException();
                        }
                        set.add(StringUtils.replaceVariable(template, "HOME", user.getHome()));
                    }
                }
            }

            for (OSUser user : allUsers) {
                if (user.getName().toLowerCase().indexOf("db2") != -1) {
                    set.add(StringUtils.replaceVariable(template, "HOME", user.getHome()));
                }
            }
        } else {
            set.add(StringUtils.replaceVariable(template, "HOME", currentUser.getHome()));
        }

        cmd.execute(StringUtils.join(set, " "));
        this.addDB2Instance(allUsers, list, cmd.getStdout());

        for (DB2Instance inst : list) {
            StringBuilder env = new StringBuilder();
            List<String> profiles = new ArrayList<String>(inst.getUser().getProfiles());
            profiles.add(inst.getDB2profile());
            for (String profile : profiles) {
                env.append(". ").append(profile).append(";");
            }

            OSCommandStdouts map = cmd.execute("env cmds", env.toString(), "dbm cfg", " db2 get dbm cfg ; echo \"(DB2DIR) =  $DB2DIR\"", "db list", "db2 list db directory", "node list", "db2 list node directory");
            List<String> dbmCfgList = map.get("dbm cfg");
            for (String line : dbmCfgList) {
                String[] array = StringUtils.splitProperty(line);
                if (array != null) {
                    String key = array[0];
                    int b, e;
                    if ((b = key.lastIndexOf('(')) != -1 && (e = key.indexOf(')', b)) != -1) {
                        key = key.substring(b + 1, e);
                    }
                    key = StringUtils.trimBlank(key);
                    String value = StringUtils.trimBlank(array[1]);

                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("db2 database meta data: " + key + " = " + value);
                    }
                    inst.addConfig(key, value);
                }
            }

            inst.setDB2Dir(inst.getConfig("DB2DIR"));
            int port = -1;
            String svcename = inst.getConfig("SVCENAME");
            if (StringUtils.isBlank(svcename)) { // db2客户端不存在这个参数
                port = this.getDB2Port(os, svcename);
            } else if (StringUtils.isNumber(svcename)) {
                port = Integer.parseInt(svcename);
            }
            if (port != -1) {
                inst.port = port;
            }

            List<String> profileList = new ArrayList<String>(inst.getUser().getProfiles());
            profileList.add(inst.getDB2profile());
            for (String profile : profileList) {
                env.append(". ").append(profile).append(";");
            }
            List<String> commands = new ArrayList<String>();
            commands.add("env print");
            commands.add(env.toString());

            Map<String, Map<String, String>> map1 = new HashMap<String, Map<String, String>>();
            List<Map<String, Property>> nodeList = this.splitPropertiesCommandStdout(map.get("node list"), 6);
            for (Map<String, Property> nodeMap : nodeList) {
                String nodeName = nodeMap.get("1").getValue();
                String hostName = nodeMap.get("5").getValue();
                String serviceName = nodeMap.get("6").getValue();

                Map<String, String> m0 = new HashMap<String, String>();
                m0.put("nodeName", nodeName);
                m0.put("hostName", hostName);
                m0.put("serviceName", serviceName);
                map1.put(nodeName, m0);
            }

            List<Map<String, Property>> dbCfgs = this.splitPropertiesCommandStdout(map.get("db list"), 9);
            for (Map<String, Property> dbCfg : dbCfgs) {
                String aliasName = dbCfg.get("1").getValue();
                String databaseName = dbCfg.get("2").getValue();
                String home = dbCfg.get("3").getValue();
                String memo = dbCfg.get("5").getValue();
                String entryType = dbCfg.get("6").getValue();
                boolean remote = "remote".equalsIgnoreCase(entryType);
                boolean indirect = "Indirect".equalsIgnoreCase(entryType);

                DB2Database database = new DB2Database();
                database.setName(databaseName);
                database.setAliasName(aliasName);
                database.setMemo(memo);
                if (indirect) {
                    database.setLocal(indirect);
                    database.setPort(port);
                    database.setHome(home);
                } else if (remote) {
                    String nodeName = home;
                    database.setRemote(remote);
                    database.setNodeName(nodeName);

                    Map<String, String> m = map1.get(nodeName);
                    database.setPort(StringUtils.parseInt(m.get("serviceName"), -1));
                    database.setRemoteHost(m.get("hostName"));
                }
                inst.addDatabase(database);

                commands.add(databaseName);
                commands.add("db2 get db cfg for " + databaseName);
            }

            // 查询所有数据库配置信息
            OSCommandStdouts m2 = cmd.execute(commands);
            String[] databaseNames = inst.getDatabaseNames();
            for (String name : databaseNames) {
                DB2Database database = inst.getDatabase(name);
                List<String> dbcfglist = m2.get(database.getName());
                for (String line : dbcfglist) {
                    String[] array = StringUtils.splitProperty(line);
                    if (array != null) {
                        String key = array[0];
                        int begin, end;
                        if ((begin = key.lastIndexOf('(')) != -1 && (end = key.indexOf(')', begin)) != -1) {
                            key = key.substring(begin + 1, end);
                        }
                        key = StringUtils.trimBlank(key);
                        String value = StringUtils.trimBlank(array[1]);

                        if (STD.out.isDebugEnabled()) {
                            STD.out.debug("db2 database meta data: " + key + " = " + value);
                        }

                        if (key.equalsIgnoreCase("SQLSTATE") && value.equalsIgnoreCase("08001")) {
                            continue;
                        } else {
                            database.setProperty(key, value);
                        }
                    }
                }
            }
        }

        return list;
    }

    private List<Map<String, Property>> splitPropertiesCommandStdout(List<String> list, int size) {
        Ensure.isTrue(size >= 1 && list != null, size, list);

        List<Map<String, Property>> propertyList = new ArrayList<Map<String, Property>>();
        if (list == null || list.size() == 0) {
            return propertyList;
        }

        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String line = it.next();
            String[] array = StringUtils.splitProperty(line);
            if (array != null && array.length == 2) {
                StringUtils.trimBlank(array);
                Map<String, Property> ll = new HashMap<String, Property>();

                Property p = new Property();
                p.setKey(array[0]);
                p.setValue(array[1]);
                ll.put(String.valueOf(ll.size() + 1), p);

                while (it.hasNext()) {
                    String next = it.next();
                    String[] a1 = StringUtils.splitProperty(next);
                    if (a1 != null && a1.length == 2) {
                        StringUtils.trimBlank(a1);
                        Property p0 = new Property();
                        p0.setKey(a1[0]);
                        p0.setValue(a1[1]);
                        ll.put(String.valueOf(ll.size() + 1), p0);
                    } else {
                        break;
                    }
                }

                if (ll.size() == size) {
                    propertyList.add(ll);
                }
            }
        }
        return propertyList;
    }

    /**
     * 返回DB2 数据库实例的端口号
     *
     * @param os
     * @param db2instName 实例名
     * @return
     */
    private int getDB2Port(OS os, String db2instName) {
        if (StringUtils.isInt(db2instName)) {
            return Integer.parseInt(db2instName);
        }

        List<OSService> services = os.getOSService(db2instName);
        for (OSService obj : services) {
            if (obj.getName().equals(db2instName)) {
                return obj.getPort();
            }
        }
        return -1;
    }

    /**
     * 保存DB2 数据库实例
     *
     * @param allUsers
     * @param instList
     * @param stdout
     */
    private void addDB2Instance(List<OSUser> allUsers, List<DB2Instance> instList, String stdout) {
        BufferedLineReader in = new BufferedLineReader(StringUtils.trimBlank(stdout));
        try {
            while (in.hasNext()) {
                String line = in.next();
                if ("sqllib".equalsIgnoreCase(FileUtils.getFilename(FileUtils.getParent(line)))) {
                    for (OSUser user : allUsers) {
                        if (StringUtils.isNotBlank(user.getHome()) && !"/".equals(user.getHome()) && line.startsWith(user.getHome())) {
                            DB2Instance inst = new DB2Instance();
                            inst.setName(user.getName());
                            inst.setUser(user);
                            inst.setDb2profile(line);

                            if (!instList.contains(inst)) {
                                instList.add(inst);
                            }
                            break;
                        }
                    }
                }
            }
        } finally {
            IO.close(in);
        }
    }

    /**
     * 返回操作系统对象
     *
     * @return
     */
    public OS getOS() {
        return this.os;
    }

    /**
     * 向制定操作系统用户配置文件中添加 db2profile 信息
     *
     * @param username 操作系统用户名
     * @return
     * @throws IOException
     */
    public boolean addUserDB2Profile(String username) throws IOException {
        return this.addUserDB2Profile(this.getOS(), username);
    }

    /**
     * 向制定操作系统用户配置文件中添加 db2profile 信息
     *
     * @param os       操作系统
     * @param username 操作系统用户名
     * @return
     * @throws IOException
     */
    public boolean addUserDB2Profile(OS os, String username) throws IOException {
        OSUser user = os.getUser(username);
        if (user == null) {
            throw new NullPointerException();
        }

        String lineSeperator = os.getLineSeparator();
        if (lineSeperator == null) {
            throw new NullPointerException();
        }

        List<String> profiles = new ArrayList<String>(user.getProfiles());
        if (profiles.isEmpty()) {
            profiles.add(NetUtils.joinUri(user.getHome(), "/" + ".bash_profile"));
        }

        Ensure.isTrue(os.supportOSFileCommand() && os.enableOSFileCommand());
        String db2profile = this.getDB2profile();
        String profile = this.existsSourceDB2profile(os, profiles, db2profile);
        if (StringUtils.isBlank(profile)) {
            String filepath = CollUtils.lastElement(profiles);

            StringBuilder env = new StringBuilder();
            env.append(lineSeperator);
            env.append(lineSeperator);
            env.append("# The following three lines have been added by UDB DB2.").append(lineSeperator);
            env.append("if [ -f " + db2profile + " ]; then").append(lineSeperator);
            env.append(". ").append(db2profile).append(lineSeperator);
            env.append("fi").append(lineSeperator);
            env.append("").append(lineSeperator);
            os.getOSFileCommand().write(filepath, os.getOSFileCommand().getCharsetName(), true, env);

            STD.out.info(ResourcesUtils.getDatabaseMessage(52, username, filepath, db2profile));
            return true;
        } else {
            STD.out.info(ResourcesUtils.getDatabaseMessage(53, username, profile, db2profile));
            return false;
        }
    }

    /**
     * 在配置文件中查找指定db2profile 文件是否已经配置
     *
     * @param os           操作系统接口
     * @param userProfiles user profiles
     * @param db2profile   db2profile path
     * @return 返回null表示未配置
     * @throws IOException
     */
    private String existsSourceDB2profile(OS os, List<String> userProfiles, String db2profile) throws IOException {
        if (os == null || !os.supportOSFileCommand() || !os.enableOSFileCommand()) {
            throw new IllegalArgumentException(StringUtils.toString(os));
        }
        if (userProfiles == null) {
            throw new NullPointerException();
        }
        if (StringUtils.isBlank(db2profile)) {
            throw new IllegalArgumentException(db2profile);
        }

        String db2profilepath = StringUtils.escapeRegex(db2profile);
        OSFileCommand cmd = os.getOSFileCommand();
        for (String profile : userProfiles) {
            BufferedLineReader in = new BufferedLineReader(cmd.read(profile, StringUtils.defaultString(cmd.getCharsetName(), StringUtils.CHARSET), 0));
            try {
                while (in.hasNext()) {
                    String line = StringUtils.trimBlank(in.next());
                    if (line.matches("\\s*\\.\\s+" + db2profilepath + "\\s*")) {
                        return FileUtils.getFilename(profile);
                    }
                }
            } finally {
                in.close();
            }
        }
        return null;
    }

    /**
     * 返回 DB2 数据库实例名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 保存 DB2 数据库实例
     *
     * @param name
     */
    protected void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name);
        } else {
            this.name = name;
        }
    }

    /**
     * DB2安装目录
     *
     * @return
     */
    public String getDB2Dir() {
        return db2dir;
    }

    /**
     * 保存 DB2 实例的安装目录
     *
     * @param db2dir
     */
    protected void setDB2Dir(String db2dir) {
        if (StringUtils.isBlank(db2dir)) {
            throw new IllegalArgumentException(db2dir);
        } else {
            this.db2dir = db2dir;
        }
    }

    /**
     * 数据库实例用户
     *
     * @return
     */
    public OSUser getUser() {
        return user;
    }

    /**
     * 保存 DB2 数据库实例用户
     *
     * @param user
     */
    protected void setUser(OSUser user) {
        this.user = user;
    }

    /**
     * 返回 DB2 数据库实例的配置文件绝对路径
     *
     * @return
     */
    public String getDB2profile() {
        return db2profile;
    }

    /**
     * 保存 DB2 数据库实例配置文件绝对路径
     *
     * @param db2profile
     */
    protected void setDb2profile(String db2profile) {
        this.db2profile = db2profile;
    }

    /**
     * 添加 DB2 数据库实例配置信息
     *
     * @param key
     * @param value
     */
    protected void addConfig(String key, String value) {
        this.config.setProperty(key, value);
    }

    /**
     * 返回 DB2 数据库实例配置信息
     *
     * @param key
     * @return
     */
    public String getConfig(String key) {
        return this.config.getProperty(key);
    }

    /**
     * 保存DB2 数据库实例的数据库
     *
     * @param database
     */
    protected void addDatabase(DB2Database database) {
        this.databases.put(database.getName(), database);
    }

    /**
     * 在 DB2 数据库实例中查找数据库
     *
     * @param name 数据库名
     * @return
     */
    public DB2Database getDatabase(String name) {
        return this.databases.get(name);
    }

    /**
     * 返回 DB2 数据库实例上的所有数据库名
     *
     * @return
     */
    public String[] getDatabaseNames() {
        Set<String> names = this.databases.keySet();
        return names.toArray(new String[names.size()]);
    }

    /**
     * 返回数据库服务端口号
     *
     * @return
     */
    public int getPort() {
        return this.port;
    }

    /**
     * 判断DB2 数据库实例是否相等
     */
    public boolean equals(Object obj) {
        if (obj instanceof DB2Instance) {
            DB2Instance inst = (DB2Instance) obj;
            return inst.getName().equals(this.getName());
        } else {
            return false;
        }
    }

    /**
     * 终止 DB2 数据库实例上的应用连接
     *
     * @param conn          被终止的数据库连接
     * @param applicationId 应用连接id
     * @return
     */
    public boolean terminateConnection(Connection conn, String applicationId) {
        if (StringUtils.isBlank(applicationId)) {
            return false;
        }

        try {
            String handle = this.getApplicationHandle(conn, applicationId);
            if (StringUtils.isBlank(handle)) {
                STD.out.warn("DB2 ApplicationId = " + applicationId + " not exists!");
                return false;
            } else if (this.forceApplication(conn, handle)) {
                STD.out.info("terminate DB2 application(" + handle + ") success ..");
                return true;
            } else {
                STD.out.info("terminate DB2 application(" + handle + ") fail ..");
                return false;
            }
        } catch (Exception e) {
            STD.out.error("terminate db2 application " + applicationId + " error!", e);
            return false;
        }
    }

    /**
     * 根据 db2 application id 查找对应的 application handle
     */
    public String getApplicationHandle(Connection conn, String applicationId) throws OSCommandException {
        String applicationHandle = "";
        Ensure.isTrue(this.getOS().supportOSCommand(), this.getOS().enableOSCommand());
        OSCommand cmd = this.getOS().getOSCommand();
        cmd.execute("db2 list applications show detail");
        BufferedLineReader in = new BufferedLineReader(StringUtils.trimBlank(cmd.getStdout()));
        try {
            while (in.hasNext()) {
                String line = in.next();
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug(line);
                }

                if (line.indexOf(applicationId) != -1) {
                    String[] array = StringUtils.splitByBlank(line);
                    if (array.length >= 3 && array[3].equals(applicationId)) {
                        applicationHandle = array[2];
                        break;
                    }
                }
            }
        } finally {
            IO.close(in);
        }

        if (StringUtils.isInt(applicationHandle)) {
            return applicationHandle;
        } else {
            STD.out.error(cmd.getStderr());
            return null;
        }
    }

    /**
     * 强制关闭db2进程
     */
    public boolean forceApplication(Connection conn, String applicationHandle) throws SQLException {
        OSUser user = this.getOS().getUser();
        String url = conn.getMetaData().getURL();
        String username = user.getName();
        String password = user.getPassword();

        // 实现逻辑: 要启动一个新线程连接数据库，在新连接上执行关闭数据库连接的命令
        Connection newconn = Jdbc.getConnection(url, username, password);
        try {
            CallableStatement statement = newconn.prepareCall("call SYSPROC.ADMIN_CMD('force application (" + applicationHandle + ")')");
            statement.execute();
            newconn.commit();
            return true;
        } catch (Exception e) {
            newconn.rollback();
            return false;
        } finally {
            newconn.close();
        }
    }

    public String toString() {
        String str = "DB2Instance [db2dir=" + db2dir + ",name=" + name + ", user=" + user + ", db2profile=" + db2profile + "\n";
        Iterator<Object> it = this.config.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = this.config.get(key);
            str += "db2 dbm cfg " + key + " = " + value + "\n";
        }

        Iterator<String> it0 = this.databases.keySet().iterator();
        while (it0.hasNext()) {
            String key = it0.next();
            Object value = this.databases.get(key);
            str += StringUtils.addLinePrefix(value.toString(), "  ") + "\n";
        }
        str += " ]";
        return str;
    }

}
