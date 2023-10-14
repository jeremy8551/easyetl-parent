package icu.etl.script.command;

import icu.etl.script.UniversalCommandCompiler;

/**
 * 文件操作类命令模版
 *
 * @author jeremy8551@qq.com
 */
public abstract class AbstractFileCommand extends AbstractTraceCommand {

    public AbstractFileCommand(UniversalCommandCompiler compiler, String str) {
        super(compiler, str);
    }
}
