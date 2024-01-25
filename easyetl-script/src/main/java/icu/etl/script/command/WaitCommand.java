package icu.etl.script.command;

import java.io.File;
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
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

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
            stderr.println(ResourcesUtils.getMessage("script.message.stderr044", pid));
            return UniversalScriptCommand.COMMAND_ERROR;
        }
        this.process = process;

        // 计算等待命令的超时时间，单位秒
        UniversalScriptCommand command = Ensure.notNull(process.getCommand());
        String script = command.getScript();
        long timeout = new MillisExpression(timeoutExpression).value();
        boolean usetimeout = timeout > 0;
        if (usetimeout && print) {
            stdout.println(ResourcesUtils.getMessage("script.message.stdout036", script, Dates.format(timeout, TimeUnit.MILLISECONDS, true)));
        } else {
            stdout.println(ResourcesUtils.getMessage("script.message.stdout037", script));
        }

        boolean terminate = false;
        while (process.isAlive()) {
            if (this.terminate || session.isTerminate() || (usetimeout && ((System.currentTimeMillis() - compileMillis) > timeout) && !terminate)) {
                try {
                    process.terminate();
                    continue;
                } catch (Throwable e) {
                    stderr.println(ResourcesUtils.getMessage("script.message.stderr027"), e);
                } finally {
                    terminate = true;
                    stderr.println(ResourcesUtils.getMessage("script.message.stderr028", script));
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
            stderr.println(ResourcesUtils.getMessage("script.message.stderr009", script));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            Integer exitcode = process.getExitcode();
            if (print) {
                stdout.println(ResourcesUtils.getMessage("script.message.stdout039", script, exitcode));
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
