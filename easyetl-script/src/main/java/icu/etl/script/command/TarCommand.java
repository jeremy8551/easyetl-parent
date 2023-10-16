package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.ioc.BeanFactory;
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
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import icu.etl.zip.Compress;

public class TarCommand extends AbstractFileCommand implements UniversalScriptInputStream, JumpCommandSupported, NohupCommandSupported {

    /** 文件绝对路径 */
    private String filepath;

    /** true 表示压缩文件, false 表示解压文件 */
    private boolean compress;

    private Compress c;

    public TarCommand(UniversalCommandCompiler compiler, String command, String filepath, boolean compress) {
        super(compiler, command);
        this.filepath = filepath;
        this.compress = compress;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.filepath = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "tar", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (this.compress) {
            return this.compressFile(session, context, stdout, stderr, forceStdout);
        } else {
            return this.decompressFile(session, context, stdout, stderr, forceStdout);
        }
    }

    public void terminate() throws IOException, SQLException {
        if (this.c != null) {
            this.c.terminate();
        }
    }

    /**
     * 压缩文件
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @return
     * @throws IOException
     */
    public int compressFile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException {
        File file = new ScriptFile(session, context, this.filepath);
        File tarfile = new File(file.getParentFile(), FileUtils.changeFilenameExt(file.getName(), "tar"));

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("tar -zcvf " + file.getAbsolutePath());
        }

        this.c = BeanFactory.get(Compress.class, "tar");
        try {
            this.c.setFile(tarfile);
            this.c.archiveFile(file, null);
            return this.c.isTerminate() ? UniversalScriptCommand.TERMINATE : 0;
        } finally {
            this.c.close();
        }
    }

    /**
     * 解压文件
     *
     * @param session
     * @param context
     * @param stdout
     * @param stderr
     * @param forceStdout
     * @return
     * @throws IOException
     */
    public int decompressFile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException {
        File file = new ScriptFile(session, context, this.filepath);

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("tar -xvf " + file.getAbsolutePath());
        }

        this.c = BeanFactory.get(Compress.class, "tar");
        try {
            this.c.setFile(file);
            this.c.extract(file.getParent(), Settings.getFileEncoding());
            return 0;
        } finally {
            this.c.close();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}