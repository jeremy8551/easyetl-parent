package icu.etl.script;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

/**
 * 编译器
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptCompiler {

    /**
     * 创建子编译器
     *
     * @return
     */
    UniversalScriptCompiler buildCompiler();

    /**
     * 执行编译操作
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param in      脚本语句输入流
     * @throws IOException
     * @throws SQLException
     */
    void compile(UniversalScriptSession session, UniversalScriptContext context, Reader in) throws IOException, SQLException;

    /**
     * 终止编译操作 {@link #compile(UniversalScriptSession, UniversalScriptContext, Reader)} 方法
     *
     * @throws IOException
     * @throws SQLException
     */
    void terminate() throws IOException, SQLException;

    /**
     * 返回 true 表示已成功编译一个命令
     *
     * @return
     * @throws IOException
     * @throws SQLException
     */
    boolean hasNext() throws IOException, SQLException;

    /**
     * 返回编译成功的命令
     *
     * @return
     */
    UniversalScriptCommand next();

    /**
     * 返回语句分析器
     *
     * @return
     */
    UniversalScriptAnalysis getAnalysis();

    /**
     * 返回语义分析器
     *
     * @return
     */
    UniversalScriptParser getParser();

    /**
     * 返回命令仓库
     *
     * @return
     */
    UniversalCommandRepository getRepository();

    /**
     * 返回已读取的行号，从 1 开始
     *
     * @return
     */
    long getLineNumber();

    /**
     * 关闭编译器
     */
    void close();

}
