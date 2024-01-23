package icu.etl.script.command;

import icu.etl.script.io.ScriptWriterFactory;

public class AbstractTraceCommandConfiguration {

    /** 标准信息输出流 */
    protected ScriptWriterFactory stdout;

    /** 错误信息输出流 */
    protected ScriptWriterFactory stderr;

    /** 等于true表示标注输出与错误输出流是同一个对象 */
    protected boolean same;

    /** 脚本命令 */
    protected String command;

    /**
     * 初始化
     *
     * @param stdout  标准信息输出接口
     * @param stderr  错误信息输出接口
     * @param same    true表示标准输出与错误输出使用同一输出流
     * @param command 脚本命令
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
     * @return 标准信息输出接口
     */
    public ScriptWriterFactory getStdout() {
        return stdout;
    }

    /**
     * 返回错误信息输出接口
     *
     * @return 错误信息输出接口
     */
    public ScriptWriterFactory getStderr() {
        return stderr;
    }

    /**
     * 判断标准输出与错误输出流是同一个输出流
     *
     * @return 返回 true 表示标注输出与错误输出流是同一个对象
     */
    public boolean isSame() {
        return same;
    }

    /**
     * 返回命令语句
     *
     * @return 脚本命令
     */
    public String getCommand() {
        return command;
    }

}
