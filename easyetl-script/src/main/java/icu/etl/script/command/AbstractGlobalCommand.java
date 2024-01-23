package icu.etl.script.command;

import icu.etl.script.UniversalCommandCompiler;

/**
 * 具有设置全局属性的命令模版类
 */
public abstract class AbstractGlobalCommand extends AbstractCommand {

    /** true表示命令具备全局属性 false表示命令具备局部属性 */
    private boolean global = false;

    public AbstractGlobalCommand(UniversalCommandCompiler compiler, String str) {
        super(compiler, str);
    }

    /**
     * 判断变量或配置是否是全局状态
     *
     * @param value 设置 true 表示变量或配置是全局状态
     */
    public void setGlobal(boolean value) {
        this.global = value;
    }

    /**
     * 判断变量或配置是否是全局状态
     *
     * @return 返回 true 表示变量或配置是全局状态
     */
    public boolean isGlobal() {
        return this.global;
    }

}
