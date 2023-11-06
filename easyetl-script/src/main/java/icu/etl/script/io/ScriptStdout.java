package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

import icu.etl.log.Log;
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
     *
     * @param log
     */
    public ScriptStdout(Log log) {
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
    public ScriptStdout(Log log, Writer writer, Format format) {
        this(log);
        this.setWriter(writer);
        this.setFormatter(format);
    }

}
