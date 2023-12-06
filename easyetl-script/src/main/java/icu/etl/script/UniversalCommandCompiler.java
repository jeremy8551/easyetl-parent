package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 脚本命令编译器
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalCommandCompiler {

    /**
     * 判断脚本命令编译器是否能编译脚本语句。
     *
     * @param name   命令的前缀
     * @param script 脚本语句
     * @return 0 表示可以编译脚本语句（且编译器继续向下尝试匹配其他命令）<br>
     * 1 表示可以编译脚本语句（且编译器停止向下匹配其他命令）<br>
     * 2 表示不能编译脚本语句（编译器会继续尝试使用其他命令编译器编译脚本语句）<br>
     * 3 表示不能编译脚本语句（脚本语句会直接作为脚本引擎的默认命令执行）
     */
    int match(String name, String script);

    /**
     * 从输入流参数 in 中读取一个完整的命令文本（如果命令文本跨越多行则可以使用词法分析器向下读取多行内容合并成一个完整语句）<br>
     * 超出命令文本的内容需要使用 {@linkplain UniversalScriptReader#setNextline(String)} 方法重新保存到输入流中，以便下一个命令编译器读取。<br>
     * <br>
     * 例如下面代码是二个命令语句都在同一行的情况，命令编译器在读取命令文本时只能读取第一个分号之前的内容，第一个分号之后的内容需要重新保存到输入流中。<br>
     * <code>
     * .. <br>
     * set varname="name"; set testname="test";<br>
     * .. <br>
     * </code>
     *
     * @param in       脚本语句输入流
     * @param analysis 脚本语句分析器
     * @return 命令的完整语句
     * @throws IOException
     */
    String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException;

    /**
     * 将脚本语句编译成 {@linkplain UniversalScriptCommand} 对象 <br>
     * <br>
     * 语义分析是审查源程序有无语义错误，为代码生成阶段收集类型信息。比如语义分析的一个工作是进行类型审查，审查每个算符是否具有语言规范允许的运算对象，当不符合语言规范时，编译程序应报告错误。
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param parser   语法分析器
     * @param analysis 语句分析器
     * @param command  脚本语句
     * @return 返回编译之后的脚本实例
     * @throws IOException
     * @throws SQLException
     */
    UniversalScriptCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String command) throws IOException, SQLException;

    /**
     * 通过输出流接口 out 输出命令的使用说明
     *
     * @param context 脚本引擎上下文信息
     * @param out     脚本引擎的标准信息输出接口
     */
    void usage(UniversalScriptContext context, UniversalScriptStdout out);

}
