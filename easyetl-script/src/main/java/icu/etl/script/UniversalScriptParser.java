package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 语法分析器
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptParser {

    /**
     * 读取下一个语句执行语法分析和语义分析，返回对应的脚本命令
     *
     * @return 脚本命令
     * @throws IOException  IO错误
     * @throws SQLException 数据库错误
     */
    UniversalScriptCommand read() throws IOException, SQLException;

    /**
     * 对一段语句进行语法分析和语义分析，返回对应的脚本命令集合
     *
     * @param script 一段语句
     * @return 脚本命令集合
     * @throws IOException  IO错误
     * @throws SQLException 数据库错误
     */
    List<UniversalScriptCommand> read(String script) throws IOException, SQLException;

}