package icu.etl.script.compiler;

import java.io.CharArrayReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;

/**
 * 语法分析器的接口实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptParser implements UniversalScriptParser {

    private UniversalScriptContext context;

    /** 脚本命令工厂 */
    private UniversalCommandRepository repository;

    /** 词法分析器 */
    private ScriptReader reader;

    /** 当前用户会话信息 */
    private UniversalScriptSession session;

    /**
     * 初始化
     *
     * @param session    用户会话信息
     * @param context    脚本引擎上下文信息
     * @param repository 编译器仓库
     * @param in         输入流
     */
    public ScriptParser(UniversalScriptSession session, UniversalScriptContext context, UniversalCommandRepository repository, ScriptReader in) {
        this.session = session;
        this.context = context;
        this.repository = repository;
        this.reader = in;
    }

    public UniversalScriptCommand read() throws IOException, SQLException {
        return this.read(this.reader);
    }

    /**
     * 从输入流中读取一个脚本语句，并编译成脚本命令
     *
     * @param in 输入流
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private UniversalScriptCommand read(ScriptReader in) throws IOException, SQLException {
        String line = in.previewline(); // 读一行字符串
        if (line == null) {
            return null;
        } else {
            UniversalCommandCompiler obj = this.repository.get(line);
            String script = obj.read(in, in); // 从脚本语句中读取一个命令
            return obj.compile(this.session, this.context, this, in, script); // 编译语句
        }
    }

    public List<UniversalScriptCommand> read(String script) throws IOException, SQLException {
        ArrayList<UniversalScriptCommand> list = new ArrayList<UniversalScriptCommand>();
        ScriptReader in = new ScriptReader(new CharArrayReader(script.toCharArray()));
        try {
            UniversalScriptCommand command = null;
            while ((command = this.read(in)) != null) {
                list.add(command);
            }
            return list;
        } finally {
            in.close();
        }
    }

}
