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
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import icu.etl.zip.Compress;

public class UnzipCommand extends AbstractFileCommand implements UniversalScriptInputStream, JumpCommandSupported, NohupCommandSupported {

    /** 文件绝对路径 */
    private String filepath;

    private Compress c;

    public UnzipCommand(UniversalCommandCompiler compiler, String command, String filepath) {
        super(compiler, command);
        this.filepath = filepath;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.filepath = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "unzip", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        File file = new ScriptFile(session, context, this.filepath);
        if (session.isEchoEnable() || forceStdout) {
            stdout.println("unzip " + file.getAbsolutePath());
        }

        this.c = context.getContainer().getBean(Compress.class, "zip");
        try {
            this.c.setFile(file);
            this.c.extract(file.getParent(), Settings.getFileEncoding());

            session.removeValue();
            session.putValue("file", file);

            return this.c.isTerminate() ? UniversalScriptCommand.TERMINATE : 0;
        } finally {
            this.c.close();
        }
    }

    public void terminate() throws Exception {
        if (this.c != null) {
            this.c.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}