package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.ClassUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 执行 JAVA 类命令 <br>
 * 格式: java 类全名 参数1 参数2 <br>
 * java 类需要实现 {@linkplain AbstractJavaCommand} 抽象类
 */
public class JavaCommand extends AbstractTraceCommand implements UniversalScriptInputStream, JumpCommandSupported, NohupCommandSupported {

    /** JAVA 对象 */
    private AbstractJavaCommand obj;

    /** JAVA 类名 */
    private String className;

    /** JAVA 对象的参数 */
    private List<String> args;

    public JavaCommand(UniversalCommandCompiler compiler, String command, String className, List<String> args) {
        super(compiler, command);
        this.className = className;
        this.args = args;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (this.args != null && !this.args.isEmpty()) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "java " + this.className, this.args));
        }
        this.args = analysis.split(StringUtils.trimBlank(IO.read(in, new StringBuilder())));
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        StringBuilder buf = new StringBuilder(this.command.length() + 30);
        buf.append("java ");
        buf.append(this.className);

        UniversalScriptAnalysis analysis = session.getAnalysis();
        String[] array = new String[this.args.size()];
        for (int i = 0; i < this.args.size(); i++) {
            array[i] = analysis.replaceShellVariable(session, context, this.args.get(i), true, true, true, false);

            String value = array[i];
            if (StringUtils.indexOfBlank(value, 0, value.length() - 1) != -1) {
                buf.append(" \"").append(value).append('\"');
            } else {
                buf.append(' ').append(value);
            }
        }

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(buf);
        }

        // 初始化
        Class<? extends AbstractJavaCommand> cls = ClassUtils.forName(this.className, true, context.getContainer().getClassLoader());
        if (cls == null) {
            throw new Exception(ResourcesUtils.getScriptStderrMessage(94, this.command, className, AbstractJavaCommand.class.getName()));
        }
        this.obj = context.getContainer().createBean(cls);

        session.removeValue();
        session.putValue("obj", this.obj);

        // 执行命令
        return this.obj.execute(session, context, stdout, stderr, array);
    }

    public void terminate() throws Exception {
        if (this.obj != null) {
            try {
                this.obj.terminate();
            } catch (Throwable e) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(27, this.command), e);
            }
        }
    }

    public boolean enableNohup() {
        return this.obj == null ? false : (this.obj instanceof NohupCommandSupported) && ((NohupCommandSupported) this.obj).enableNohup();
    }

    public boolean enableJump() {
        return this.obj == null ? false : (this.obj instanceof JumpCommandSupported) && ((JumpCommandSupported) this.obj).enableJump();
    }

}
