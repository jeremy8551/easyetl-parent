package icu.etl.script;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import icu.etl.script.io.ScriptFileExpression;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.script.session.ScriptSubProcess;

/**
 * 用户会话信息 <br>
 * <p>
 * 用户或程序在执行 {@linkplain ScriptEngine#eval(String)} 方法视作一次会话
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptSession {

    /**
     * 设置会话的返回值（即 {@linkplain ScriptEngine#eval(String)} 方法的返回值）
     *
     * @param key   键
     * @param value 值
     * @return 返回null表示没有替换 非null表示被替换的值
     */
    Object putValue(String key, Object value);

    /**
     * 会话的返回值（即 {@linkplain ScriptEngine#eval(String)} 方法的返回值） <br>
     * 如果命令没有返回值，则返回null <br>
     * 如果命令返回了一个值，则直接返回这个值 <br>
     * 如果命令返回了多个值，则将多个值封装到一个 Map 中, 并返回这个 Map 对象（key属性是忽略大小写的）
     *
     * @return 返回值
     */
    Object getValue();

    /**
     * 删除上一个命令的返回值
     */
    void removeValue();

    /**
     * 返回用户会话编号
     *
     * @return
     */
    String getId();

    /**
     * 返回父会话编号
     *
     * @return
     */
    String getParentID();

    /**
     * 返回脚本引擎名称
     *
     * @return
     */
    String getScriptName();

    /**
     * 设置脚本文件
     *
     * @param file 脚本文件表达式
     * @throws IOException 访问文件错误
     */
    void setScriptFile(ScriptFileExpression file) throws IOException;

    /**
     * 判断 true 表示是脚本文件
     *
     * @return
     */
    boolean isScriptFile();

    /**
     * 返回主线程
     *
     * @return
     */
    ScriptMainProcess getMainProcess();

    /**
     * 返回后台运行的进程
     *
     * @return
     */
    ScriptSubProcess getSubProcess();

    /**
     * 返回用户会话的工厂
     *
     * @return
     */
    UniversalScriptSessionFactory getSessionFactory();

    /**
     * 添加变量方法的临时变量
     *
     * @param name  变量名
     * @param value 变量值
     */
    void addMethodVariable(String name, Object value);

    /**
     * 返回变量方法的临时变量值
     *
     * @param name 变量名
     * @return
     */
    Object getMethodVariable(String name);

    /**
     * 删除变量方法的临时变量
     *
     * @param name 变量名
     * @return
     */
    Object removeMethodVariable(String name);

    /**
     * 保存自定义方法的参数值数组
     *
     * @param parameter
     */
    void setFunctionParameter(String[] parameter);

    /**
     * 返回自定义方法的参数值数组
     *
     * @return
     */
    String[] getFunctionParameter();

    /**
     * 删除自定义方法的参数值数组
     */
    void removeFunctionParameter();

    /**
     * 判断当前会话信息是否关闭
     *
     * @return
     */
    boolean isAlive();

    /**
     * 设置echo命令是否可用
     *
     * @param enable true表示可用 false表示不可用
     */
    void setEchoEnabled(boolean enable);

    /**
     * 返回echo命令是否可用
     *
     * @return true表示不可用 false表示可用
     */
    boolean isEchoDisable();

    /**
     * 返回echo命令是否可用
     *
     * @return true表示可用 false表示不可用
     */
    boolean isEchoEnable();

    /**
     * 保存当前用户会话使用的编译器
     *
     * @param compiler 编译器
     */
    void setCompiler(UniversalScriptCompiler compiler);

    /**
     * 返回当前用户会话使用的编译器
     *
     * @return
     */
    UniversalScriptCompiler getCompiler();

    /**
     * 返回语句分析器
     *
     * @return
     */
    UniversalScriptAnalysis getAnalysis();

    /**
     * 返回变量值
     *
     * @return
     */
    <E> E getVariable(String key);

    /**
     * 保存变量
     *
     * @param key   变量名
     * @param value 变量值
     */
    void addVariable(String key, Object value);

    /**
     * 返回变量集合
     *
     * @return
     */
    Map<String, Object> getVariables();

    /**
     * 判断是否存在变量
     *
     * @param name 变量名
     * @return
     */
    boolean containsVariable(String name);

    /**
     * 移除变量
     *
     * @param key 变量名
     * @return 变量值
     */
    Object removeVariable(String key);

    /**
     * 设置脚本引擎默认的目录
     *
     * @param dir
     */
    void setDirectory(File dir);

    /**
     * 返回脚本引擎默认的目录
     *
     * @return
     */
    String getDirectory();

    /**
     * 创建一个子会话
     *
     * @return
     */
    UniversalScriptSession subsession();

    /**
     * 判断当前用户会话是否被终止
     *
     * @return
     */
    boolean isTerminate();

    /**
     * 终止用户会话
     *
     * @throws Exception
     */
    void terminate() throws Exception;

    /**
     * 清空所有信息
     */
    void close();

    /**
     * 返回用户会话创建时间
     *
     * @return
     */
    Date getCreateTime();

    /**
     * 返回用户会话结束时间
     *
     * @return
     */
    Date getEndTime();

    /**
     * 返回临时文件目录
     *
     * @return 目录
     */
    File getTempDir();

}
