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
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.FtpList;
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CdCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    private String filepath;

    private boolean localhost;

    public CdCommand(UniversalCommandCompiler compiler, String command, String filepath, boolean localhost) {
        super(compiler, command);
        this.filepath = filepath;
        this.localhost = localhost;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.filepath = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "cd", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        boolean print = session.isEchoEnable() || forceStdout;
        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        if (this.localhost || ftp == null) {
            ScriptFile file = new ScriptFile(session, context, this.filepath);
            if (print) {
                stdout.println("cd " + file.getAbsolutePath());
            }
            session.setDirectory(file);
            session.removeValue();
            session.putValue("cd", file);
            return 0;
        } else {
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String filepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.filepath, true, true, true, false), false);
            if (print) {
                stdout.println("cd " + filepath);
            }
            return ftp.cd(filepath) ? 0 : UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
