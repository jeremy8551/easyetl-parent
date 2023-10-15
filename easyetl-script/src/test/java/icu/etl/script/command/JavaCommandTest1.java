package icu.etl.script.command;

import icu.etl.printer.Printer;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

public class JavaCommandTest1 extends AbstractJavaCommand {

    public JavaCommandTest1() {
        super();
    }

    @Override
    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String[] args) throws Exception {
        context.getEngine().eval("step java类中的嵌套语句!");

        return 0;
    }

    public void echoUsage(Printer out) {

    }

    @Override
    public boolean enableJump() {
        return false;
    }

    @Override
    public boolean enableNohup() {
        return true;
    }

    @Override
    public boolean enablePipe() {
        return false;
    }
}
