package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.concurrent.Executor;
import icu.etl.database.load.LoadEngine;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadEngineLaunch;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptThread;
import icu.etl.script.command.feature.JumpCommandSupported;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.StringUtils;

/**
 * 装载数据文件到数据库表，语法如下: <br>
 * <br>
 * db load from /file/path of del <br>
 * method <br>
 * --N(name1, name2) 用于设置数据源中字段名序列 <br>
 * P(3,2,1) 用于设置数据源（数据文件）中字段位置的序列集合（只能是数字，从1开始，1表示文件中第一列数值） <br>
 * C(key1, key2) 用于指定合并数据时的关联条件（merge模式时必填） <br>
 * modified by <br>
 * catalog=id 设置脚本引擎中数据库编目编号 <br>
 * tableCatalog=null 设置数据库表所属编目 <br>
 * launch= 用于设置装数引擎启动条件，属性值可以是类名或脚本语句（脚本语句返回值是0表示可以执行数据装载，返回值是非0不能执行数据装载） <br>
 * converter=字段名:javaClassName,f2:javaClassName?key=value, 用于设置字段名数据转换器的映射关系 <br>
 * savecount= 每装载 n 行后建立一致点。消息文件中将生成和记录一些消息，用于表明在保存点所在时间上有多少输入行被成功地装载。<br>
 * dateformat="" 用于设置日期字符串的格式 <br>
 * timeformat=“” 用于设置时间字符串的格式 <br>
 * timestampformat=“” 用于设置时间戳字符串的格式 <br>
 * keepblanks 将数据装入到一个变长列时，会截断尾部空格，若未指定则将保留空格。 <br>
 * --norowwarnings 禁止发出行警告 <br>
 * chardel=x 设置字符串字段二端的限定符 <br>
 * readbuf=100M 用于设置输入流缓冲区长度，单位字节 <br>
 * messages= 用于设置消息文件绝对路径 <br>
 * coldel=x 用于设置字段分隔符 <br>
 * --decplusblank 正数前是否使用加号 <br>
 * escapechar= 用于设置转义字符 <br>
 * nocrlf 删除字符变量中的回车符和换行符 <br>
 * --nochardel <br>
 * dumpfile=filepath 设置错误数据存储文件路径 <br>
 * codepage=1208 设置数据源的代码页 <br>
 * <br>
 * [ replace | insert | merge ] 表示数据装载模式 <br>
 * into tableName(fname1,fname3,fname2) 设置插入数据库表的方式与字段序列 <br>
 * <br>
 * for exception tableName 对于索引字段重复的字段，重复记录保存到 for 语句指定的表中 <br>
 * <br>
 * indexing mode [ rebuild | incremental ] 设置表上索引的处理规则 <br>
 * rebuild 模式强制重新构建所有索引 <br>
 * incremental 模式只向索引中添加新的数据 <br>
 * --AUTOSELECT 模式允许实用程序在 REBUILD 和 INCREMENTAL 之间作出选择 <br>
 * --DEFERRED 模式意味着在装载期间不会创建索引。涉及的索引上会作出标记，但是需要刷新。当重新启动数据库或者第一次访问那些索引时，才会重新构建那些索引。 <br>
 * <br>
 * statistics use profile 句柄表示数据文件装载后，之前的目标表统计信息很可能已经无效了，因为表中添加了更多的数据。您可以选择在构建阶段根据为目标表定义的概要文件来收集统计信息。 <br>
 * <br>
 * prevent repeat operation
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-04
 */
public class DBLoadCommand extends AbstractTraceCommand implements UniversalScriptThread, JumpCommandSupported, NohupCommandSupported {

    /** 装数引擎 */
    private LoadEngine engine;

    /** 用于判断是否可以执行装载数据任务 */
    private LoadEngineLaunch obj;

    /** 脚本语句，用于判断是否可以执行装载任务 */
    private String script;

    public DBLoadCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
        this.engine = new LoadEngine();
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        if (session.isEchoEnable() || forceStdout) {
            UniversalScriptAnalysis analysis = session.getAnalysis();
            stdout.println(analysis.replaceShellVariable(session, context, this.command, true, true, true, false));
        }

        this.engine.set(context.getFactory().getContext());
        this.engine.run();

        if (this.engine.alreadyError()) {
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            return this.engine.isTerminate() ? UniversalScriptCommand.TERMINATE : 0;
        }
    }

    public void terminate() throws IOException, SQLException {
        if (this.engine != null) {
            this.engine.terminate();
        }
    }

    public boolean start(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, ContainerCommand container) throws IOException, SQLException {
        if (this.obj != null) {
            return this.obj.ready(this.engine.getContext());
        } else if (StringUtils.isNotBlank(this.script)) {
            UniversalScriptAnalysis analysis = session.getAnalysis();
            String script = analysis.replaceShellVariable(session, context, this.script, true, true, true, false);
            UniversalScriptEngine engine = context.getEngine();
            return engine.eval(session, context, stdout, stderr, script) == 0;
        } else {
            return true;
        }
    }

    public Executor getExecutor() {
        LoadEngine engine = this.engine;
        this.engine = null;
        return engine;
    }

    /**
     * 用于判断是否可以执行装载数据任务
     *
     * @param obj
     */
    public void setRule(LoadEngineLaunch obj) {
        this.obj = obj;
    }

    /**
     * 脚本语句，用于判断是否可以执行装载任务
     *
     * @param str
     */
    public void setRule(String str) {
        this.script = str;
    }

    /**
     * 返回装数引擎上下文信息
     *
     * @return
     */
    public LoadEngineContext getContext() {
        if (this.engine == null) {
            return null;
        } else {
            return this.engine.getContext();
        }
    }

    public boolean enableNohup() {
        return true;
    }

    public boolean enableJump() {
        return true;
    }

}
