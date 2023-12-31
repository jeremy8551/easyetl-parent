package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import icu.etl.expression.MillisExpression;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.session.ScriptProcess;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.TimeWatch;

/**
 * 等待脚本命令执行完毕 <br>
 * <p>
 * wait pid=1 3min|3sec|3hou|3day
 */
public class WaitCommand extends AbstractTraceCommand {

    /** nohup 命令生成的编号 */
    private String id;

    /** 脚本命令执行的超时时间，单位: 秒 */
    private String timeout;

    /** 后台线程 */
    private ScriptProcess process;

    public WaitCommand(UniversalCommandCompiler compiler, String command, String id, String timeout) {
        super(compiler, command);
        this.id = id;
        this.timeout = timeout;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        long compileMillis = session.getCompiler().getCompileMillis();
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String pid = analysis.replaceVariable(session, context, this.id, false);
        String timeoutExpression = analysis.replaceVariable(session, context, this.timeout, false);
        boolean print = session.isEchoEnable() || forceStdout;

        ScriptProcess process = session.getSubProcess().get(pid);
        if (process == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(44, pid));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            this.process = process;
        }

        // 计算等待命令的超时时间，单位秒
        UniversalScriptCommand command = process.getCommand();
        if (command == null) {
            throw new NullPointerException();
        }

        String script = command.getScript();
        TimeWatch watch = new TimeWatch();
        long timeout = new MillisExpression(timeoutExpression).value();
        boolean usetimeout = timeout > 0;
        if (usetimeout && print) {
            stdout.println(ResourcesUtils.getScriptStdoutMessage(36, script, Dates.format(timeout, TimeUnit.MILLISECONDS, true)));
        } else {
            stdout.println(ResourcesUtils.getScriptStdoutMessage(37, script));
        }

        boolean terminate = false;
        while (process.isAlive()) {
            if (this.terminate || session.isTerminate() || (usetimeout && ((System.currentTimeMillis() - compileMillis) > timeout) && !terminate)) {
                try {
                    process.terminate();
                    continue;
                } catch (Throwable e) {
                    stderr.println(ResourcesUtils.getScriptStderrMessage(27), e);
                } finally {
                    terminate = true;
                    stderr.println(ResourcesUtils.getScriptStderrMessage(28, script));
                }
            }

            // 等待后台线程运行完毕
            long wait = timeout - (System.currentTimeMillis() - compileMillis);
            if (wait > 0) {
                process.getEnvironment().getWaitDone().sleep(wait);
            }

            // 命令被终止 或 用户会话被终止时，直接退出
            if (this.terminate || session.isTerminate()) {
                break;
            }
        }

        if (this.terminate) {
            return UniversalScriptCommand.TERMINATE;
        } else if (process.getExitcode() == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(9, script));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            Integer exitcode = process.getExitcode();
            if (print) {
                stdout.println(ResourcesUtils.getScriptStdoutMessage(39, script, exitcode));
            }
            return exitcode;
        }
    }

    public void terminate() throws Exception {
        this.terminate = true;
        if (this.process != null) {
            this.process.terminate();
            this.process.getEnvironment().getWaitDone().wakeup();
        }
    }

}
