package icu.etl.script;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.CharArrayReader;
import java.io.Closeable;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import icu.etl.script.internal.ScriptVariable;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎 <br>
 * <br>
 * 脚本引擎使用说明详见 help 命令: <br>
 * <b> ScriptEngineManager manager = new ScriptEngineManager(); </b> <br>
 * <b> ScriptEngine engine = manager.getEngineByExtension("etl"); </b> <br>
 * <b> engine.eval("help"); </b> <br>
 * <b> engine.eval("exit 0"); </b> <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-06-01
 */
public class UniversalScriptEngine implements ScriptEngine, Closeable {

    /** {@linkplain #toString()} 方法的返回值 */
    private String memo;

    /** true表示脚本引擎已关闭 */
    private AtomicBoolean close;

    /** 脚本引擎上下文信息 */
    protected UniversalScriptContext context;

    /** 脚本引擎的工厂类 */
    protected UniversalScriptEngineFactory factory;

    /** 用户会话信息工厂 */
    protected UniversalScriptSessionFactory sessionFactory;

    /**
     * 初始化
     *
     * @param factory 脚本引擎工厂类
     */
    public UniversalScriptEngine(UniversalScriptEngineFactory factory) {
        this.factory = Ensure.notNull(factory);
        this.memo = UniversalScriptEngine.class.getSimpleName() + "@" + StringUtils.toRandomUUID();
        this.close = new AtomicBoolean(false);
        this.context = new UniversalScriptContext(this);
        this.sessionFactory = factory.buildSessionFactory();
    }

    /**
     * 初始化
     *
     * @param parent 父脚本引擎
     */
    public UniversalScriptEngine(UniversalScriptEngine parent) {
        this(parent.getFactory());
        this.context.setParent(parent.getContext());
    }

    /**
     * 返回脚本中的变量或数据库编目信息
     *
     * @return
     */
    public Object get(String key) {
        return this.context.getAttribute(key);
    }

    /**
     * 返回脚本引擎中指定域的信息
     *
     * @return
     */
    public Bindings getBindings(int scope) {
        return this.context.getBindings(scope);
    }

    /**
     * 返回脚本引擎的上下文信息
     *
     * @return
     */
    public UniversalScriptContext getContext() {
        return this.context;
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
     * 向脚本引擎中添加全局变量
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void put(String name, Object value) {
        this.context.setAttribute(name, value, UniversalScriptContext.ENGINE_SCOPE);
    }

    /**
     * 向指定的域信息
     *
     * @param bindings 脚本引擎域信息
     * @param scope    域标志信息
     */
    public void setBindings(Bindings bindings, int scope) {
        this.context.setBindings(bindings, scope);
    }

    /**
     * 设置脚本引擎上下文信息
     *
     * @param context 脚本引擎上下文信息
     */
    public void setContext(ScriptContext context) {
        this.context = this.castScriptContext(context);
    }

    /**
     * 创建域信息
     */
    public UniversalScriptVariable createBindings() {
        return new ScriptVariable();
    }

    /**
     * 将输入参数强制转换为脚本引擎上下文信息
     *
     * @param context 脚本引擎上下文信息
     * @return 脚本引擎上下文信息
     */
    protected UniversalScriptContext castScriptContext(ScriptContext context) {
        if (context instanceof UniversalScriptContext) {
            return (UniversalScriptContext) context;
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(72, context.getClass().getName(), UniversalScriptContext.class.getName()));
        }
    }

    public Integer eval(String script) {
        CharArrayReader in = new CharArrayReader(script.toCharArray());
        return this.eval(in, this.context);
    }

    public Integer eval(String script, ScriptContext scriptContext) {
        CharArrayReader in = new CharArrayReader(script.toCharArray());
        return this.eval(in, this.castScriptContext(scriptContext));
    }

    public Integer eval(String script, Bindings bindings) {
        this.setBindings(bindings, UniversalScriptContext.ENGINE_SCOPE);
        CharArrayReader in = new CharArrayReader(script.toCharArray());
        return this.eval(in, this.context);
    }

    public Integer eval(Reader in, Bindings bindings) {
        this.setBindings(bindings, UniversalScriptContext.ENGINE_SCOPE);
        return this.eval(in, this.context);
    }

    public Integer eval(Reader in) {
        return this.eval(in, this.context);
    }

    public Integer eval(Reader in, ScriptContext scriptContext) {
        UniversalScriptContext context = this.castScriptContext(scriptContext);
        context.setReader(in);

        int value = -1;
        UniversalScriptSession session = this.sessionFactory.build();
        try {
            value = this.eval(session, context, context.getStdout(), context.getStderr(), false, in);
        } catch (Throwable e) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr146", session.getMainProcess().getErrorScript(), e.getLocalizedMessage()), e);
        } finally {
            session.close();
        }

        if (value == 0) {
            return value;
        } else {
            throw new UniversalScriptException(String.valueOf(value));
        }
    }

    /**
     * 执行脚本语句
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param in          语句输入流
     * @return 返回0表示正确, 返回非0表示不正确
     * @throws Exception 文件访问错误
     */
    public int eval(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Reader in) throws Exception {
        context.getCommandListeners().startScript(session, context, stdout, stderr, forceStdout, in);
        UniversalScriptCompiler oldCompiler = session.getCompiler(); // 保存当前使用的编译器
        UniversalScriptCommand command = null;
        UniversalCommandResultSet resultSet = null;
        ScriptMainProcess process = session.getMainProcess();
        UniversalScriptCompiler compiler = (oldCompiler == null) ? this.getFactory().buildCompiler() : oldCompiler.buildCompiler();
        try {
            compiler.compile(session, context, in); // 执行编译
            session.setCompiler(compiler); // 在读取命令之前一定要保存编译器到用户会话信息中

            while (!session.isTerminate() && compiler.hasNext()) {
                command = compiler.next();
                resultSet = process.execute(session, context, stdout, stderr, forceStdout, command);
                if (resultSet.isExitSession()) {
                    break;
                }
            }

            context.getCommandListeners().exitScript(session, context, stdout, stderr, forceStdout, command, resultSet);
            return resultSet.getExitcode();
        } catch (Exception e) {
            context.getCommandListeners().catchScript(session, context, stdout, stderr, forceStdout, command, resultSet, e);
            return UniversalScriptCommand.ERROR;
        } finally {
            compiler.close(); // 关闭当前编译器

            if (oldCompiler != null) { // 恢复历史编译器
                session.setCompiler(oldCompiler);
            }
        }
    }

    /**
     * 执行命令代换
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param stdout  标准信息输出接口
     * @param stderr  错误信息输出接口
     * @param str     字符串
     * @return
     */
    public int eval(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String str) {
        UniversalScriptCompiler oldCompiler = session.getCompiler(); // 保存当前使用的编译器

        int exitcode = 0;
        ScriptMainProcess process = session.getMainProcess();
        UniversalScriptCompiler compiler = (oldCompiler == null) ? this.getFactory().buildCompiler() : oldCompiler.buildCompiler();
        try {
            compiler.compile(session, context, new CharArrayReader(str.toCharArray())); // 执行编译
            session.setCompiler(compiler); // 在读取命令之前一定要保存编译器到用户会话信息中

            // 继续向下执行
            while (!session.isTerminate() && compiler.hasNext()) {
                UniversalScriptCommand command = compiler.next();
                UniversalCommandResultSet result = process.execute(session, context, stdout, stderr, false, command);
                exitcode = result.getExitcode();
                if (result.isExitSession()) {
                    break;
                } else {
                    continue;
                }
            }

            if (session.isTerminate()) { // 会话已被终止
                stderr.println(ResourcesUtils.getScriptStderrMessage(46, session.getScriptName()));
                return UniversalScriptCommand.TERMINATE;
            } else {
                return exitcode;
            }
        } catch (Throwable e) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(58, str), e);
        } finally {
            compiler.close(); // 关闭当前编译器

            if (oldCompiler != null) { // 恢复历史编译器
                session.setCompiler(oldCompiler);
            }
        }
    }

    /**
     * 返回 true 表示脚本引擎已关闭
     *
     * @return
     */
    public boolean isClose() {
        return this.close.get();
    }

    public void close() {
        if (!this.close.compareAndSet(false, true) || this.context == null) {
            return;
        }

        IO.flushQuietly(this.context.getWriter(), this.context.getErrorWriter(), this.context.getStepWriter());
        this.context.setWriter(null);
        this.context.setWriter(null);
        this.context.setWriter(null);
        this.context.setReader(null);

        this.context.getGlobalVariable().clear(); // 清空全局变量
        this.context.getLocalVariable().clear(); // 清空局部变量
        this.context.getGlobalCatalog().clear(); // 清空全局数据库编目
        this.context.getLocalCatalog().clear(); // 清空局部数据库编目
        this.context.getGlobalPrograms().close(); // 关闭全局程序
        this.context.getLocalPrograms().close(); // 关闭局部程序
    }

    public String toString() {
        return this.memo;
    }

}
