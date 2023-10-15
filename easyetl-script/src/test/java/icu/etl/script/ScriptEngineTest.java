package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import icu.etl.script.UniversalScriptEngineFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试使用标准日志输出脚本引擎日志
 */
public class ScriptEngineTest {

    @Test
    public void test() {
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory();
        ScriptEngine engine = null;
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
    public void tes1() {
        ScriptEngineManager e = new ScriptEngineManager();
        ScriptEngine engine = null;
        try {
            engine = e.getEngineByExtension("etl");
            engine.eval("help");
            engine.eval("exit 0");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }

}