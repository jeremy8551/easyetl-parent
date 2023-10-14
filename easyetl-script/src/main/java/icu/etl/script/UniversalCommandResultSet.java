package icu.etl.script;

/**
 * 脚本引擎执行结果
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalCommandResultSet {

    /**
     * 返回脚本命令的返回值
     *
     * @return
     */
    int getExitcode();

    /**
     * 返回 true 表示退出当前会话
     *
     * @return
     */
    boolean isExitSession();

    /**
     * 设置 true 表示立刻退出当前会话
     *
     * @param val
     */
    void setExitSession(boolean val);

    /**
     * 设置脚本命令返回值
     *
     * @param value
     */
    void setExitcode(int value);

}
