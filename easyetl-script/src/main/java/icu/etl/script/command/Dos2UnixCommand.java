package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.FileUtils;

public class Dos2UnixCommand extends AbstractTraceCommand implements JumpCommandSupported, NohupCommandSupported {

    private String value;

    public Dos2UnixCommand(UniversalCommandCompiler compiler, String command, String value) {
        super(compiler, command);
        this.value = value;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(analysis.replaceShellVariable(session, context, this.command, true, true, true, false));
        }

        String str = analysis.replaceShellVariable(session, context, this.value, true, true, true, false);
        if (FileUtils.isFile(str)) {
            FileUtils.dos2unix(new File(str), context.getCharsetName());
        } else {
            FileUtils.dos2unix(str);
        }
        return 0;
    }

    public void terminate() throws IOException, SQLException {

    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
