package icu.etl.script.command.feature;

import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

public interface JumpCommandSupported {

    /**
     * 返回 true 表示命令在 jump 命令过程中不会执行 {@linkplain icu.etl.script.UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法
     *
     * @return
     */
    boolean enableJump();

}
