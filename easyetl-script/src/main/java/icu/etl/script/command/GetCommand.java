package icu.etl.script.command;

import java.io.File;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.FtpList;
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 从远程服务器上下载文件或目录 <br>
 * get remotefile localfile
 */
public class GetCommand extends AbstractFileCommand implements NohupCommandSupported {

    private OSFtpCommand ftp;

    private String localfile;

    private String remotefile;

    public GetCommand(UniversalCommandCompiler compiler, String command, String localfile, String remotefile) {
        super(compiler, command);
        this.localfile = localfile;
        this.remotefile = remotefile;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String localfilepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.localfile, true, true, true, false), true);
        String remotefilepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.remotefile, true, true, true, false), false);

        if (session.isEchoEnable() || forceStdout) {
            if (StringUtils.isBlank(localfilepath)) {
                stdout.println("get " + remotefilepath);
            } else {
                stdout.println("get " + remotefilepath + " " + localfilepath);
            }
        }

        try {
            this.ftp = FtpList.get(context).getFTPClient();
            if (this.ftp == null) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr035"));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            if (analysis.isBlankline(localfilepath)) {
                localfilepath = session.getDirectory();
            }

            File file = new ScriptFile(session, context, localfilepath);
            if (!file.exists()) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr037", localfilepath));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            this.ftp.download(remotefilepath, new ScriptFile(session, context, localfilepath));
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

}
