package icu.etl.script.io;

import java.io.File;

import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.FileUtils;

/**
 * 文件信息
 *
 * @author jeremy8551@qq.com
 */
public class ScriptFile extends File {
    private final static long serialVersionUID = 1L;

    /**
     * 替换文件路径中的变量
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param pathname 路径信息（如果是文件名，则默认使用当前目录作为父目录）
     */
    public ScriptFile(UniversalScriptSession session, UniversalScriptContext context, String pathname) {
        super(replaceFilepath(session, context, pathname));
    }

    /**
     * 替换文件路径中的变量
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param pathname 路径信息（如果是文件名，则默认使用当前目录作为父目录）
     * @return
     */
    private static String replaceFilepath(UniversalScriptSession session, UniversalScriptContext context, String pathname) {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String filepath0 = analysis.replaceShellVariable(session, context, pathname, true, true, true, false);
        String filepath1 = FileUtils.replaceFolderSeparator(filepath0);
        String parent = FileUtils.getParent(filepath1);
        return parent == null ? FileUtils.joinPath(session.getDirectory(), filepath1) : filepath1;
    }

}
