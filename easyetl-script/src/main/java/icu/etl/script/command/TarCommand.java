package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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

    /** 压缩接口 */
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
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "tar", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (this.compress) {
            return this.compressFile(session, context, stdout, stderr, forceStdout);
        } else {
            return this.decompressFile(session, context, stdout, stderr, forceStdout);
        }
    }

    public void terminate() throws Exception {
        if (this.c != null) {
            this.c.terminate();
        }
    }

    /**
     * 压缩文件
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @return 返回0表示正确, 返回非0表示不正确
     * @throws IOException 执行命令发生错误
     */
    public int compressFile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException {
        File file = new ScriptFile(session, context, this.filepath);
        File tarfile = new File(file.getParentFile(), FileUtils.changeFilenameExt(file.getName(), "tar"));

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("tar -zcvf " + file.getAbsolutePath());
        }

        this.c = context.getContainer().getBean(Compress.class, "tar");
        try {
            this.c.setFile(tarfile);
            this.c.archiveFile(file, null);

            session.removeValue();
            session.putValue("file", tarfile);

            return this.c.isTerminate() ? UniversalScriptCommand.TERMINATE : 0;
        } finally {
            this.c.close();
        }
    }

    /**
     * 解压文件
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @return 返回0表示正确, 返回非0表示不正确
     * @throws IOException 执行命令发生错误
     */
    public int decompressFile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException {
        File tarfile = new ScriptFile(session, context, this.filepath);

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("tar -xvf " + tarfile.getAbsolutePath());
        }

        this.c = context.getContainer().getBean(Compress.class, "tar");
        try {
            this.c.setFile(tarfile);
            this.c.extract(tarfile.getParent(), Settings.getFileEncoding());

            session.removeValue();
            session.putValue("file", tarfile);

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
