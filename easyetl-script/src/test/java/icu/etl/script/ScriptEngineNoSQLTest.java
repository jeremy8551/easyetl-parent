package icu.etl.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseURL;
import icu.etl.ioc.BeanContext;
import icu.etl.ioc.BeanFactory;
import icu.etl.os.OS;
import icu.etl.os.OSUser;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ScriptEngineNoSQLTest {

    @Test
    public void test() throws IOException {
        BeanContext context = new BeanContext("icu.etl,sout:");

        // 设置外部资源文件
        System.setProperty(ResourcesUtils.PROPERTY_RESOURCE, FileUtils.joinFilepath(ClassUtils.getClasspath(this.getClass()), "script_res.properties"));

        // 新文件
        OS os = BeanFactory.get(OS.class);
        OSUser user = os.getUser();
        File delfile = new File(user.getHome(), "bhc_finish.del");

        // 复制测试文件
        InputStream in = ClassUtils.getResourceAsStream("/bhc_finish.del");
        Assert.assertNotNull(in);
        try {
            FileUtils.write(delfile, "UTF-8", false, in);
        } finally {
            IO.close(in);
        }

        Ensure.isTrue(!ResourcesUtils.existsMessage("test.msg.stdout"));

        String url = TestEnv.getDBUrl();
        DatabaseDialect dialect = BeanFactory.get(DatabaseDialect.class, url);
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
            String path = FileUtils.joinFilepath(ClassUtils.getClasspath(this.getClass()), "script");
            File file = new File(path);
            assertTrue(file.getAbsolutePath(), file.exists());

            Ensure.isZero(engine.eval("export set curr_dir_path='" + file.getAbsolutePath() + "'"));
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

            engine.eval("cd " + file + "; . classpath:/script/testNoDB.sql");
            Assert.fail();
        } catch (ScriptException se) {
            Assert.assertEquals("1000", engine.getContext().getAttribute("testvalue000"));
            Assert.assertEquals("333", StringUtils.splitByBlank(se.getMessage())[1]);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            try {
                engine.eval("exit 0");
            } catch (Throwable e) {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }
}
