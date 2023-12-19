package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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
import icu.etl.script.io.ScriptFile;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CatCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    private String charsetName;

    private String filepath;

    public CatCommand(UniversalCommandCompiler compiler, String command, String charsetName, String filepath) {
        super(compiler, command);
        this.charsetName = charsetName;
        this.filepath = filepath;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.filepath = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "cat", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        ScriptFile file = new ScriptFile(session, context, this.filepath);
        String content = FileUtils.readline(file, StringUtils.defaultString(this.charsetName, context.getCharsetName()), 0);

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(content);
        }

        session.removeValue();
        session.putValue("cat", content);
        return 0;
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
