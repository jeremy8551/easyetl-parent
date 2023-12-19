package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.FtpList;
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 上传本地文件或目录到远程服务器 <br>
 * put localfile remotefile
 */
public class PutCommand extends AbstractFileCommand implements JumpCommandSupported, NohupCommandSupported {

    private OSFtpCommand ftp;

    /** 本地文件绝对路径 */
    private String localfile;

    /** 上传远程服务器的文件或目录 */
    private String remotefile;

    public PutCommand(UniversalCommandCompiler compiler, String command, String localfile, String remotefile) {
        super(compiler, command);
        this.localfile = localfile;
        this.remotefile = remotefile;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String localfilepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.localfile, true, true, true, false), true);
        String remotedir = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.remotefile, true, true, true, false), false);

        if (session.isEchoEnable() || forceStdout) {
            if (StringUtils.isBlank(remotedir)) {
                stdout.println("put " + localfilepath);
            } else {
                stdout.println("put " + localfilepath + " " + remotedir);
            }
        }

        try {
            this.ftp = FtpList.get(context).getFTPClient();
            if (this.ftp == null) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(35));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            File localfile = new ScriptFile(session, context, localfilepath);
            if (!localfile.exists()) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(36, localfilepath));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            if (analysis.isBlankline(remotedir)) {
                remotedir = this.ftp.pwd();
            }

            this.ftp.upload(localfile, remotedir);
            return 0;
        } finally {
            this.ftp = null;
        }
    }

    public void terminate() throws Exception {
        if (this.ftp != null) {
            this.ftp.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
