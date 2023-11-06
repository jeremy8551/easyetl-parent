package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 语法分析器 <br>
 * 语法分析（英语：syntactic analysis，也叫 parsing）是根据某种给定的形式文法对由单词序列（如英语单词序列）构成的输入文本进行分析并确定其语法结构的一种过程。
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptParser {

    /**
     * 读取下一个语句执行语法分析和语义分析，返回对应的脚本命令
     *
     * @return
     * @throws IOException
     * @throws SQLException
     */
    UniversalScriptCommand read() throws IOException, SQLException;

    /**
     * 对一段语句进行语法分析和语义分析，返回对应的脚本命令集合
     *
     * @param script 一段语句
     * @return
     * @throws IOException
     * @throws SQLException
     */
    List<UniversalScriptCommand> read(String script) throws IOException, SQLException;

}