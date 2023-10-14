package icu.etl.script.internal;

import icu.etl.printer.Printer;
import icu.etl.printer.Progress;

/**
 * 脚本引擎进度输出类
 *
 * @author jeremy8551@qq.com
 */
public class ScriptProgress extends Progress {

    /**
     * 初始化
     *
     * @param out
     * @param message
     * @param total
     */
    public ScriptProgress(Printer out, String message, int total) {
        super(out, message, total);
    }

    /**
     * 初始化
     *
     * @param taskId
     * @param out
     * @param message
     * @param total
     */
    public ScriptProgress(String taskId, Printer out, String message, int total) {
        super(taskId, out, message, total);
    }

}
