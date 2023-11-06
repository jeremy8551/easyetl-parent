package icu.etl.script;

import java.util.Iterator;

import icu.etl.script.internal.CommandCompilerContext;

/**
 * 脚本命令编译器集合
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalCommandRepository {

    /**
     * 加载所有脚本命令的编译器
     *
     * @param context
     */
    void load(UniversalScriptContext context);

    /**
     * 设置默认的编译器
     *
     * @param compiler
     */
    void setDefault(UniversalCommandCompiler compiler);

    /**
     * 返回默认的编译器
     *
     * @return
     */
    UniversalCommandCompiler getDefault();

    /**
     * 判断是否已添加脚本命令
     *
     * @param compiler 脚本命令的编译器类信息
     * @return
     */
    boolean contains(Class<? extends UniversalCommandCompiler> compiler);

    /**
     * 添加脚本命令编译器
     *
     * @param names    脚本命令前缀
     * @param compiler 命令的编译器
     */
    void add(String[] names, UniversalCommandCompiler compiler);

    /**
     * 返回编译器
     *
     * @param cls
     * @return
     */
    <E extends UniversalCommandCompiler> E get(Class<E> cls);

    /**
     * 返回脚本语句对应的脚本命令编译器
     *
     * @param script 脚本语句
     * @return
     */
    UniversalCommandCompiler get(String script);

    /**
     * 返回所有脚本命令（按命令的使用说明排序）
     *
     * @return
     */
    Iterator<CommandCompilerContext> iterator();

    /**
     * 清空所有信息
     */
    void clear();

    /**
     * 返回 true 表示未加载任何脚本引擎变量方法
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 输出所有命令的说明信息
     *
     * @param charsetName 字符集
     * @return 字符图形
     */
    String toString(String charsetName);

}