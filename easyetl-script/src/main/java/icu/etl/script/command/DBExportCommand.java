package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import icu.etl.concurrent.Executor;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.export.Extracter;
import icu.etl.database.export.ExtracterContext;
import icu.etl.database.export.UserListener;
import icu.etl.database.internal.StandardJdbcConverterMapper;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyContext;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptThread;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.ProgressMap;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.util.ClassUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * db export to datapath of type modified by attributes source <br>
 * <br>
 * <br>
 * attributes 属性包括: <br>
 * colname # 表示输出字段名，如果title参数有值表示输出指定标题信息 <br>
 * append #表示追加输出数据 <br>
 * dateformat="" <br>
 * timeformat=“” <br>
 * timestampformat=“” <br>
 * catalog= # 数据库编目 <br>
 * chardel=’ # 字符串分隔符 <br>
 * charhide=“|’ # 删除字符串中特定字符 <br>
 * escapes=“|\ 对字符串中特定字符进行转义 <br>
 * escape=\ <br>
 * coldel=, # 字段分隔符 <br>
 * convert=1:javaClassName,f2:javaClassName?key=value, <br>
 * skip=-1 # true表示根据日志文件过滤指定行数后开始卸载数据文件 <br>
 * rowdel=\n # 文件行分隔符 <br>
 * rowlistener= # 行监听器 <br>
 * encoding=UTF-8 # 文件内容字符集 <br>
 * cache=1000 # 写入文件的缓存行数 <br>
 * maximum=100000 # 文件最大记录数，超过时写入新文件 <br>
 * message=“c:\\test 2020\\test\export.log” # 设置信息文件所在位置 <br>
 * <br>
 * <br>
 * source: <br>
 * select … from table with ur ; <br>
 * <br>
 * <br>
 * datapath 表达式格式: <br>
 * http://download/request/response/filename <br>
 * sftp://name@host:port?password=/filepath <br>
 * ftp://name@host:port?password=/filepath <br>
 * bean://kind/mode/major/minor <br>
 * filepath <br>
 * <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 */
public class DBExportCommand extends AbstractTraceCommand implements UniversalScriptThread, NohupCommandSupported {

    /** 任务信息 */
    private Extracter executor;

    /** 数据文件 */
    private String dataTarget;

    /** 数据文件类型 */
    private String dataType;

    /** SQL语句 */
    private String dataSource;

    /** 输入属性 */
    private CommandAttribute attrs;

    public DBExportCommand(UniversalCommandCompiler compiler, String command, String dataTarget, String dataType, String sql, CommandAttribute attributes) {
        super(compiler, command);
        this.dataTarget = dataTarget;
        this.dataType = dataType;
        this.dataSource = sql;
        this.attrs = attributes;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (this.start(session, context, stdout, stderr, null)) {
            if (session.isEchoEnable() || forceStdout) {
                String newTarget = FileUtils.replaceFolderSeparator(this.dataTarget);
                String newCommand = StringUtils.replace(this.command, this.dataTarget, newTarget);
                UniversalScriptAnalysis analysis = session.getAnalysis();
                stdout.println(analysis.replaceShellVariable(session, context, newCommand, true, true, true, true));
            }

            this.executor.run();
            return (this.executor.alreadyError() || this.executor.isTerminate()) ? UniversalScriptCommand.TERMINATE : 0;
        } else {
            return UniversalScriptCommand.COMMAND_ERROR;
        }
    }

    public void terminate() throws IOException, SQLException {
        if (this.executor != null) {
            this.executor.terminate();
        }
    }

    public boolean start(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, ContainerCommand container) throws IOException, SQLException {
        if (this.executor == null) {
            this.executor = new Extracter(context.getFactory().getContext());
            this.executor.getLogger().setStdout(stdout);
            this.executor.getLogger().setStderr(stderr);

            UniversalScriptAnalysis analysis = session.getAnalysis();
            String newTarget = FileUtils.replaceFolderSeparator(analysis.replaceShellVariable(session, context, this.dataTarget, true, true, true, false));
            String dataType = analysis.replaceShellVariable(session, context, this.dataType, true, true, true, false);
            String dataSource = analysis.replaceShellVariable(session, context, this.dataSource, true, true, true, true);
            CommandAttribute attribute = this.attrs.clone(session, context);

            TextTableFile format = context.getFactory().getContext().getBean(TextTableFile.class, dataType, attribute);
            String messagefilepath = attribute.getAttribute("messagefilepath");
            File msgfile = FileUtils.createFile(messagefilepath, 3, "messagefile", "export");
            JdbcConverterMapper mapper = new StandardJdbcConverterMapper(attribute.getAttribute("convert"), String.valueOf(analysis.getSegment()), String.valueOf(analysis.getMapdel()));
            UserListenerList listeners = new UserListenerList(context.getFactory().getContext(), attribute.getAttribute("listener"));

            // 保存属性
            ExtracterContext cxt = this.executor.getContext();
            cxt.setTarget(newTarget);
            cxt.setSource(dataSource);
            cxt.setFormat(format);
            cxt.setConverters(mapper);
            cxt.setMessagefile(msgfile);
            cxt.setListener(listeners);
            cxt.setAppend(attribute.contains("append"));
            cxt.setCacheLines(attribute.contains("writebuf") ? attribute.getIntAttribute("writebuf") : 100);
            cxt.setCharFilter(attribute.getAttribute("charhide"));
            cxt.setEscapes(attribute.getAttribute("escapes"));
            cxt.setDateformat(attribute.getAttribute("dateformat"));
            cxt.setTimeformat(attribute.getAttribute("timeformat"));
            cxt.setTimestampformat(attribute.getAttribute("timestampformat"));
            cxt.setTitle(attribute.contains("colname"));
            cxt.setMaximum(attribute.contains("maxrows") ? attribute.getIntAttribute("maxrows") : 0);
            cxt.setProgress(attribute.contains("progress") ? ProgressMap.getProgress(context, attribute.getAttribute("progress")) : null);
            cxt.setHttpServletRequest(context.getAttribute("httpServletRequest"));
            cxt.setHttpServletResponse(context.getAttribute("httpServletResponse"));

            // 确定卸数使用的数据库连接
            ScriptDataSource pool = ScriptDataSource.get(context);
            String catalog = StringUtils.defaultString(attribute.getAttribute("catalog"), pool.getCatalog());
            if (StringUtils.isBlank(catalog)) { // 默认使用脚本引擎当前正在使用的数据库编目
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(65, this.command));
            } else {
                cxt.setDataSource(pool.getPool(catalog));
            }
        }

        return this.executor.getListener().ready();
    }

    public Executor getExecutor() {
        Extracter instance = this.executor;
        this.executor = null;
        return instance;
    }

    public boolean enableNohup() {
        return true;
    }

    private class UserListenerList extends ArrayList<UserListener> {
        private final static long serialVersionUID = 1L;

        public UserListenerList(EasyContext context, String listeners) {
            super();
            this.parse(context, listeners);
        }

        public void parse(EasyContext context, String listeners) {
            String[] array = StringUtils.split(StringUtils.trimBlank(listeners), ',');
            for (String className : array) {
                if (StringUtils.isNotBlank(className)) {
                    this.add(context.createBean(ClassUtils.loadClass(className)));
                }
            }
        }
    }

}
