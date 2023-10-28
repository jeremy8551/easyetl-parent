package icu.etl;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import icu.etl.database.Jdbc;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.log.STD;
import icu.etl.os.OSConnectCommand;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 测试环境初始化
 *
 * @author jeremy8551@qq.com
 */
public class TestEnv {
    public static final String envmode = TestEnv.class.getPackage().getName() + ".test.mode";

    private static Properties p = new Properties();

    /** 容器上下文信息 */
    private static EasyBeanContext context = new EasyBeanContext();

    static {
        if (p.isEmpty()) {
            TestEnv.load();
        }
    }

    public static void main(String[] args) {
        Set<Object> keys = TestEnv.p.keySet();
        for (Object key : keys) {
            System.out.println(key + " = " + p.get(key));
        }
    }

    /**
     * 加载分环境变量
     */
    public static synchronized void load() {
        try {
            p.load(ClassUtils.getResourceAsStream("/testconfig.properties"));

            String mode = Optional.ofNullable(System.getProperty(envmode)).orElse("home");
            InputStream in = ClassUtils.getResourceAsStream("/testconfig-" + mode + ".properties");
            if (in != null) {
                p.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立一个数据库链接
     *
     * @return
     */
    public static final Connection getConnection() {
        ClassUtils.loadClass(getDBDriver());
        return Jdbc.getConnection(getDBUrl(), getDBAdmin(), getDBAdminpw());
    }

    /**
     * 返回 JDBC 连接参数集合
     *
     * @return
     */
    public static Properties getJdbcconfig() {
        Properties p = new Properties();
        p.put(Jdbc.driverClassName, getDBDriver());
        p.put(Jdbc.url, getDBUrl());
        p.put(OSConnectCommand.username, getDBAdmin());
        p.put(OSConnectCommand.password, getDBAdminpw());
        p.put("adminUsername", getDBAdmin());
        p.put("adminPassword", getDBAdminpw());
        return p;
    }

    public static String getMailHost() {
        return p.getProperty("mail.host");
    }

    public static String getMailUsername() {
        return p.getProperty("mail.username");
    }

    public static String getMailPassword() {
        return p.getProperty("mail.password");
    }

    public static String getMailCharset() {
        return p.getProperty("mail.charset");
    }

    public static String getDBDriver() {
        return p.getProperty("database.driverClassName");
    }

    public static String getDBUrl() {
        return p.getProperty("database.url");
    }

    public static String getDBUsername() {
        return p.getProperty("database.username");
    }

    public static String getDBpassword() {
        return p.getProperty("database.password");
    }

    public static String getDBAdmin() {
        return p.getProperty("database.admin");
    }

    public static String getDBAdminpw() {
        return p.getProperty("database.adminPw");
    }

    public static String getFtpHost() {
        return p.getProperty("ftp.host");
    }

    public static int getFtpPort() {
        return Integer.parseInt(p.getProperty("ftp.port"));
    }

    public static String getFtpUsername() {
        return p.getProperty("ftp.username");
    }

    public static String getFtpPassword() {
        return p.getProperty("ftp.password");
    }

    public static String getSSHHost() {
        return p.getProperty("ssh.host");
    }

    public static int getSSHPort() {
        String str = p.getProperty("ssh.port");
        if (StringUtils.isBlank(str)) {
            STD.out.error("请在java虚拟机中设置属性 " + envmode);
        }
        return Integer.parseInt(str);
    }

    public static String getSSHUsername() {
        return p.getProperty("ssh.username");
    }

    public static String getSSHPassword() {
        return p.getProperty("ssh.password");
    }

    public static String getProxyHost() {
        return p.getProperty("proxy.host");
    }

    public static int getProxyPort() {
        return Integer.parseInt(p.getProperty("proxy.ssh.port"));
    }

    public static String getProxyUsername() {
        return p.getProperty("proxy.ssh.username");
    }

    public static String getProxyPassword() {
        return p.getProperty("proxy.ssh.password");
    }

    public static String getDatabaseHost() {
        return p.getProperty("database.host");
    }

    public static String getDatabaseUser() {
        return p.getProperty("database.ssh.username");
    }

    public static String getDatabaseUserPassword() {
        return p.getProperty("database.ssh.password");
    }

}
