package icu.etl.script;

import java.io.Reader;

/**
 * 脚本引擎命令的输入流 <br>
 * 实现 {@link UniversalScriptInputStream} 接口的命令即表示该命令支持从输入流中输入参数 <br>
 * 使用管道命令时，默认使用 {@link UniversalScriptInputStream} 接口获取上一个命令的输入信息
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptInputStream {

    /**
     * 命令从输入流参数 in 中读取信息
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param parser   语法分析器
     * @param analysis 语句分析器
     * @param in       管道输入流
     * @throws Exception 读取数据发生错误
     */
    void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws Exception;

}
