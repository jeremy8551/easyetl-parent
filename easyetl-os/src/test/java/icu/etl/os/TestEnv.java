package icu.etl.os;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import icu.etl.log.STD;
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

}
