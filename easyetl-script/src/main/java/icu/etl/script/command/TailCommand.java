package icu.etl.script.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import icu.etl.collection.ArrayDeque;
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
 * 读取字符串最后几行内容 <br>
 * <br>
 * tail -n 1 文件名或文件路径
 *
 * @author jeremy8551@qq.com
 */
public class TailCommand extends AbstractFileCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** 前几行数据 */
    private int line;

    /** 管道输入参数 */
    private String parameter;

    /** 文件字符集编码 */
    private String charsetName;

    /** 文件绝对路径 */
    private String filepath;

    public TailCommand(UniversalCommandCompiler compiler, String command, int line, String charsetName, String filepath) {
        super(compiler, command);
        this.line = line;
        this.charsetName = charsetName;
        this.filepath = filepath;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (analysis.isBlankline(this.filepath)) {
            this.parameter = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
        } else {
            throw new UniversalScriptException(ResourcesUtils.getMessage("script.message.stderr014", this.command, "tail", this.filepath));
        }
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        ArrayDeque<String> queue = new ArrayDeque<String>(this.line);

        if (session.getAnalysis().isBlankline(this.filepath)) {
            BufferedLineReader in = new BufferedLineReader(this.parameter);
            try {
                while (!this.terminate && in.hasNext()) {
                    String line = in.next();
                    queue.offer(line);
                    if (queue.size() > this.line) { // 先进先出
                        queue.poll();
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
                while (!this.terminate && (line = in.readLine()) != null) {
                    queue.offer(line);
                    if (queue.size() > this.line) { // 先进先出
                        queue.poll();
                    }
                }
            } finally {
                in.close();
            }
        }

        if (session.isEchoEnable() || forceStdout) {
            for (String line : queue) {
                stdout.println(line);
            }
        }
        return this.terminate ? UniversalScriptCommand.TERMINATE : 0;
    }

    public void terminate() throws Exception {
        this.terminate = true;
    }

    public boolean enableNohup() {
        return true;
    }

}
