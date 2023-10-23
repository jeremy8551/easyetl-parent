package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.os.OS;
import icu.etl.os.OSCommand;
import icu.etl.printer.OutputStreamPrinter;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.SSHClientMap;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 执行操作系统命令 <br>
 * os ps -ef
 */
public class ExecuteOSCommand extends AbstractTraceCommand implements UniversalScriptInputStream, JumpCommandSupported, NohupCommandSupported {

    /** 操作系统命令 */
    private String oscommand;

    /** 命令执行的终端 */
    private OSCommand terminal;

    public ExecuteOSCommand(UniversalCommandCompiler compiler, String command, String oscommand) {
        super(compiler, command);
        this.oscommand = oscommand;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.oscommand)) {
            this.oscommand = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "os", this.oscommand));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        this.terminal = SSHClientMap.get(context).last(); // 优先使用 declare ssh2 client 语句定义的客户端
        if (this.terminal == null) {
            OS os = context.getFactory().getContext().get(OS.class);
            this.terminal = os.getOSCommand();
        }

        UniversalScriptAnalysis analysis = session.getAnalysis();
        String command = analysis.replaceShellVariable(session, context, this.oscommand, true, true, false, true);
        String charsetName = StringUtils.defaultString(this.terminal.getCharsetName(), context.getCharsetName());
        return this.terminal.execute(command, 0, new OutputStreamPrinter(stdout, charsetName), new OutputStreamPrinter(stderr, charsetName));
    }

    public void terminate() throws IOException, SQLException {
        if (this.terminal != null) {
            this.terminal.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
