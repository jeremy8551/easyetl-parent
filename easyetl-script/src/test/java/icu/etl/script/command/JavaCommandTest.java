package icu.etl.script.command;

import icu.etl.printer.Printer;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

public class JavaCommandTest extends AbstractJavaCommand {

    public JavaCommandTest() {
        super();
    }

    public void echoUsage(Printer out) {
    }

    @Override
    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String[] args) throws Exception {
        int timeout = StringUtils.parseInt(args[0], 120);

        context.addGlobalVariable("JavaCommandTest", "JavaCommandTest110");
        TimeWatch watch = new TimeWatch();

        System.out.println(StringUtils.toString(args));
        System.out.println("等待 " + timeout + " 秒!");
        while (!this.terminate) {
            if (this.terminate || watch.useSeconds() >= timeout) {
                break;
            }

        }

        this.terminate = false;
        return 0;
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
