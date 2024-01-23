package icu.etl.script.command;

import icu.etl.script.UniversalCommandCompiler;

/**
 * 使用脚本命令具有归属的功能
 *
 * @author jeremy8551@qq.com
 */
public abstract class AbstractSlaveCommand extends AbstractTraceCommand {

    /** break 命令归属的循环体对象 */
    private Object owner;

    public AbstractSlaveCommand(UniversalCommandCompiler compiler, String str) {
        super(compiler, str);
    }

    /**
     * 设置归属的对象
     *
     * @param object 归属对象
     */
    public void setOwner(Object object) {
        this.owner = object;
    }

    /**
     * 判断是否存在拥有者
     *
     * @return 返回true表示存在拥有者
     */
    public boolean existsOwner() {
        return this.owner != null;
    }

}
