package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import icu.etl.expression.DataUnitExpression;
import icu.etl.os.OSFile;
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
import icu.etl.util.CollectionUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 用于测试本地文件或远程文件的大小，或测量字符串的长度 <br>
 * <br>
 * length string; <br>
 * length -h string; <br>
 * length -b string; <br>
 * length -c string; <br>
 * length -f filepath; <br>
 * length -r remotefilepath; <br>
 */
public class LengthCommand extends AbstractTraceCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 操作类型 */
    private char type;

    /** 字符串或文件路径 */
    private String parameter;

    public LengthCommand(UniversalCommandCompiler compiler, String command, char type, String parameter) {
        super(compiler, command);
        this.type = type;
        this.parameter = parameter;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.parameter)) {
            this.parameter = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "length", this.parameter));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        boolean print = session.isEchoEnable() || forceStdout;
        UniversalScriptAnalysis analysis = session.getAnalysis();
        if (this.type == 'r') { // remote file path
            OSFtpCommand ftp = FtpList.get(context).getFTPClient();
            if (ftp == null) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(35));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            String filepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.parameter, true, true, true, false), false);
            if (ftp.isDirectory(filepath)) {
                String parent = FileUtils.getParent(filepath);
                String filename = FileUtils.getFilename(filepath);
                List<OSFile> list = ftp.ls(parent);
                for (OSFile file : list) {
                    if (file.getName().equals(filename)) {
                        if (print) {
                            stdout.println(file.length());
                        }
                        return 0;
                    }
                }

                stderr.println(ResourcesUtils.getScriptStderrMessage(50, filepath));
                return UniversalScriptCommand.COMMAND_ERROR;
            } else if (ftp.isFile(filepath)) { // expression is remote file path
                OSFile file = CollectionUtils.firstElement(ftp.ls(filepath));
                if (file == null) {
                    throw new NullPointerException();
                }
                if (print) {
                    stdout.println(file.length());
                }
                return 0;
            } else {
                stderr.println(ResourcesUtils.getScriptStderrMessage(50, filepath));
                return UniversalScriptCommand.COMMAND_ERROR;
            }
        } else if (this.type == 'f') { // local file path
            File file = new ScriptFile(session, context, this.parameter);
            if (print) {
                stdout.println(file.length());
            }
            return 0;
        } else if (this.type == 'c') { // character length
            String str = analysis.replaceShellVariable(session, context, this.parameter, true, true, true, false);
            if (print) {
                stdout.println(str.length());
            }
            return 0;
        } else if (this.type == 'b') { // string's bytes
            String str = analysis.replaceShellVariable(session, context, this.parameter, true, true, true, false);
            if (print) {
                stdout.println(StringUtils.toBytes(str, context.getCharsetName()).length);
            }
            return 0;
        } else if (this.type == 'h') { // Humanize view
            String str = analysis.replaceShellVariable(session, context, this.parameter, true, true, true, false);
            if (print) {
                stdout.println(DataUnitExpression.toString(BigDecimal.valueOf(StringUtils.toBytes(str, context.getCharsetName()).length)));
            }
            return 0;
        } else {
            stderr.println(this.command);
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
