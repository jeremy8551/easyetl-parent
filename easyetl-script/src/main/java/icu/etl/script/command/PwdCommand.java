package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.FtpList;

/**
 * 显示本地目录路径<br>
 * 显示远程目录路径<br>
 */
public class PwdCommand extends AbstractFileCommand implements NohupCommandSupported {

    /** true表示本地 false表示远程服务器 */
    private boolean localhost;

    public PwdCommand(UniversalCommandCompiler compiler, String command, boolean localhost) {
        super(compiler, command);
        this.localhost = localhost;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        boolean print = session.isEchoEnable() || forceStdout;
        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        if (this.localhost || ftp == null) {
            if (print) {
                stdout.println(session.getDirectory());
            }
            return 0;
        } else {
            if (print) {
                stdout.println(ftp.pwd());
            }
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
