package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.session.ScriptProcess;
import icu.etl.script.session.ScriptProcessEnvironment;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
import icu.etl.util.TimeWatch;

/**
 * 后台运行脚本命令 <br>
 * {@literal nohup command &}
 */
public class NohupCommand extends AbstractCommand {

    /** 后台运行的命令 */
    private UniversalScriptCommand subcommand;

    /** 后台线程的运行环境 */
    private ScriptProcessEnvironment environment;

    public NohupCommand(UniversalCommandCompiler compiler, String command, UniversalScriptCommand subcommand) {
        super(compiler, command);
        this.subcommand = subcommand;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        File logfile = new File(session.getDirectory(), "nohup.out");
        if (!logfile.canWrite()) {
            logfile = new File(Settings.getUserHome(), logfile.getName());
        }

        // 创建子线程
        ScriptProcessEnvironment environment = new ScriptProcessEnvironment(session, context, stdout, stderr, forceStdout, this.subcommand, logfile);
        ScriptProcess process = session.getSubProcess().create(environment);
        process.start();
        this.environment = environment;
        boolean print = session.isEchoEnable() || forceStdout;

        // 等待后台线程启动，防止启动超时
        int timeout = 20 * 60 * 1000; // 超时时间：20分钟
        TimeWatch watch = new TimeWatch();
        boolean hasPrint = false;
        while (!this.terminate && process.waitFor()) { // 等待线程启动运行后退出
            environment.getWaitRun().sleep(timeout);
            if (this.terminate) {
                break;
            }

            if (!hasPrint) {
                if (print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(24, this.subcommand));
                }
                hasPrint = true;
            }

            // 判断是否大于超时时间
            if (watch.useMillis() > timeout) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(25, this.subcommand.getScript()));
                return UniversalScriptCommand.COMMAND_ERROR;
            }
        }

        if (this.terminate) {
            return UniversalScriptCommand.TERMINATE;
        } else {
            session.addVariable(UniversalScriptVariable.VARNAME_PID, process.getPid()); // 保存进程编号
            if (print) {
                stdout.println("appending output to " + logfile.getAbsolutePath() + FileUtils.lineSeparator + process.getPid());
            }
            return 0;
        }
    }

    public void terminate() throws IOException, SQLException {
        this.terminate = true;
        if (this.environment != null) {
            this.environment.getWaitRun().wakeup();
        }
    }

}
