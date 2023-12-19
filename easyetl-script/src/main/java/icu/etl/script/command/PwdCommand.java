package icu.etl.script.command;

import java.io.File;

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

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        boolean print = session.isEchoEnable() || forceStdout;
        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        if (this.localhost || ftp == null) {
            if (print) {
                String pwd = session.getDirectory();
                stdout.println(pwd);
                session.removeValue();
                session.putValue("pwd", pwd);
            }
            return 0;
        } else {
            if (print) {
                String pwd = ftp.pwd();
                stdout.println(pwd);
                session.removeValue();
                session.putValue("pwd", pwd);
            }
            return 0;
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
