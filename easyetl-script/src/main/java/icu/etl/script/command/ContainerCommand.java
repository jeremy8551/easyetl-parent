package icu.etl.script.command;

import java.util.List;
import java.util.Map;

import icu.etl.concurrent.EasyJobService;
import icu.etl.concurrent.ThreadSource;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.WithBodyCommandSupported;
import icu.etl.script.internal.ContainerCommandReader;
import icu.etl.util.StringUtils;

/**
 * container to execute tasks in parallel using thread=2 dropIndex buildIndex freq=day batch=10000 rollback begin ... end
 *
 * @author jeremy8551@qq.com
 */
public class ContainerCommand extends AbstractCommand implements WithBodyCommandSupported {

    /** 参数集合 */
    private Map<String, String> attributes;

    /** 代码块 */
    private List<UniversalScriptCommand> cmdlist;

    /** 运行容器 */
    private EasyJobService service;

    public ContainerCommand(UniversalCommandCompiler compiler, String command, Map<String, String> attributes, List<UniversalScriptCommand> cmdlist) {
        super(compiler, command);
        this.attributes = attributes;
        this.cmdlist = cmdlist;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String thread = analysis.replaceShellVariable(session, context, this.attributes.get("thread"), true, true, true, false);
        int number = StringUtils.parseInt(thread, 2);

        boolean print = session.isEchoEnable() || forceStdout;
        if (print) {
            stdout.println(analysis.replaceShellVariable(session, context, this.command, true, false, true, true));
        }

        ContainerCommandReader in = new ContainerCommandReader(session, context, stdout, stderr, this.cmdlist);
        try {
            this.service = context.getContainer().getBean(ThreadSource.class).getJobService(number);
            this.service.execute(in);
            return 0;
        } finally {
            in.close();
        }
    }

    public void terminate() throws Exception {
        if (this.service != null) {
            this.service.terminate();
        }
    }

}
