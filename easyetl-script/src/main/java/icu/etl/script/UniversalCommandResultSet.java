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
     * @return 返回值
     */
    int getExitcode();

    /**
     * 判断退出会话的方式
     *
     * @return 返回true表示退出当前会话 false表示继续向下执行
     */
    boolean isExitSession();

    /**
     * 设置退出会话的方式
     *
     * @param exit 设置true表示立刻退出当前会话 false表示（忽略错误）继续向下执行
     */
    void setExitSession(boolean exit);

    /**
     * 设置脚本命令返回值
     *
     * @param value 返回值
     */
    void setExitcode(int value);

}
