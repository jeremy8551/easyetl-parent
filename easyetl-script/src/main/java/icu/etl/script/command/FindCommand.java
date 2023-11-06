package icu.etl.script.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.printer.OutputStreamPrinter;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.io.ScriptFile;
import icu.etl.script.io.ScriptNullStdout;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 搜索命令 <br>
 * find -n test -r -h -e -o -s -d -p filepath <br>
 */
public class FindCommand extends AbstractTraceCommand {

    /** 输出流 */
    private OutputStream out;

    /** 文件路径或目录路径 */
    private String filepath;

    /** 搜索内容 */
    private String name;

    /** 文件编码 */
    private String encoding;

    /** 日志文件路径 */
    private String outputFile;

    /** 输出信息时使用分隔符 */
    private String outputDelimiter;

    /** true表示：遍历子目录 */
    private boolean loop;

    /** true表示: 搜索隐藏文件 */
    private boolean hidden;

    /** true表示: 删除重复字符 */
    private boolean distinct;

    /** true表示: 输出字符串的位置 */
    private boolean position;

    /** 删除重复字符 */
    private Set<String> set;

    public FindCommand(UniversalCommandCompiler compiler, String command, String filepath, String name, String encoding, String outputFile, String outputDelimiter, boolean loop, boolean hidden, boolean distinct, boolean position) {
        super(compiler, command);
        this.filepath = filepath;
        this.name = name;
        this.encoding = encoding;
        this.outputFile = outputFile;
        this.outputDelimiter = outputDelimiter;
        this.loop = loop;
        this.hidden = hidden;
        this.distinct = distinct;
        this.position = position;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String name = analysis.replaceVariable(session, context, this.name, false);
        String filepath = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.filepath, true, true, true, false));

        if (analysis.isBlankline(this.outputFile)) {
            if (session.isEchoEnable() || forceStdout) {
                this.out = new OutputStreamPrinter(stdout, this.encoding);
            } else {
                this.out = new OutputStreamPrinter(new ScriptNullStdout(stdout), this.encoding);
            }
        } else {
            File logfile = new ScriptFile(session, context, this.outputFile);
            FileUtils.createFile(logfile);
            this.out = new FileOutputStream(logfile);
        }

        File file = new ScriptFile(session, context, filepath);
        this.search(session, file, name);
        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

    protected void search(UniversalScriptSession session, File fileOrDir, String name) throws IOException {
        if (fileOrDir.exists() && fileOrDir.isDirectory()) {
            File[] list = FileUtils.array(fileOrDir.listFiles());
            for (File file : list) {
                if (session.isTerminate()) {
                    return;
                }

                if (file.exists() && file.isFile()) {
                    this.searchFile(session, file, name);
                } else if (file.exists() && file.isDirectory() && this.loop) {
                    this.search(session, file, name);
                }
            }
        } else if (fileOrDir.isFile() && fileOrDir.exists() && fileOrDir.canRead()) {
            this.searchFile(session, fileOrDir, name);
        }
    }

    protected void searchFile(UniversalScriptSession session, File file, String name) throws IOException {
        if (file.isHidden() && !this.hidden) {
            return;
        }

        BufferedReader reader = IO.getBufferedReader(file, this.encoding);
        try {
            int lineno = 0;
            String line = null;
            Pattern pattern = Pattern.compile(name);
            while ((line = reader.readLine()) != null) {
                if (session.isTerminate()) {
                    return;
                }

                lineno++;
                line = StringUtils.replaceAll(line, "\t", "  ");

                Matcher match = pattern.matcher(line);
                while (match.find()) {
                    if (session.isTerminate()) {
                        return;
                    }

                    String find = match.group();
                    int begin = match.start();
                    int end = match.end();

                    if (this.distinct) {
                        if (this.set == null) {
                            this.set = new HashSet<String>();
                        }

                        String str = session.getAnalysis().trim(find, 0, 0);
                        if (!this.set.contains(str)) {
                            this.set.add(str);

                            if (this.position) {
                                this.print(file.getAbsolutePath() + " -> " + lineno + " line, range: " + begin + " - " + end);
                                this.print("\n");
                                this.print(line);
                                this.print(this.outputDelimiter);
                                this.printTips(name, line, begin);
                            } else {
                                this.print(file.getAbsolutePath() + " -> " + lineno + " line");
                                this.print("\n");
                                this.print(line);
                                this.print(this.outputDelimiter);
                            }
                        }
                    } else {
                        if (this.position) {
                            this.print(file.getAbsolutePath() + " -> " + lineno + " line, range: " + begin + " - " + end);
                            this.print("\n");
                            this.print(line);
                            this.print(this.outputDelimiter);
                            this.printTips(name, line, begin);
                        } else {
                            this.print(file.getAbsolutePath() + " -> " + lineno + " line");
                            this.print("\n");
                            this.print(line);
                            this.print(this.outputDelimiter);
                        }
                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    public void printTips(String name, String line, int begin) throws IOException {
        int length = StringUtils.length(line.substring(0, begin), this.encoding);
        int size = StringUtils.length(name, this.encoding);
        StringBuilder buf = new StringBuilder(StringUtils.left("", length + size + 2, ' '));
        for (int i = length; i < length + size; i++) {
            buf.setCharAt(i, '^');
        }
        this.print(buf.toString());
        this.print(this.outputDelimiter);
    }

    protected void print(String message) throws IOException {
        byte[] bytes = message.getBytes(this.encoding);
        this.out.write(bytes);
    }

}
