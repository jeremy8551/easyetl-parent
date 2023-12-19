package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
import icu.etl.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineTest {

    /**
     * 使用JDK的脚本引擎接口测试
     */
    @Test
    public void test1() {
        ScriptEngineManager e = new ScriptEngineManager();
        ScriptEngine engine;
        try {
            engine = e.getEngineByExtension("etl");
            engine.eval("help");
            engine.eval("exit 0");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * 测试使用标准日志输出脚本引擎日志
     */
    @Test
    public void test2() {
        EasyContext context = new EasyBeanContext("debug:sout");
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory(context);
        UniversalScriptEngine engine;
        try {
            engine = factory.getScriptEngine();
            engine.eval("help");
            engine.eval("exit 0");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test3() {
        EasyContext context = new EasyBeanContext("info:sout");
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory(context);
        UniversalScriptEngine engine;
        try {
            engine = factory.getScriptEngine();
            engine.eval("echo 'testvalue' > $temp/test.log; cat $temp/test.log; echo $temp ");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test4() {
        EasyContext context = new EasyBeanContext("info:sout");
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory(context);
        UniversalScriptEngine engine;
        try {
            engine = factory.getScriptEngine();
            File file = (File) engine.eval("cp classpath:/bhc_finish.del ${HOME}");
            System.out.println("复制后的文件: " + file.getAbsolutePath());
            FileUtils.assertFile(file);

            File file2 = (File) engine.eval("cp " + file.getAbsolutePath() + " $temp");
            System.out.println("复制后的文件: " + file2.getAbsolutePath());
            FileUtils.assertFile(file2);

            engine.eval("echo test > $temp/test1/test1.log");
            engine.eval("echo test > $temp/test1/test2/test2.log");

            File dir = (File) engine.eval("cp " + file2.getParent() + " ${HOME}");
            System.out.println("复制后的目录: " + dir);
            FileUtils.assertDirectory(dir);

            System.out.println("结果1: " + engine.eval("wc " + file.getAbsolutePath()) + "|");

            System.out.println("结果2: " + engine.eval("wc -l " + file.getAbsolutePath()));
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }

}