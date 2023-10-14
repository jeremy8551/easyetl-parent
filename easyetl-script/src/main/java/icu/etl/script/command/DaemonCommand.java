package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.io.ScriptFileExpression;

/**
 * 合并执行一个脚本文件, 脚本文件执行完毕后会同步子脚本文件的局部变量，全局变量，数据库编目信息
 */
public class DaemonCommand extends ExecuteFileCommand {

    public DaemonCommand(UniversalCommandCompiler compiler, String command, String filepath) {
        super(compiler, command, filepath);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext parent, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        return super.execute(session, parent, stdout, stderr, forceStdout, outfile, errfile);
    }

    public int execute(UniversalScriptSession session, UniversalScriptEngine engine, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, ScriptFileExpression file, String charsetName) throws IOException, SQLException {
        try {
            return super.execute(session, engine, context, stdout, stderr, forceStdout, file, charsetName);
        } finally {
            // 将脚本文件产生的变量复制到其父脚本引擎中
            UniversalScriptContext parent = context.getParent();
            if (parent != null) {
                parent.getLocalVariable().addAll(engine.getContext().getLocalVariable());
                parent.getGlobalVariable().addAll(engine.getContext().getGlobalVariable());
            }
        }
    }

    public void terminate() throws IOException, SQLException {
        super.terminate();
    }

    public boolean enableNohup() {
        return super.enableNohup();
    }

}
