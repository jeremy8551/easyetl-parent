package script;

import java.io.Reader;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * 脚本引擎接口，用于兼容 JDK1.5
 *
 * @author Mike Grogan
 * @since 1.6
 */
public interface ScriptEngine {

    public static final String ARGV = "javax.script.argv";

    public static final String FILENAME = "javax.script.filename";

    public static final String ENGINE = "javax.script.engine";

    public static final String ENGINE_VERSION = "javax.script.engine_version";

    public static final String NAME = "javax.script.name";

    public static final String LANGUAGE = "javax.script.language";

    public static final String LANGUAGE_VERSION = "javax.script.language_version";

    public Object eval(String script, javax.script.ScriptContext context) throws ScriptException;

    public Object eval(Reader reader, javax.script.ScriptContext context) throws ScriptException;

    public Object eval(String script) throws ScriptException;

    public Object eval(Reader reader) throws ScriptException;

    public Object eval(String script, javax.script.Bindings n) throws ScriptException;

    public Object eval(Reader reader, javax.script.Bindings n) throws ScriptException;

    public void put(String key, Object value);

    public Object get(String key);

    public javax.script.Bindings getBindings(int scope);

    public void setBindings(javax.script.Bindings bindings, int scope);

    public Bindings createBindings();

    public javax.script.ScriptContext getContext();

    public void setContext(ScriptContext context);

    public ScriptEngineFactory getFactory();

}
