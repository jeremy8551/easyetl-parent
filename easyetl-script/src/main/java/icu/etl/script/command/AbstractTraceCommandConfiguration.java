package icu.etl.script.command;

import icu.etl.script.io.ScriptWriterFactory;

public class AbstractTraceCommandConfiguration {

    /** 标准信息输出流 */
    protected ScriptWriterFactory stdout;

    /** 错误信息输出流 */
    protected ScriptWriterFactory stderr;

    /** 等于true表示标注输出与错误输出流是同一个对象 */
    protected boolean same;

    protected String command;

    /**
     * 初始化
     *
     * @param stdout
     * @param stderr
     * @param same
     * @param command
     */
    public AbstractTraceCommandConfiguration(ScriptWriterFactory stdout, ScriptWriterFactory stderr, boolean same, String command) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.same = same;
        this.command = command;
    }

    /**
     * 返回标准信息输出接口
     *
     * @return
     */
    public ScriptWriterFactory getStdout() {
        return stdout;
    }

    /**
     * 返回错误信息输出接口
     *
     * @return
     */
    public ScriptWriterFactory getStderr() {
        return stderr;
    }

    /**
     * 返回 true 表示标注输出与错误输出流是同一个对象
     *
     * @return
     */
    public boolean isSame() {
        return same;
    }

    /**
     * 返回命令语句
     *
     * @return
     */
    public String getCommand() {
        return command;
    }

}
