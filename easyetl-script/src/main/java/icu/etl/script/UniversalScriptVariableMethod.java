package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 脚本引擎变量方法
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptVariableMethod {

    /**
     * 执行变量方法
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param stdout   标准信息输出流
     * @param stderr   错误信息输出流
     * @param analysis 语句分析器
     * @param name     变量名
     * @param method   方法名, 如: .trim()
     * @return 返回0表示成功，返回非0表示错误
     * @throws IOException
     * @throws SQLException
     */
    int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String name, String method) throws IOException, SQLException;

    /**
     * 返回 {@linkplain #execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, UniversalScriptAnalysis, String, String)} 方法的计算结果
     *
     * @return
     */
    Object value();

}
