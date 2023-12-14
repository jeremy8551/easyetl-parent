package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.concurrent.ThreadSource;
import icu.etl.io.TextTableFile;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.sort.OrderByExpression;
import icu.etl.sort.TableFileSortContext;
import icu.etl.sort.TableFileSorter;
import icu.etl.util.IO;

/**
 * 对表格型文件进行排序 <br>
 * <p>
 * sort table file ~/file/name.txt of del [modified by filecount=2 keeptmp readbuf=8192] order by int(1) desc,2,3 {asc | desc}
 *
 * @author jeremy8551@qq.com
 */
public class SortTableFileCommand extends AbstractTraceCommand {

    private TableFileSorter tfs;
    private String filepath;
    private String filetype;
    private OrderByExpression[] orders;
    private CommandAttribute map;

    public SortTableFileCommand(UniversalCommandCompiler compiler, String script, String filepath, String filetype, OrderByExpression[] orders, CommandAttribute map) {
        super(compiler, script);
        this.filepath = filepath;
        this.filetype = filetype;
        this.orders = orders;
        this.map = map;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(session.getAnalysis().replaceShellVariable(session, context, this.command, true, true, true, false));
        }

        TableFileSortContext cxt = new TableFileSortContext();
        cxt.setThreadSource(context.getContainer().getBean(ThreadSource.class));
        cxt.setDeleteFile(!this.map.contains("keeptemp"));
        cxt.setKeepSource(!this.map.contains("covsrc"));
        if (this.map.contains("maxfile")) {
            cxt.setFileCount(this.map.getIntAttribute("maxfile"));
        }
        if (this.map.contains("maxrow")) {
            cxt.setMaxRows(this.map.getIntAttribute("maxrow"));
        }
        if (this.map.contains("readbuf")) {
            cxt.setReaderBuffer(this.map.getIntAttribute("readbuf"));
        } else {
            cxt.setReaderBuffer(IO.FILE_BYTES_BUFFER_SIZE);
        }
        if (this.map.contains("thread")) {
            cxt.setThreadNumber(this.map.getIntAttribute("thread"));
        }
        if (this.map.contains("writebuf")) {
            cxt.setWriterBuffer(this.map.getIntAttribute("writebuf"));
        }

        TextTableFile file = context.getContainer().getBean(TextTableFile.class, this.filetype, this.map);
        file.setAbsolutePath(this.filepath);

        this.tfs = new TableFileSorter(cxt);
        this.tfs.sort(file, this.orders);
        return 0;
    }

    public void terminate() throws IOException, SQLException {
        if (this.tfs != null) {
            this.tfs.terminate();
        }
    }

}
