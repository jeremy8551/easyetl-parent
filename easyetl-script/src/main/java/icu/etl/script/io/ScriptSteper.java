package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.log.Log;
import icu.etl.printer.StandardPrinter;
import icu.etl.script.UniversalScriptSteper;

/**
 * 步骤信息输出的接口实现类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptSteper extends StandardPrinter implements UniversalScriptSteper {

    /**
     * 初始化
     */
    public ScriptSteper(Log log) {
        if (log == null) {
            throw new NullPointerException();
        } else {
            this.log = log;
        }
    }

    /**
     * 初始化
     *
     * @param log
     * @param writer
     * @param format
     */
    public ScriptSteper(Log log, Writer writer, Format format) {
        this(log);
        this.setWriter(writer);
        this.setFormatter(format);
    }

}
