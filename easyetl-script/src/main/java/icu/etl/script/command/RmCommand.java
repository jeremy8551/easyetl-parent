package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.FtpList;
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 删除本地文件或目录 或 删除远程服务器上的文件或目录 <br>
 * <br>
 * rm filepath
 */
public class RmCommand extends AbstractFileCommand implements UniversalScriptInputStream, JumpCommandSupported, NohupCommandSupported {

    /** 文件绝对路径 */
    private String filepath;

    /** true表示本地 false表示远程服务器 */
    private boolean localhost;

    /** true表示布尔值取反 */
    private boolean reverse;

    public RmCommand(UniversalCommandCompiler compiler, String command, String filepath, boolean localhost, boolean reverse) {
        super(compiler, command);
        this.filepath = filepath;
        this.localhost = localhost;
        this.reverse = reverse;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (!analysis.isBlankline(this.filepath)) {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "rm", this.filepath));
        }

        this.filepath = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        boolean print = session.isEchoEnable() || forceStdout;
        if (this.localhost || ftp == null) {
            ScriptFile file = new ScriptFile(session, context, this.filepath);

            if (this.reverse) {
                if (print) {
                    stdout.println("!rm " + file.getAbsolutePath());
                }
                return FileUtils.delete(file) ? UniversalScriptCommand.COMMAND_ERROR : 0;
            } else {
                if (print) {
                    stdout.println("rm " + file.getAbsolutePath());
                }
                return FileUtils.delete(file) ? 0 : UniversalScriptCommand.COMMAND_ERROR;
            }
        } else {
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String filepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.filepath, true, true, true, false), false);
            if (this.reverse) {
                if (print) {
                    stdout.println("!rm " + filepath);
                }
                return ftp.rm(filepath) ? UniversalScriptCommand.COMMAND_ERROR : 0;
            } else {
                if (print) {
                    stdout.println("rm " + filepath);
                }
                return ftp.rm(filepath) ? 0 : UniversalScriptCommand.COMMAND_ERROR;
            }
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
