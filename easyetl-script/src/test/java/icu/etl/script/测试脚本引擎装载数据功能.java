package icu.etl.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.TestEnv;
import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.os.OS;
import icu.etl.os.OSUser;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;

import static org.junit.Assert.assertTrue;

public class 测试脚本引擎装载数据功能 {

    public static void main(String[] args) throws ScriptException, IOException {
        Class<?> cls = 测试脚本引擎装载数据功能.class;
        AnnotationEasyetlContext cxt = new AnnotationEasyetlContext();

        // 新文件
        OS os = cxt.get(OS.class);
        OSUser user = os.getUser();
        File delfile = new File(user.getHome(), "bhc_finish.del");

        // 复制测试文件
        String packageName = ClassUtils.getPackageName(cls, 2).replace('.', '/');
        String uri = "/" + packageName + "/bhc_finish.del";
        InputStream in = ClassUtils.getResourceAsStream(uri);
        try {
            FileUtils.write(delfile, "UTF-8", false, in);
        } finally {
            in.close();
        }

        Ensure.isTrue(!ResourcesUtils.existsMessage("test.msg.stdout"));
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        if (engine == null) {
            throw new NullPointerException();
        }

        try {
            String classpath = ClassUtils.getClasspath(cls);
            String pkgdir = DatabaseDialectTest.class.getPackage().getName().replace('.', '/');
            String path = FileUtils.joinFilepath(classpath, pkgdir, "testload.sql");

            File file = new File(path);
            assertTrue(file.getAbsolutePath(), file.exists());

            Ensure.isZero(engine.eval("export set databaseDriverName='" + TestEnv.getDBDriver() + "'"));
            Ensure.isZero(engine.eval("export set databaseUrl='" + TestEnv.getDBUrl() + "'"));
            Ensure.isZero(engine.eval("export set username='" + TestEnv.getDBAdmin() + "'"));
            Ensure.isZero(engine.eval("export set password='" + TestEnv.getDBAdminpw() + "'"));
            Ensure.isZero(engine.eval("export set databaseHost='" + TestEnv.getDatabaseHost() + "'"));
            Ensure.isZero(engine.eval("export set databaseSSHUser='" + TestEnv.getDatabaseUser() + "'"));
            Ensure.isZero(engine.eval("export set databaseSSHUserPw='" + TestEnv.getDatabaseUserPassword() + "'"));
            Ensure.isZero(engine.eval("export set delfilepath='" + delfile.getAbsolutePath() + "'"));

            Integer value = (Integer) engine.eval("cd " + file.getParent() + ";" + ". " + file.getAbsolutePath() + " ");
            Ensure.isTrue(value == 0 || value == -3, value);

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
