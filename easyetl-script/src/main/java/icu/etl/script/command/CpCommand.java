package icu.etl.script.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

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
import icu.etl.script.io.ScriptFileExpression;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CpCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    private String srcFileExpression;

    private String dstFileExpression;

    public CpCommand(UniversalCommandCompiler compiler, String command, String srcFileExpression, String dstFileExpression) {
        super(compiler, command);
        this.srcFileExpression = srcFileExpression;
        this.dstFileExpression = dstFileExpression;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.srcFileExpression)) {
            this.srcFileExpression = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "cat", this.dstFileExpression));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        ScriptFileExpression srcfile = new ScriptFileExpression(session, context, this.srcFileExpression);
        String dstfilepath = session.getAnalysis().replaceShellVariable(session, context, this.dstFileExpression, true, true, true, false);

        if (session.isEchoEnable() || forceStdout) {
            stdout.println("cp " + srcfile.getAbsolutePath() + " " + dstfilepath);
        }

        File file = null;
        File dstfile = new File(dstfilepath);
        if (dstfile.exists()) {
            if (dstfile.isDirectory()) {
                file = new File(dstfile, srcfile.getName());
            } else {
                file = dstfile;
            }
        } else {
            FileUtils.createDirectory(dstfile.getParentFile());
            file = dstfile;
        }

        InputStream in = srcfile.getInputStream();
        FileOutputStream out = new FileOutputStream(file, false);
        try {
            IO.write(in, out);
            return 0;
        } finally {
            IO.close(in, out);
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
