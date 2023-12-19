package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
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
import icu.etl.script.internal.EasyJobReaderImpl;
import icu.etl.util.StringUtils;

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
    private EasyJobService container;

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

        EasyJobReaderImpl in = new EasyJobReaderImpl(session, context, stdout, stderr, this);
        try {
            this.container = context.getContainer().getBean(ThreadSource.class).getJobService(number);
            this.container.execute(in);
            return 0;
        } finally {
            in.close();
        }
    }

    public void terminate() throws Exception {
        if (this.container != null) {
            this.container.terminate();
        }
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
