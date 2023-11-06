package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import icu.etl.concurrent.ExecutorContainer;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.ScriptContainerReader;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * container to execute tasks in parallel using thread=3 dropIndex buildIndex freq=day batch=10000 rollback begin ... end
 *
 * @author jeremy8551@qq.com
 */
public class ContainerCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** 参数集合 */
    private Map<String, String> attributes;

    /** 代码块 */
    private List<UniversalScriptCommand> cmdlist;

    /** 运行容器 */
    private ExecutorContainer container;

    public ContainerCommand(UniversalCommandCompiler compiler, String command, Map<String, String> attributes, List<UniversalScriptCommand> cmdlist) {
        super(compiler, command);
        this.attributes = attributes;
        this.cmdlist = cmdlist;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        TimeWatch watch = new TimeWatch();
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String thread = analysis.replaceShellVariable(session, context, this.attributes.get("thread"), true, true, true, false);
        int number = StringUtils.parseInt(thread, 3);

        boolean print = session.isEchoEnable() || forceStdout;
        if (print) {
            stdout.println(analysis.replaceShellVariable(session, context, this.command, true, false, true, true));
        }

        ScriptContainerReader in = new ScriptContainerReader(session, context, stdout, stderr, this);
        try {
            this.container = new ExecutorContainer(in);
            if (this.container.execute(number) == 0) {
                if (print) {
                    stdout.println(ResourcesUtils.getScriptStdoutMessage(47, this.container.getStartNumber(), watch.useTime()));
                }
                return 0;
            } else {
                stderr.println(ResourcesUtils.getScriptStderrMessage(105, this.container.getStartNumber(), this.container.getErrorNumber(), watch.useTime()));
                return UniversalScriptCommand.COMMAND_ERROR;
            }
        } catch (Exception e) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(105, this.container.getStartNumber(), this.container.getErrorNumber(), watch.useTime()), e);
            return UniversalScriptCommand.COMMAND_ERROR;
        } finally {
            in.close();
        }
    }

    public void terminate() throws IOException, SQLException {
        if (this.container != null) {
            this.container.terminate();
        }
    }

    /**
     * 返回容器的所有参数
     *
     * @return
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * 返回容器运行的代码块
     *
     * @return
     */
    public List<UniversalScriptCommand> getList() {
        return this.cmdlist;
    }

}
