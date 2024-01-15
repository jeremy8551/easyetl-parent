package icu.etl.script.internal;

import icu.etl.script.UniversalCommandResultSet;

/**
 * 脚本命令返回值
 *
 * @author jeremy8551@qq.com
 */
public class CommandResultSet implements UniversalCommandResultSet {

    /** 脚本命令返回值 */
    private int exitcode;

    /** true表示退出当前用户会话 */
    private boolean exit;

    public CommandResultSet() {
        this.exit = false;
    }

    public void setExitcode(int value) {
        this.exitcode = value;
    }

    public int getExitcode() {
        return this.exitcode;
    }

    public boolean isExitSession() {
        return this.exit;
    }

    public void setExitSession(boolean exit) {
        this.exit = exit;
    }

}
