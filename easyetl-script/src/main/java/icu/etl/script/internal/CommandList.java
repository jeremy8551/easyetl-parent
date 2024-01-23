package icu.etl.script.internal;

import java.util.ArrayList;
import java.util.List;

import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.command.AbstractSlaveCommand;
import icu.etl.util.Ensure;

/**
 * 脚本命令集合
 */
public class CommandList extends ArrayList<UniversalScriptCommand> implements Cloneable {
    private final static long serialVersionUID = 1L;

    /** 脚本命令集合名字 */
    protected String name;

    /**
     * 初始化
     *
     * @param name     名字
     * @param commands 脚本命令集合
     */
    public CommandList(String name, List<UniversalScriptCommand> commands) {
        super(commands.size());
        this.name = name;
        this.addAll(commands);
    }

    /**
     * 脚本命令集合归属的命令
     *
     * @param scriptCommand 脚本命令
     */
    public void setOwner(UniversalScriptCommand scriptCommand) {
        Ensure.notNull(scriptCommand);
        for (int i = 0; i < this.size(); i++) {
            UniversalScriptCommand command = this.get(i);
            if (command instanceof AbstractSlaveCommand) {
                ((AbstractSlaveCommand) command).setOwner(scriptCommand);
            }
        }
    }

    /**
     * 返回脚本命令集合名字
     *
     * @return 命令集合的名字
     */
    public String getName() {
        return name;
    }

    /**
     * 清空所有信息
     */
    public void clear() {
        super.clear();
        this.name = null;
    }

    public CommandList clone() {
        return new CommandList(this.name, this);
    }

}