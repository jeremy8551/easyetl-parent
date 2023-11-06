package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
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

}