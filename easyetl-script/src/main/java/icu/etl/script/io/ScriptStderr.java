package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.log.Log;
import icu.etl.script.UniversalScriptStderr;

/**
 * 错误信息输出接口的实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptStderr extends ScriptStdout implements UniversalScriptStderr {

    public ScriptStderr(Log log, Writer writer, Format f) {
        super(log, writer, f);
    }

    public ScriptStderr(Log log) {
        super(log);
    }

}
