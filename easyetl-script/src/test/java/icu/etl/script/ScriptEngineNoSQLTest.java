package icu.etl.script;

import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineNoSQLTest {

    public WithDBRule rule = new WithDBRule();

    @Test
    public void test() throws IOException, ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            engine.setBindings(rule.getEnvironment(), UniversalScriptContext.ENVIRONMENT_SCOPE);
            engine.eval(". classpath:/script/testNoDB.sql");
            Assert.fail();
        } catch (ScriptException se) {
            se.printStackTrace(System.out);
            Assert.assertEquals("1000", engine.getContext().getAttribute("testvalue000"));
            Assert.assertEquals("333", StringUtils.splitByBlank(se.getMessage())[1]);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            engine.eval("exit 0");
        }
    }
}
