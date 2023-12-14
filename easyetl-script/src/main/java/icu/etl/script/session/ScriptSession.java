package icu.etl.script.session;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCompiler;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptSessionFactory;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.io.ScriptFileExpression;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎用户会话信息接口的实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptSession implements UniversalScriptSession {

    /** 会话编号 */
    private String id;

    /** 父会话编号 */
    private String pid;

    /** 表示用户会话归属的集合 */
    private ScriptSessionFactory factory;

    /** 会话创建时间 */
    private Date startTime;

    /** 会话结束时间 */
    private Date endTime;

    /** 主线程 */
    private ScriptMainProcess main;

    /** 子线程 */
    private ScriptSubProcess subs;

    /** 用户自定义方法的参数 */
    private String[] functionParameters;

    /** true表示会话正在使用 */
    private boolean isAlive;

    /** echo命令是否可用，true表示可用 false表示不可用 */
    private boolean echoEnabled;

    /** true表示会话已被终止 */
    private volatile boolean terminate;

    /** 变量方法的缓存变量 */
    private Hashtable<String, Object> methodVariable;

    /** 语句分析器 */
    private UniversalScriptAnalysis anlaysis;

    /** 编译器 */
    private UniversalScriptCompiler compiler;

    /** 变量集合 */
    private Map<String, Object> variable;

    /** 脚本引擎名（可以不唯一） */
    private String name;

    /** 临时文件存储目录 */
    private File tempDir;

    /**
     * 初始化
     */
    private ScriptSession() {
        this.id = "M" + StringUtils.toRandomUUID();
        this.name = ResourcesUtils.getScriptStdoutMessage(14); // 脚本引擎
        this.isAlive = true;
        this.startTime = new Date();
        this.endTime = null;
        this.variable = new HashMap<String, Object>();
        this.methodVariable = new Hashtable<String, Object>();
        this.main = new ScriptMainProcess();
        this.subs = new ScriptSubProcess();
        this.echoEnabled = true;
        this.terminate = false;
        this.tempDir = FileUtils.getTempDir(UniversalScriptEngine.class.getSimpleName(), "engine", "session", this.id);

        this.setDirectory(Settings.getUserHome()); // 会话当前目录
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_TEMP, this.tempDir.getAbsolutePath()); // 临时文件目录
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_HOME, Settings.getUserHome()); // 用户目录
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_USER, Settings.getUserName()); // 当前用户名
    }

    /**
     * 初始化
     *
     * @param factory 脚本引擎会话工厂
     */
    public ScriptSession(ScriptSessionFactory factory) {
        this();
        this.factory = factory;
    }

    /**
     * 设置脚本文件信息
     *
     * @param file 脚本文件表达式
     */
    public void setScriptFile(ScriptFileExpression file) {
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_SCRIPTNAME, file.getName());
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_SCRIPTFILE, file.getAbsolutePath());
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_LINESEPARATOR, file.getLineSeparator());

        StringBuilder buf = new StringBuilder();
        if (StringUtils.isNotBlank(this.pid) && this.factory.get(this.pid).isScriptFile()) {
            buf.append(ResourcesUtils.getScriptStdoutMessage(16)); // 子脚本文件
        } else {
            buf.append(ResourcesUtils.getScriptStdoutMessage(17)); // 脚本文件
        }
        buf.append(file.getAbsolutePath());
        this.name = buf.toString();
    }

    public boolean isScriptFile() {
        return this.containsVariable(UniversalScriptVariable.SESSION_VARNAME_SCRIPTFILE);
    }

    public String getScriptName() {
        return this.name;
    }

    public ScriptMainProcess getMainProcess() {
        return this.main;
    }

    public ScriptSubProcess getSubProcess() {
        return this.subs;
    }

    public UniversalScriptSessionFactory getSessionFactory() {
        return this.factory;
    }

    public void addMethodVariable(String name, Object value) {
        this.methodVariable.put(name, value);
    }

    public Object getMethodVariable(String name) {
        return this.methodVariable.get(name);
    }

    public Object removeMethodVariable(String name) {
        return this.methodVariable.remove(name);
    }

    public void setFunctionParameter(String[] parameter) {
        this.functionParameters = parameter;
    }

    public String[] getFunctionParameter() {
        return this.functionParameters;
    }

    public void removeFunctionParameter() {
        this.functionParameters = null;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public String getId() {
        return this.id;
    }

    public String getParentID() {
        return this.pid;
    }

    public void setEchoEnabled(boolean enable) {
        this.echoEnabled = enable;
    }

    public boolean isEchoDisable() {
        return !this.echoEnabled;
    }

    public boolean isEchoEnable() {
        return this.echoEnabled;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setDirectory(File dir) {
        FileUtils.assertDirectory(dir);
        this.addVariable(UniversalScriptVariable.SESSION_VARNAME_PWD, dir.getAbsolutePath());
    }

    public String getDirectory() {
        return this.getVariable(UniversalScriptVariable.SESSION_VARNAME_PWD);
    }

    public void terminate() throws Exception {
        if (!this.terminate) {
            this.terminate = true;
            if (this.compiler != null) {
                this.compiler.terminate();
            }
            this.main.terminate();
            this.subs.terminate();
        }
    }

    public void setCompiler(UniversalScriptCompiler compiler) {
        this.compiler = Ensure.notNull(compiler);
        this.anlaysis = compiler.getAnalysis();
    }

    public UniversalScriptCompiler getCompiler() {
        return this.compiler;
    }

    public UniversalScriptAnalysis getAnalysis() {
        return this.anlaysis;
    }

    @SuppressWarnings("unchecked")
    public <E> E getVariable(String key) {
        return (E) this.variable.get(key);
    }

    public void addVariable(String key, Object value) {
        this.variable.put(key, value);
    }

    public Object removeVariable(String key) {
        return this.variable.remove(key);
    }

    public Map<String, Object> getVariables() {
        return this.variable;
    }

    public boolean containsVariable(String name) {
        return this.variable.containsKey(name);
    }

    public File getTempDir() {
        return this.tempDir;
    }

    public Date getCreateTime() {
        return this.startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public UniversalScriptSession subsession() {
        ScriptSession session = new ScriptSession();
        session.factory = this.factory;
        session.pid = this.id;
        session.id = "S" + StringUtils.toRandomUUID();
        session.echoEnabled = this.echoEnabled;
        session.anlaysis = this.anlaysis;
        session.compiler = null;
        session.variable.putAll(this.variable);
        session.subs = this.subs; // 同步子线程
        this.factory.add(session);
        return session;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof UniversalScriptSession) {
            UniversalScriptSession session = (UniversalScriptSession) obj;
            return session.getId().equals(this.id);
        } else {
            return false;
        }
    }

    public void close() {
        this.isAlive = false;
        this.factory.remove(this.id);
        this.endTime = new Date();
    }

}
