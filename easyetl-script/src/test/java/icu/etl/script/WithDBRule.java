package icu.etl.script;

import javax.script.SimpleBindings;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseURL;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ClassUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.ObjectUtils;
import icu.etl.util.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class WithDBRule implements TestRule {

    /** 容器上下文信息 */
    private EasyBeanContext context;

    /** 脚本引擎的环境变量集合 */
    private SimpleBindings environment;

    /** true表示找不到数据库 */
    private boolean notFindDatabase;

    public WithDBRule() {
    }

    /**
     * 返回容器上下文信息
     *
     * @return 容器上下文信息
     */
    public EasyBeanContext getContext() {
        init();
        return context;
    }

    /**
     * 返回脚本引擎的环境变量集合
     *
     * @return 环境变量集合
     */
    public SimpleBindings getEnvironment() {
        init();
        return environment;
    }

    public Statement apply(Statement statement, Description description) {
        init();
        return new WithDBStatement(this, statement);
    }

    public Connection getConnection() {
        String url = (String) environment.get("databaseUrl");
        String username = (String) environment.get("username");
        String password = (String) environment.get("password");
        return Jdbc.getConnection(url, username, password);
    }

    private void init() {
        if (context == null) {
            context = new EasyBeanContext("sout+:info");

            try {
                environment = new WithDBConfig(context);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String url = (String) environment.get("databaseUrl");
            String username = (String) environment.get("username");
            String password = (String) environment.get("password");
            JdbcDao dao = new JdbcDao(context);
            try {
                this.notFindDatabase = !dao.connect(url, 0, username, password);
            } catch (Exception e) {
                this.notFindDatabase = true;
                e.printStackTrace();
            } finally {
                dao.commit();
                dao.close();
            }
        }
    }

    public static class WithDBStatement extends Statement {
        private Statement statment;

        private WithDBRule rule;

        public WithDBStatement(WithDBRule rule, Statement statment) {
            this.statment = statment;
            this.rule = rule;
        }

        public void evaluate() throws Throwable {
            if (rule.notFindDatabase) {
                System.out.println("**************** 未找到可用的数据库 ****************");
                return;
            }

            try {
                System.out.println("================ 方法开始运行 ====================");
                System.out.println();
                this.statment.evaluate();
            } finally {
                System.out.println();
                System.out.println("================ 方法运行结束 ====================");
            }
        }
    }

    public static class WithDBConfig extends SimpleBindings {

        public WithDBConfig(EasyContext context) throws IOException {
            super();

            Properties p = this.load();
            String jdbcUrl = p.getProperty("database.url");
            List<DatabaseURL> urls = context.getBean(DatabaseDialect.class, jdbcUrl).parseJdbcUrl(jdbcUrl);
            DatabaseURL url = urls.get(0);
            String host = url.getHostname();
            String databaseName = url.getDatabaseName();

            this.put("curr_dir_path", FileUtils.joinPath(ClassUtils.getClasspath(WithDBRule.class), "script"));
            this.put("temp", FileUtils.getTempDir("test", WithDBConfig.class.getSimpleName()).getAbsolutePath());
            this.put("host", host);
            this.put("databaseName", databaseName);
            this.put("databaseDriverName", p.getProperty("database.driverClassName"));
            this.put("databaseUrl", jdbcUrl);
            this.put("username", p.getProperty("database.admin"));
            this.put("password", p.getProperty("database.adminPw"));
            this.put("admin", p.getProperty("database.admin"));
            this.put("adminPw", p.getProperty("database.adminPw"));
            this.put("databaseHost", p.getProperty("database.host"));
            this.put("databaseSSHUser", p.getProperty("database.ssh.username"));
            this.put("databaseSSHUserPw", p.getProperty("database.ssh.password"));
            this.put("ftphost", p.getProperty("ftp.host"));
            this.put("ftpuser", p.getProperty("ftp.username"));
            this.put("ftppass", p.getProperty("ftp.password"));
            this.put("proxyhost", p.getProperty("proxy.host"));
            this.put("proxyuser", p.getProperty("proxy.ssh.username"));
            this.put("proxypass", p.getProperty("proxy.ssh.password"));
            this.put("sshhost", p.getProperty("ssh.host"));
            this.put("sshusername", p.getProperty("ssh.username"));
            this.put("sshpassword", p.getProperty("ssh.password"));
        }

        public Properties load() throws IOException {
            Properties p = new Properties();
            p.load(ClassUtils.getResourceAsStream("/testconfig.properties"));

            String envmode = WithDBRule.class.getPackage().getName() + ".test.mode";
            String mode = ObjectUtils.coalesce(System.getProperty(envmode), "home");
            InputStream in = ClassUtils.getResourceAsStream("/testconfig-" + mode + ".properties");
            if (in != null) {
                p.load(in);
            }

            // 将 Properties 中属性保存到集合中
            for (Iterator<Object> it = p.keySet().iterator(); it.hasNext(); ) {
                String key = StringUtils.trimBlank(it.next());
                String value = StringUtils.trimBlank(p.getProperty(key));
                this.put(key, value);
            }
            return p;
        }

        @Override
        public boolean containsKey(Object key) {
            if (key == null || (key instanceof String && ((String) key).length() == 0)) {
                return false;
            } else {
                return super.containsKey(key);
            }
        }

        @Override
        public Object get(Object key) {
            if (key == null || (key instanceof String && ((String) key).length() == 0)) {
                return null;
            } else {
                return super.get(key);
            }
        }
    }

}
