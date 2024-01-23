package icu.etl.script.command;

import java.io.File;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.io.ScriptStderr;
import icu.etl.script.io.ScriptStdout;
import icu.etl.script.io.ScriptWriterFactory;

/**
 * 带日志输出的脚本引擎命令模版类 <br>
 * 命令中需要指定日志输出时，可以在 {@linkplain AbstractTraceCommand} 类基础上实现功能 <br>
 * {@linkplain AbstractTraceCommand} 类提供了对命令中 {@literal >>} logfile 表达式的解析 <br>
 * {@linkplain AbstractTraceCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean, File, File)} 接口中 stdout 与 stderr 分别表示标准输出与错误信息输出 <br>
 * 如果命令中指定了输出日志则 stdout 表示向日志文件写入的标准信息输出流 <br>
 * 如果命令中指定了输出日志则 stderr 表示向日志文件写入的错误信息输出流 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-10-14
 */
public abstract class AbstractTraceCommand extends AbstractCommand {

    /** 标准信息输出流 */
    protected ScriptWriterFactory stdout;

    /** 错误信息输出流 */
    protected ScriptWriterFactory stderr;

    /** 等于true表示标注输出与错误输出流是同一个对象 */
    protected volatile boolean same;

    /**
     * 初始化操作
     *
     * @param compiler 脚本命令编译器
     * @param script   文本命令
     */
    public AbstractTraceCommand(UniversalCommandCompiler compiler, String script) {
        super(compiler, script);
    }

    /**
     * 设置标准信息输出接口
     *
     * @param stdout 标准信息输出接口
     * @param stderr 错误信息输出接口
     * @param same   等于true表示标注输出与错误输出流是同一个对象
     */
    public void setPrinter(ScriptWriterFactory stdout, ScriptWriterFactory stderr, boolean same) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.same = same;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws Exception {
        boolean autoCloseOut = false;
        boolean autoCloseErr = false;
        try {
            File outfile = null;
            if (this.stdout != null) {
                autoCloseOut = true;
                stdout = new ScriptStdout(this.stdout.build(session, context), stdout.getFormatter());
                outfile = this.stdout.getFile();
            }

            File errfile = null;
            if (this.stderr != null) {
                autoCloseErr = true;
                stderr = new ScriptStderr(this.stderr.build(session, context), stderr.getFormatter());
                errfile = this.stderr.getFile();
            }

            return this.execute(session, context, stdout, stderr, forceStdout, outfile, errfile);
        } finally {
            if (autoCloseOut && this.stdout != null) {
                this.stdout.close();
            }

            if (autoCloseErr && !this.same && this.stderr != null) {
                this.stderr.close();
            }
        }
    }

    /**
     * 执行命令
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param outfile     标准信息的日志文件（如未设置标准信息日志，此时为null）
     * @param errfile     错误信息的日志文件（如未设置错误信息日志，此时为null）
     * @return 返回0表示正确, 返回非0表示不正确
     * @throws Exception 执行命令发生错误
     */
    public abstract int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception;

}
