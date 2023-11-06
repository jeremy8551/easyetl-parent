package icu.etl.script;

import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import icu.etl.ioc.EasyBeanContext;
import org.junit.Rule;
import org.junit.Test;

/**
 * 测试数据装载命令
 */
public class ScriptEngineDBLoadCmdTest {

    @Rule
    public WithDBRule rule = new WithDBRule();

    @Test
    public void test() throws ScriptException, IOException {
        // System.setProperty("icu.etl.dblog", "true");
        EasyBeanContext context = rule.getContext();
        UniversalScriptEngineFactory manager = new UniversalScriptEngineFactory(context);
        ScriptEngine engine = manager.getScriptEngine();
        engine.setBindings(rule.getEnvironment(), UniversalScriptContext.ENVIRONMENT_SCOPE);
        engine.eval(". classpath:/script/testload.sql");
    }

}
