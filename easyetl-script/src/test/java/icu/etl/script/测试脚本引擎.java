package icu.etl.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import icu.etl.TestEnv;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseURL;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.os.OS;
import icu.etl.os.OSUser;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class 测试脚本引擎 {

    public static void main(String[] args) throws IOException {
        new 测试脚本引擎().test();
    }

    @Test
    public void test() throws IOException {
        System.setProperty("tls.logger", "sout");
        System.setProperty("tls.includes", "icu.etl,atom.jar");
        EasyBeanContext context = new EasyBeanContext();

        // 测试外部资源文件
        String filename = "script_res.properties";
        File pf = new File(FileUtils.getTempDir(this.getClass()), filename);
        FileUtils.write(pf, "utf-8", false, ClassUtils.getResourceAsStream("/" + filename));
        System.setProperty(ResourcesUtils.PROPERTY_RESOURCE, pf.getAbsolutePath());
        System.out.println(pf.getAbsolutePath());

        // 新文件
        OS os = context.getBean(OS.class);
        OSUser user = os.getUser();
        File delfile = new File(user.getHome(), "bhc_finish.del");

        // 复制测试文件
        String packageName = ClassUtils.getPackageName(this.getClass(), 2).replace('.', '/');
        String uri = "/" + packageName + "/bhc_finish.del";
        InputStream in = ClassUtils.getResourceAsStream(uri);
        try {
            FileUtils.write(delfile, "UTF-8", false, in);
        } finally {
            in.close();
        }

        Ensure.isTrue(!ResourcesUtils.existsMessage("test.msg.stdout"));

        String url = TestEnv.getDBUrl();

        DatabaseDialect dialect = context.getBean(DatabaseDialect.class, url);
        List<DatabaseURL> config = dialect.parseJdbcUrl(url);
        DatabaseURL p = config.get(0);
        String host = p.getHostname();
        String databaseName = p.getDatabaseName();

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        if (engine == null) {
            throw new NullPointerException();
        }

        try {
            String classpath = ClassUtils.getClasspath(测试脚本引擎.class);
            String path = FileUtils.joinFilepath(classpath, DatabaseDialectTest.class.getPackage().getName().replace('.', '/'), "test.sql");

            File file = new File(path);
            assertTrue(file.getAbsolutePath(), file.exists());

            Ensure.isZero(engine.eval("export set curr_dir_path='" + file.getParentFile().getAbsolutePath() + "'"));
            Ensure.isZero(engine.eval("export set temp='" + FileUtils.getTempDir(UniversalScriptContext.class).getAbsolutePath() + "'"));

            Ensure.isZero(engine.eval("export set host='" + host + "'")); // database ip
            Ensure.isZero(engine.eval("export set databaseName='" + databaseName + "'"));
            Ensure.isZero(engine.eval("export set databaseDriverName='" + TestEnv.getDBDriver() + "'"));
            Ensure.isZero(engine.eval("export set databaseUrl='" + TestEnv.getDBUrl() + "'"));
            Ensure.isZero(engine.eval("export set username='" + TestEnv.getDBAdmin() + "'"));
            Ensure.isZero(engine.eval("export set password='" + TestEnv.getDBAdminpw() + "'"));
            Ensure.isZero(engine.eval("export set admin='" + TestEnv.getDBAdmin() + "'"));
            Ensure.isZero(engine.eval("export set adminPw='" + TestEnv.getDBAdminpw() + "'"));

            Ensure.isZero(engine.eval("export set databaseHost='" + TestEnv.getDatabaseHost() + "'"));
            Ensure.isZero(engine.eval("export set databaseSSHUser='" + TestEnv.getDatabaseUser() + "'"));
            Ensure.isZero(engine.eval("export set databaseSSHUserPw='" + TestEnv.getDatabaseUserPassword() + "'"));

            Ensure.isZero(engine.eval("export set ftphost='" + TestEnv.getFtpHost() + "'"));
            Ensure.isZero(engine.eval("export set ftpuser='" + TestEnv.getFtpUsername() + "'"));
            Ensure.isZero(engine.eval("export set ftppass='" + TestEnv.getFtpPassword() + "'"));

            Ensure.isZero(engine.eval("export set proxyhost='" + TestEnv.getProxyHost() + "'"));
            Ensure.isZero(engine.eval("export set proxyuser='" + TestEnv.getProxyUsername() + "'"));
            Ensure.isZero(engine.eval("export set proxypass='" + TestEnv.getProxyPassword() + "'"));

            Ensure.isZero(engine.eval("export set sshhost='" + TestEnv.getSSHHost() + "'"));
            Ensure.isZero(engine.eval("export set sshusername='" + TestEnv.getSSHUsername() + "'"));
            Ensure.isZero(engine.eval("export set sshpassword='" + TestEnv.getSSHPassword() + "'"));

            Ensure.isZero(engine.eval("export set delfilepath='" + delfile.getAbsolutePath() + "'"));

            Ensure.isZero(engine.eval("echo $host $admin $adminPw $jdbcfilepath "));

            Integer value = (Integer) engine.eval("cd " + file.getParent() + ";" + ". " + file.getAbsolutePath() + " ");
            Ensure.isTrue(value == 0 || value == -3, value);

            Ensure.equals(engine.getContext().getAttribute("testvalue000"), "1000");

//			Integer v0 = (Integer) engine.eval(" close ; ");
//			Asserts.assertTrue(v0 == 0);
//			
//			Integer value = (Integer) engine.eval("echo test .."); // test echo string
//			Asserts.assertTrue(value == -2, value);

            Ensure.isTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            Ensure.isTrue(false);
        } finally {
            try {
                engine.eval("exit 0");
            } catch (Throwable e) {
                e.printStackTrace();
                Ensure.isTrue(false);
            }
        }
    }
}
