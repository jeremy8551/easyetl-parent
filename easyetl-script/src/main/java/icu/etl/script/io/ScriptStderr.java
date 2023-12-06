package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.script.UniversalScriptStderr;

/**
 * 错误信息输出接口的实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptStderr extends ScriptStdout implements UniversalScriptStderr {

    /**
     * 初始化
     */
    public ScriptStderr() {
        super();
    }

    /**
     * 初始化
     *
     * @param writer 输出流
     * @param format 格式化工具
     */
    public ScriptStderr(Writer writer, Format format) {
        super(writer, format);
    }

}
