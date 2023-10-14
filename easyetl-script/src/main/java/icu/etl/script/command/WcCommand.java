package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.io.BufferedLineReader;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.io.ScriptFile;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 显示文件的行数 字数 字节数 文件名 <br>
 * <br>
 * wc -l /home/user/file.txt
 *
 * @author jeremy8551@qq.com
 */
public class WcCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 文件字符集编码 */
    private String charsetName;

    /** 文件绝对路径 */
    private String filepath;

    /** 管道输入参数 */
    private String parameter;

    /** true表示统计字数 */
    private boolean words;

    /** true表示统计字节数 */
    private boolean bytes;

    /** true表示统计行数 */
    private boolean lines;

    public WcCommand(UniversalCommandCompiler compiler, String command, String filepath, String charsetName, String parameter, boolean words, boolean bytes, boolean lines) {
        super(compiler, command);
        this.filepath = filepath;
        this.charsetName = charsetName;
        this.parameter = parameter;
        this.words = words;
        this.bytes = bytes;
        this.lines = lines;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.parameter = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "wc", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        long rows = 0, words = 0, bytes = 0;
        String last = "";

        if (session.getAnalysis().isBlankline(this.filepath)) {
            BufferedLineReader in = new BufferedLineReader(this.parameter);
            try {
                for (; !this.terminate && in.hasNext(); in.next()) {
                    rows++;
                }
                words = this.parameter.length();
                bytes = StringUtils.length(this.parameter, StringUtils.defaultString(this.charsetName, context.getCharsetName()));
            } finally {
                in.close();
            }
        } else {
            File file = new ScriptFile(session, context, this.filepath);
            bytes = file.length();

            BufferedLineReader in = new BufferedLineReader(file, StringUtils.defaultString(this.charsetName, context.getCharsetName()));
            try {
                words = in.skip(file.length());
                rows = in.getLineNumber();
            } finally {
                in.close();
            }

            last = " " + file.getAbsolutePath();
        }

        StringBuilder buf = new StringBuilder(50);
        if (this.lines) {
            buf.append(StringUtils.right(rows, 10, this.charsetName, ' '));
        }

        if (this.words) {
            buf.append(StringUtils.right(words, 10, this.charsetName, ' '));
        }

        if (this.bytes) {
            buf.append(StringUtils.right(bytes, 10, this.charsetName, ' '));
        }

        buf.append(last);

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(buf);
        }
        return this.terminate ? UniversalScriptCommand.TERMINATE : 0;
    }

    public void terminate() throws IOException, SQLException {
        this.terminate = true;
    }

    public boolean enableNohup() {
        return true;
    }

}
