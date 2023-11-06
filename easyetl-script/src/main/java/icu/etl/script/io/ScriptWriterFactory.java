package icu.etl.script.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import icu.etl.io.NullWriter;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;

/**
 * 日志文件输出流
 *
 * @author jeremy8551@qq.com
 */
public class ScriptWriterFactory {

    /** 输出流 */
    private Writer out;

    /** 文件绝对路径 */
    private String filepath;

    /** true表示将数据写入到文件末尾位置 false表示将数据写入文件起始位置（会覆盖原文件内容） */
    private boolean append;

    /** 日志文件 */
    private File logfile;

    /**
     * 初始化
     *
     * @param filepath 文件绝对路径
     * @param append   true表示将数据写入到文件末尾位置 false表示将数据写入文件起始位置（会覆盖原文件内容）
     */
    public ScriptWriterFactory(String filepath, boolean append) {
        this.filepath = filepath;
        this.append = append;
    }

    /**
     * 打开输出流
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @return
     * @throws IOException
     */
    public Writer build(UniversalScriptSession session, UniversalScriptContext context) throws IOException {
        if (session == null) {
            throw new NullPointerException();
        }
        if (context == null) {
            throw new NullPointerException();
        }

        UniversalScriptAnalysis analysis = session.getAnalysis();
        String filepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.filepath, true, true, true, false));
        this.logfile = new File(filepath);
        if ("/dev/null".equals(filepath)) {
            this.out = new NullWriter();
        } else {
            if (FileUtils.createFile(this.logfile)) {
                this.out = IO.getFileWriter(this.logfile, context.getCharsetName(), this.append);
            } else {
                throw new IOException(ResourcesUtils.getScriptStderrMessage(62, this.logfile.getAbsolutePath()));
            }
        }
        return this.out;
    }

    /**
     * 返回日志文件
     *
     * @return
     */
    public File getFile() {
        return this.logfile;
    }

    /**
     * 关闭输出流
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (this.out != null) {
            try {
                this.out.flush();
            } finally {
                this.out.close();
                this.out = null;
            }
        }
    }

}
