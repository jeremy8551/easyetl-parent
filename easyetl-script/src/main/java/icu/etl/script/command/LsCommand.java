package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.os.OSFile;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.os.linux.Linuxs;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
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
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharTable;
import icu.etl.util.CollUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 显示本地文件信息<br>
 * 显示远程文件信息
 */
public class LsCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 文件绝对路径 */
    private List<String> filepathList;

    /** true表示本地 false表示远程服务器 */
    private boolean localhost;

    public LsCommand(UniversalCommandCompiler compiler, String command, List<String> filepathList, boolean localhost) {
        super(compiler, command);
        this.filepathList = filepathList;
        this.localhost = localhost;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (CollUtils.isEmpty(this.filepathList)) {
            String expression = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
            this.filepathList = analysis.split(expression);
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "ls", this.filepathList));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        List<String> filepaths = new ArrayList<String>(this.filepathList.size());
        for (String filepath : this.filepathList) {
            filepaths.add(analysis.replaceShellVariable(session, context, filepath, true, true, true, false));
        }

        StringBuilder cb = new StringBuilder("ls " + StringUtils.join(filepaths, " ")).append(FileUtils.lineSeparator);

        CharTable table = new CharTable();
        table.addTitle("filename");

        OSFtpCommand ftp = FtpList.get(context).getFTPClient();
        if (this.localhost || ftp == null) {
            if (filepaths.isEmpty()) {
                filepaths = ArrayUtils.asList(session.getDirectory());
            }

            for (String filepath : filepaths) {
                File file = new ScriptFile(session, context, filepath);
                if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    continue;
                }

                if (file.isDirectory()) {
                    File[] files = FileUtils.array(file.listFiles());
                    for (File f : files) {
                        if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                            continue;
                        }

                        table.addValue(Linuxs.toLongname(f));
                    }
                } else {
                    table.addValue(Linuxs.toLongname(file));
                }
            }
        } else {
            if (filepaths.isEmpty()) {
                filepaths = ArrayUtils.asList(ftp.pwd());
            }

            for (String path : filepaths) {
                List<OSFile> list = ftp.ls(FileUtils.replaceFolderSeparator(path, false));
                for (OSFile file : list) {
                    table.addValue(file.getLongname());
                }
            }
        }

        cb.append(table.removeLeftBlank().toSimpleShape());

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(cb);
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}