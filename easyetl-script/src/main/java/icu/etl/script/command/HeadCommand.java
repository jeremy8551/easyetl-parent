package icu.etl.script.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import icu.etl.io.BufferedLineReader;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
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
 * 读取字符串前几行内容 <br>
 * <br>
 * head -n 1 /home/user/file.txt
 *
 * @author jeremy8551@qq.com
 */
public class HeadCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 前几行数据 */
    private int line;

    /** 管道输入参数 */
    private String parameter;

    /** 文件字符集编码 */
    private String charsetName;

    /** 文件绝对路径 */
    private String filepath;

    public HeadCommand(UniversalCommandCompiler compiler, String command, int line, String parameter, String charsetName, String filepath) {
        super(compiler, command);
        this.line = line;
        this.parameter = parameter;
        this.charsetName = charsetName;
        this.filepath = filepath;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.parameter = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "head", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        int count = 0;
        boolean print = session.isEchoEnable() || forceStdout;
        if (session.getAnalysis().isBlankline(this.filepath)) {
            BufferedLineReader in = new BufferedLineReader(this.parameter);
            try {
                while (in.hasNext()) {
                    String line = in.next();
                    if (++count <= this.line && print) {
                        stdout.println(line);
                    }
                }
            } finally {
                in.close();
            }
        } else {
            ScriptFile file = new ScriptFile(session, context, this.filepath);
            BufferedReader in = IO.getBufferedReader(file, StringUtils.defaultString(this.charsetName, context.getCharsetName()));
            try {
                String line = null;
                while ((line = in.readLine()) != null && ++count <= this.line && print) {
                    stdout.println(line);
                }
            } finally {
                in.close();
            }
        }
        return 0;
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
