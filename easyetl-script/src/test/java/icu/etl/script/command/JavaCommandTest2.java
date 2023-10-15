package icu.etl.script.command;

import icu.etl.printer.Printer;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

public class JavaCommandTest2 extends AbstractJavaCommand {

    public JavaCommandTest2() {
        super();
    }

    public void echoUsage(Printer out) {
    }

    @Override
    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String[] args) throws Exception {
        throw new Exception("测试脚本引擎处理异常错误!");
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
