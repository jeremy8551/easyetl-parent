package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandResultSet;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.io.ScriptStdbuf;
import icu.etl.script.session.ScriptMainProcess;

public class PipeCommand extends AbstractCommand implements NohupCommandSupported {

    private List<UniversalScriptCommand> list;

    private UniversalScriptCommand run;

    public PipeCommand(UniversalCommandCompiler compiler, String command, List<UniversalScriptCommand> commands) {
        super(compiler, command);
        this.list = commands;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        int size = this.list.size();
        int last = size - 1;
        UniversalScriptParser parser = session.getCompiler().getParser();

        ScriptMainProcess process = session.getMainProcess();
        ScriptStdbuf cache = new ScriptStdbuf(stdout);
        for (int index = 0; !this.terminate && index < size; index++) {
            UniversalScriptCommand command = this.list.get(index);
            this.run = command;

            if (index > 0 && command instanceof UniversalScriptInputStream) {
                UniversalScriptInputStream pipe = (UniversalScriptInputStream) command;
                pipe.read(session, context, parser, session.getAnalysis(), cache.toReader());
            }

            cache.clear();
            UniversalCommandResultSet result = process.execute(session, context, (index == last ? stdout : cache), stderr, true, command);
            int value = result.getExitcode();
            if (value != 0) {
                return UniversalScriptCommand.COMMAND_ERROR;
            }
        }

        return this.terminate ? UniversalScriptCommand.TERMINATE : 0;
    }

    public void terminate() throws IOException, SQLException {
        this.terminate = true;
        if (this.run != null) {
            this.run.terminate();
        }
    }

    public boolean enableNohup() {
        return true;
    }

}
