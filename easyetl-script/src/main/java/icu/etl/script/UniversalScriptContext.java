package icu.etl.script;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import javax.script.Bindings;
import javax.script.ScriptContext;

import icu.etl.io.AliveReader;
import icu.etl.script.internal.ScriptCatalog;
import icu.etl.script.internal.ScriptListener;
import icu.etl.script.internal.ScriptProgram;
import icu.etl.script.io.ScriptStderr;
import icu.etl.script.io.ScriptStdout;
import icu.etl.script.io.ScriptSteper;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎上下文信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-06-01
 */
public class UniversalScriptContext implements ScriptContext {

    /** 局部变量域（只能在当前脚本引擎内访问） */
    public final static int ENGINE_SCOPE = 100;

    /** 全局变量域（可以在当前脚本及其子脚本中访问） */
    public final static int GLOBAL_SCOPE = 200;

    /** 脚本引擎正在执行的语句输入流 */
    private Reader reader;

    /** 父脚本引擎的上下文信息 */
    private UniversalScriptContext parent;

    /** 归属的脚本引擎 */
    private UniversalScriptEngine engine;

    /** 脚本引擎的工厂 */
    private UniversalScriptEngineFactory factory;

    /** 命令监听器集合 */
    private UniversalScriptListener listeners;

    /** 全局变量集合 */
    private UniversalScriptVariable globalVariable;

    /** 局部变量集合 */
    private UniversalScriptVariable localVariable;

    /** 全局数据库编目集合（可以在当前脚本引擎及其子脚本引擎中访问） */
    private ScriptCatalog globalCatalog;

    /** 局部数据库编目集合（只能在当前脚本引擎中使用） */
    private ScriptCatalog localCatalog;

    /** 用于保存用户自定义数据或程序 */
    private ScriptProgram globalPrograms;

    /** 用于保存用户自定义数据或程序 */
    private ScriptProgram localPrograms;

    /** 内部对象转换器 */
    private UniversalScriptFormatter format;

    /** 标准信息输出接口 */
    private UniversalScriptStdout stdout;

    /** 错误信息输出接口 */
    private UniversalScriptStderr stderr;

    /** 步骤信息输出接口 */
    private UniversalScriptSteper steper;

    /** 校验规则 */
    private UniversalScriptChecker checker;

    /**
     * 初始化
     *
     * @param engine 当前脚本引擎上下文信息归属的脚本引擎
     */
    public UniversalScriptContext(UniversalScriptEngine engine) {
        if (engine == null) {
            throw new NullPointerException();
        }

        this.engine = engine;
        this.factory = engine.getFactory();
        this.format = this.factory.buildFormatter();
        this.checker = this.factory.buildChecker();
        this.listeners = new ScriptListener();
        this.globalVariable = this.engine.createBindings();
        this.localVariable = this.engine.createBindings();
        this.globalCatalog = new ScriptCatalog();
        this.localCatalog = new ScriptCatalog();
        this.globalPrograms = new ScriptProgram();
        this.localPrograms = new ScriptProgram();

        this.stdout = new ScriptStdout(this.factory.getStdoutLog(), null, this.format);
        this.stderr = new ScriptStderr(this.factory.getStderrLog(), null, this.format);
        this.steper = new ScriptSteper(this.factory.getStdoutLog(), null, this.format);
    }

    /**
     * 保存父脚本引擎上下文信息
     *
     * @param context 父脚本引擎上下文信息
     */
    public void setParent(UniversalScriptContext context) {
        if (context == null) {
            throw new NullPointerException();
        } else {
            this.parent = context;
        }

        // 复制监听器
        if (context.listeners != null) {
            this.listeners.addAll(context.listeners);
        }

        // 复制程序
        if (context.globalPrograms != null) {
            this.globalPrograms.addAll(context.globalPrograms);
        }

        // 复制全局变量
        if (context.globalVariable != null) {
            this.globalVariable.addAll(context.globalVariable);
        }

        // 复制全局数据库编目信息
        if (context.globalCatalog != null) {
            this.globalCatalog.addAll(context.globalCatalog);
        }
    }

    /**
     * 返回当前脚本引擎对象的父脚本引擎对象
     *
     * @return
     */
    public UniversalScriptContext getParent() {
        return this.parent;
    }

    /**
     * 返回脚本引擎的工厂
     *
     * @return
     */
    public UniversalScriptEngineFactory getFactory() {
        return factory;
    }

    /**
     * 返回脚本引擎上下文信息对应的脚本引擎对象
     *
     * @return
     */
    public UniversalScriptEngine getEngine() {
        return this.engine;
    }

    /**
     * 返回脚本引擎内部对象转换器
     *
     * @return
     */
    public UniversalScriptFormatter getFormatter() {
        return this.format;
    }

    /**
     * 返回校验规则
     *
     * @return
     */
    public UniversalScriptChecker getChecker() {
        return this.checker;
    }

    /**
     * 返回用户设置的全局和局部变量中的字符集名
     *
     * @return
     */
    public String getCharsetName() {
        Object value = this.getAttribute(UniversalScriptVariable.VARNAME_CHARSET);
        if (value instanceof String) {
            return (String) value;
        } else {
            return StringUtils.CHARSET;
        }
    }

    /**
     * 返回执行命令的监听器
     *
     * @return
     */
    public UniversalScriptListener getCommandListeners() {
        return listeners;
    }

    /**
     * 添加一个程序
     *
     * @param value
     */
    public void addProgram(String name, Object value, boolean global) {
        if (global) {
            if (this.globalPrograms.containsKey(name)) {
                throw new UnsupportedOperationException(name);
            } else {
                this.globalPrograms.put(name, value);
            }
        } else {
            if (this.localPrograms.containsKey(name)) {
                throw new UnsupportedOperationException(name);
            } else {
                this.localPrograms.put(name, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E> E getProgram(String name, boolean global) {
        if (global) {
            return (E) this.globalPrograms.get(name);
        } else {
            return (E) this.localPrograms.get(name);
        }
    }

    /**
     * 返回局部程序集合
     *
     * @return
     */
    protected ScriptProgram getLocalPrograms() {
        return this.localPrograms;
    }

    /**
     * 返回全局程序集合
     *
     * @return
     */
    protected ScriptProgram getGlobalPrograms() {
        return this.globalPrograms;
    }

    /**
     * 判断是否已配置局部数据库编目信息
     *
     * @param name 数据库编目名
     * @return
     */
    public boolean containsLocalCatalog(String name) {
        return this.localCatalog.containsKey(name.toUpperCase());
    }

    /**
     * 添加局部数据库编目信息
     *
     * @param name  数据库编目名
     * @param value 数据库编目所在文件或数据库编目所在 Properties 对象
     * @throws IOException
     */
    public Properties addLocalCatalog(String name, Object value) throws IOException {
        if (StringUtils.isBlank(name) || value == null) {
            throw new IllegalArgumentException();
        } else if (value instanceof Properties) {
            return this.localCatalog.put(name.toUpperCase(), (Properties) value);
        } else if (value instanceof String) {
            Properties catalog = FileUtils.loadProperties((String) value);
            return this.localCatalog.put(name.toUpperCase(), catalog);
        } else {
            throw new UnsupportedOperationException(value.getClass().getName());
        }
    }

    /**
     * 返回指定数据库编目信息
     *
     * @param name 数据库编目名
     * @return 数据库编目信息
     */
    public Properties getLocalCatalog(String name) {
        return this.localCatalog.get(name.toUpperCase());
    }

    /**
     * 返回局部数据库编目集合
     *
     * @return
     */
    protected ScriptCatalog getLocalCatalog() {
        return this.localCatalog;
    }

    /**
     * 删除局部数据库编目信息
     *
     * @param name 数据库编目名
     * @return
     */
    public Properties removeLocalCatalog(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name);
        } else {
            return this.localCatalog.remove(name.toUpperCase());
        }
    }

    /**
     * 返回数据库编目信息，优先使用全局数据库编目信息。
     *
     * @param name 数据库编目名
     * @return 数据库编目信息
     */
    public Properties getCatalog(String name) {
        String key = name.toUpperCase();
        Properties obj = this.globalCatalog.get(key);
        if (obj == null) {
            return this.localCatalog.get(key);
        } else {
            return obj;
        }
    }

    /**
     * 添加全局数据库编目信息
     *
     * @param name  数据库编目名
     * @param value 数据库编目信息，可以是 Properties 或 filepath
     * @throws IOException
     */
    public Properties addGlobalCatalog(String name, Object value) throws IOException {
        if (StringUtils.isBlank(name) || value == null) {
            throw new IllegalArgumentException();
        } else if (value instanceof Properties) {
            return this.globalCatalog.put(name.toUpperCase(), (Properties) value);
        } else if (value instanceof String) {
            Properties catalog = FileUtils.loadProperties((String) value);
            return this.globalCatalog.put(name.toUpperCase(), catalog);
        } else {
            throw new UnsupportedOperationException(value.getClass().getName());
        }
    }

    /**
     * 返回指定数据库编目信息
     *
     * @param name 数据库编目名
     * @return 数据库编目信息
     */
    public Properties getGlobalCatalog(String name) {
        return this.globalCatalog.get(name.toUpperCase());
    }

    /**
     * 返回全局数据库编目集合
     *
     * @return
     */
    protected ScriptCatalog getGlobalCatalog() {
        return this.globalCatalog;
    }

    /**
     * 删除指定数据库编目信息
     *
     * @param name 数据库编目名
     * @return
     */
    public Properties removeGlobalCatalog(String name) {
        return this.globalCatalog.remove(name.toUpperCase());
    }

    /**
     * 返回局部变量集合
     *
     * @return
     */
    public UniversalScriptVariable getLocalVariable() {
        return this.localVariable;
    }

    /**
     * 判断是否存在局部变量
     *
     * @param name 变量名
     * @return
     */
    public boolean containsLocalVariable(String name) {
        return this.localVariable.containsKey(name);
    }

    /**
     * 添加局部变量
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void addLocalVariable(String name, Object value) {
        this.localVariable.put(name, value == null ? "" : value);
    }

    /**
     * 返回局部变量值
     *
     * @param name 变量名
     * @return
     */
    public Object getLocalVariable(String name) {
        return this.localVariable.get(name);
    }

    /**
     * 返回全局变量集合
     *
     * @return
     */
    public UniversalScriptVariable getGlobalVariable() {
        return this.globalVariable;
    }

    /**
     * 判断是否存在全局变量
     *
     * @param name 变量名
     * @return
     */
    public boolean containsGlobalVariable(String name) {
        return this.globalVariable.containsKey(name);
    }

    /**
     * 添加全局变量
     *
     * @param name  变量名
     * @param value variable value
     */
    public void addGlobalVariable(String name, Object value) {
        if (value == null) {
            value = "";
        }

        this.globalVariable.put(name, value);
        if (this.localVariable.containsKey(name)) {
            this.localVariable.put(name, value);
        }
    }

    /**
     * 返回全局变量值
     *
     * @param name 变量名
     * @return
     */
    public Object getGlobalVariable(String name) {
        return this.globalVariable.get(name);
    }

    /**
     * 将参数集合中的所有参数添加到指定域中
     *
     * @param bindings 参数集合
     * @param scope    域的编号 <br>
     *                 {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     *                 {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public void setBindings(Bindings bindings, int scope) {
        switch (scope) {
            case UniversalScriptContext.ENGINE_SCOPE:
                if (bindings != null) {
                    this.localVariable.putAll(bindings);
                }
                break;

            case UniversalScriptContext.GLOBAL_SCOPE:
                if (bindings != null) {
                    this.globalVariable.putAll(bindings);
                }
                break;

            default:
                throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(59, scope));
        }
    }

    /**
     * 判断是否存在变量
     *
     * @param name 变量名
     * @return
     */
    public boolean containsAttribute(String name) {
        return this.globalVariable.containsKey(name) || this.localVariable.containsKey(name);
    }

    /**
     * 返回变量值 <br>
     * 如果在不同的域中存在同名的变量名时，按域的优先级从高到低返回变量值，域的优先级如下：<br>
     * {@literal 局部变量域 > 全局变量域 > 全局数据库编目域 > 内置变量域 }
     */
    public Object getAttribute(String name) {
        if (this.localVariable.containsKey(name)) {
            return this.getLocalVariable(name);
        }

        if (this.globalVariable.containsKey(name)) {
            return this.getGlobalVariable(name);
        }

        return null;
    }

    /**
     * 返回变量值
     *
     * @param name  变量名
     * @param scope 域的编号 <br>
     *              {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     *              {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case UniversalScriptContext.ENGINE_SCOPE:
                return this.getLocalVariable(name);

            case UniversalScriptContext.GLOBAL_SCOPE:
                return this.getGlobalVariable(name);

            default:
                throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(59, scope));
        }
    }

    /**
     * 删除指定域中的变量
     *
     * @param name  变量名
     * @param scope 域的编号 <br>
     *              {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     *              {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public Object removeAttribute(String name, int scope) {
        switch (scope) {
            case UniversalScriptContext.ENGINE_SCOPE:
                return this.localVariable.remove(name);

            case UniversalScriptContext.GLOBAL_SCOPE:
                return this.globalVariable.remove(name);

            default:
                throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(59, scope));
        }
    }

    /**
     * 添加变量到指定域中
     *
     * @param name  变量名
     * @param value 变量值
     * @param scope 域的编号 <br>
     *              {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     *              {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public void setAttribute(String name, Object value, int scope) {
        switch (scope) {
            case UniversalScriptContext.ENGINE_SCOPE:
                this.addLocalVariable(name, value);
                return;

            case UniversalScriptContext.GLOBAL_SCOPE:
                this.addGlobalVariable(name, value);
                return;
        }

        throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(59, scope));
    }

    /**
     * 返回变量所在的域编号 <br>
     * 如果在不同的域中存在同名的变量名时，按域的优先级从高到低返回，域的优先级如下：<br>
     * {@literal 局部变量域 > 全局变量域 > 全局数据库编目域 > 内置变量域 }
     *
     * @param name 变量名
     * @return 域的编号 <br>
     * {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     * {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public int getAttributesScope(String name) {
        if (this.localVariable.containsKey(name)) {
            return UniversalScriptContext.ENGINE_SCOPE;
        } else if (this.globalVariable.containsKey(name)) {
            return UniversalScriptContext.GLOBAL_SCOPE;
        } else {
            return -1;
        }
    }

    /**
     * 返回指定域的变量集合
     *
     * @param scope 域的编号 <br>
     *              {@link UniversalScriptContext#ENGINE_SCOPE} <br>
     *              {@link UniversalScriptContext#GLOBAL_SCOPE} <br>
     */
    public Bindings getBindings(int scope) {
        switch (scope) {
            case UniversalScriptContext.ENGINE_SCOPE:
                return this.localVariable;

            case UniversalScriptContext.GLOBAL_SCOPE:
                return this.globalVariable;

            default:
                throw new IllegalArgumentException(ResourcesUtils.getScriptStderrMessage(59, scope));
        }
    }

    /**
     * 返回脚本引擎支持的所有域
     */
    public List<Integer> getScopes() {
        return ArrayUtils.asList(UniversalScriptContext.ENGINE_SCOPE, UniversalScriptContext.GLOBAL_SCOPE);
    }

    /**
     * 返回读取脚本语句的 Reader
     */
    public Reader getReader() {
        return this.reader;
    }

    /**
     * 设置读取脚本语句的 Reader
     */
    public void setReader(Reader reader) {
        this.reader = new AliveReader(reader);
    }

    /**
     * 设置输出标准信息使用的 Writer
     */
    public void setWriter(Writer writer) {
        this.stdout.setWriter(writer);
    }

    /**
     * 返回输出标准信息使用的 Writer
     */
    public Writer getWriter() {
        return this.stdout.getWriter();
    }

    /**
     * 设置用于显示错误输出的 Writer
     */
    public void setErrorWriter(Writer writer) {
        this.stderr.setWriter(writer);
    }

    /**
     * 返回用于显示错误输出的 Writer
     */
    public Writer getErrorWriter() {
        return this.stderr.getWriter();
    }

    /**
     * 设置用于输出步骤信息的 Writer
     *
     * @param writer
     */
    public void setStepWriter(Writer writer) {
        this.steper.setWriter(writer);
    }

    /**
     * 返回用于输出步骤信息的 Writer
     *
     * @return
     */
    public Writer getStepWriter() {
        return this.steper.getWriter();
    }

    /**
     * 返回脚本引擎的标准输出对象
     *
     * @return
     */
    public UniversalScriptStdout getStdout() {
        return this.stdout;
    }

    /**
     * 返回脚本引擎的错误输出对象
     *
     * @return
     */
    public UniversalScriptStderr getStderr() {
        return this.stderr;
    }

    /**
     * 返回脚本引擎的步骤输出对象
     *
     * @return
     */
    public UniversalScriptSteper getSteper() {
        return this.steper;
    }

}
