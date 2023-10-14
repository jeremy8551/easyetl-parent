package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.StringUtils;

/**
 * 生成一个32位的唯一字符串
 */
public class UUIDCommand extends AbstractTraceCommand implements NohupCommandSupported {

    public UUIDCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(StringUtils.toRandomUUID());
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
