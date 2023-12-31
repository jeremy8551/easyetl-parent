package icu.etl.script.compiler;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.PriorityQueue;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyContext;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptCompiler;
import icu.etl.script.UniversalScriptConfiguration;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.internal.CommandRepository;
import icu.etl.util.StringUtils;

/**
 * 编译器
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "default", description = "即时编译器")
public class ScriptCompiler implements UniversalScriptCompiler {

    /** 脚本命令编译器集合 */
    protected CommandRepository commandRepository;

    /** 缓冲区 */
    protected PriorityQueue<UniversalScriptCommand> cache;

    /** true表示终止编译操作 */
    protected volatile boolean terminate;

    /** 语句分析器 */
    protected UniversalScriptAnalysis analysis;

    /** 词法分析器 */
    protected ScriptReader reader;

    /** 语法分析器 */
    protected ScriptParser parser;

    /** 起始行数 */
    protected long startLineNumber;

    /** 容器上下文信息 */
    protected EasyContext context;

    /** 读取命令的时间戳 */
    protected long readMillis;

    /**
     * 初始化
     */
    public ScriptCompiler(EasyContext context) {
        this.context = context;
        this.cache = new PriorityQueue<UniversalScriptCommand>(10, new Comparator<UniversalScriptCommand>() {
            public int compare(UniversalScriptCommand o1, UniversalScriptCommand o2) {
                return 0;
            }
        });

        this.analysis = this.context.getBean(UniversalScriptAnalysis.class);
        this.commandRepository = new CommandRepository(this.analysis);
        this.terminate = false;
        this.startLineNumber = 0;
    }

    public UniversalScriptCompiler buildCompiler() {
        ScriptCompiler obj = new ScriptCompiler(this.context);
        obj.getRepository().setDefault(this.commandRepository.getDefault()); // 设置子编译器的默认命令
        if (this.reader != null) { // 设置起始行数
            obj.startLineNumber = this.reader.getLineNumber() - 1; // 设置子编译器其实行数
        }
        return obj;
    }

    public void compile(UniversalScriptSession session, UniversalScriptContext context, Reader in) throws Exception {
        this.commandRepository.clear();
        this.commandRepository.load(context);
        if (this.commandRepository.getDefault() == null) { // 设置脚本引擎默认命令
            String script = context.getContainer().getBean(UniversalScriptConfiguration.class).getDefaultCommand(); // 读取配置信息中的默认脚本语句
            if (StringUtils.isNotBlank(script)) {
                UniversalCommandCompiler compiler = this.commandRepository.get(script);
                this.commandRepository.setDefault(compiler);
            }
        }

        this.reader = new ScriptReader(in, this.startLineNumber);
        this.parser = new ScriptParser(session, context, this.commandRepository, this.reader);
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean hasNext() throws Exception {
        this.readMillis = System.currentTimeMillis();
        if (this.terminate) {
            this.cache.clear();
            return false;
        } else if (this.cache.size() == 0) {
            UniversalScriptCommand command = this.parser.read();
            return command != null && this.cache.add(command);
        } else {
            return true;
        }
    }

    public UniversalScriptCommand next() {
        return this.cache.poll();
    }

    public UniversalScriptAnalysis getAnalysis() {
        return this.analysis;
    }

    public UniversalScriptParser getParser() {
        return parser;
    }

    public UniversalCommandRepository getRepository() {
        return this.commandRepository;
    }

    public long getLineNumber() {
        return this.reader == null ? 0 : this.reader.getLineNumber();
    }

    public long getCompileMillis() {
        return readMillis;
    }

    public void close() {
        if (this.reader != null) {
            this.reader.close();
        }
        this.cache.clear();
        this.terminate = false;
    }

}
