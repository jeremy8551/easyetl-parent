package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import icu.etl.io.BufferedLineReader;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 过滤数据 <br>
 * {@literal -B<显示行数> 或 --before-context=<显示行数> : 除了显示符合样式的那一行之外，并显示该行之前的内容。} <br>
 * {@literal -C<显示行数> 或 --context=<显示行数>或-<显示行数> : 除了显示符合样式的那一行之外，并显示该行之前后的内容。} <br>
 * -i 或 --ignore-case : 忽略字符大小写的差别。 <br>
 * -o 或 --only-matching : 只显示匹配PATTERN 部分。 <br>
 * -v 或 --invert-match : 显示不包含匹配文本的所有行。 <br>
 * -l 或 --file-with-matches : 列出文件内容符合指定的样式的文件名称。 <br>
 * -L 或 --files-without-match : 列出文件内容不符合指定的样式的文件名称。 <br>
 * -F 或 --fixed-regexp : 将样式视为固定字符串的列表。 <br>
 * -G 或 --basic-regexp : 将样式视为普通的表示法来使用。 <br>
 */
public class GrepCommand extends AbstractTraceCommand implements UniversalScriptInputStream, NohupCommandSupported {

    /** true表示忽略大小写 */
    private boolean ignoreCase;

    /** true 表示排除 */
    private boolean exclude;

    /** 查找的字符串 */
    private String find;

    /** 字符串内容 */
    private String inputString;

    public GrepCommand(UniversalCommandCompiler compiler, String command, boolean ignoreCase, boolean exclude, String find) {
        super(compiler, command);
        this.ignoreCase = ignoreCase;
        this.exclude = exclude;
        this.find = find;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        this.inputString = StringUtils.trimBlank(IO.read(in, new StringBuilder()));
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        StringBuilder buf = new StringBuilder(this.inputString.length());
        BufferedLineReader in = new BufferedLineReader(this.inputString);
        try {
            if (this.exclude) {
                while (in.hasNext()) {
                    String line = in.next();

                    if (StringUtils.indexOf(line, this.find, 0, this.ignoreCase) == -1) {
                        buf.append(line).append(in.getLineSeparator());
                        continue;
                    }
                }
            } else {
                while (in.hasNext()) {
                    String line = in.next();

                    if (StringUtils.indexOf(line, this.find, 0, this.ignoreCase) != -1) {
                        buf.append(line).append(in.getLineSeparator());
                        continue;
                    }
                }
            }
        } finally {
            in.close();
        }

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(buf);
        }
        return 0;
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

}
