package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.printer.StandardPrinter;
import icu.etl.script.UniversalScriptStdout;

/**
 * 标准信息输出接口的实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptStdout extends StandardPrinter implements UniversalScriptStdout {

    /**
     * 初始化
     */
    public ScriptStdout() {
        super();
    }

    /**
     * 初始化
     *
     * @param writer 输出流
     * @param format 格式化工具
     */
    public ScriptStdout(Writer writer, Format format) {
        this();
        this.setWriter(writer);
        this.setFormatter(format);
    }

}
