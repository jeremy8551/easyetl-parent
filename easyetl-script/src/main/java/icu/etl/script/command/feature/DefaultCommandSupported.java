package icu.etl.script.command.feature;

import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

/**
 * 返回 true 表示命令可以作为脚本引擎的默认命令使用。 <br>
 * 即: 当脚本引擎不能识别脚本命令语句时，脚本命令语句将作为默认命令执行。 <br>
 * 默认命令的 {@linkplain UniversalScriptCommand#execute(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean)} 方法实现应支持不同命令的输入。
 *
 * @author jeremy8551@qq.com
 */
public interface DefaultCommandSupported {

}
