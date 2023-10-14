package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.FtpList;
import icu.etl.util.ResourcesUtils;

public class ByeCommand extends AbstractTraceCommand {

    public ByeCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println("bye");
        }

        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        if (ftp == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(35));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            FtpList.get(context).close();
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

}
