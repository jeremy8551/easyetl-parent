package icu.etl.script.io;

import java.io.Writer;
import java.text.Format;

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
    public ScriptSteper() {
        super();
    }

    /**
     * 初始化
     *
     * @param writer 输出流
     * @param format 格式化工具
     */
    public ScriptSteper(Writer writer, Format format) {
        this();
        this.setWriter(writer);
        this.setFormatter(format);
    }

}
