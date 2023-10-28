package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试使用标准日志输出脚本引擎日志
 */
public class ScriptEngineTest {

    @Test
    public void tes1() {
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

    @Test
    public void test() {
        EasyContext context = new EasyBeanContext("debug:sout");
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory(context);
        ScriptEngine engine;
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