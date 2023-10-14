package icu.etl.script.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import icu.etl.annotation.ScriptCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.util.CharTable;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本命令仓库
 *
 * @author jeremy8551@qq.com
 */
public class CommandRepository implements UniversalCommandRepository, Iterable<CommandCompilerContext> {

    /** 脚本引擎语句分析器 */
    private UniversalScriptAnalysis analysis;

    /** 所有命令编译器 */
    private List<CommandCompilerContext> all;

    /** 优先级为 1 的命令编译器 */
    private List<CommandCompilerContext> rule1;

    /** 优先级为 2 的命令编译器，集合中是命令名与脚本命令配置信息的映射关系 */
    private Map<String, CommandCompilerContext> rule2;

    /** 优先级为 3 的命令编译器，集合中是命令名与脚本命令集合的映射关系，即一个命令名对应多个命令的情况 */
    private Map<String, List<CommandCompilerContext>> rule3;

    /** 优先级为 4 的命令编译器 */
    private List<CommandCompilerContext> rule4;

    /** 优先级为 5 的命令编译器，脚本引擎默认命令编译器 */
    private UniversalCommandCompiler defaultCompiler;

    /** 识别脚本语句时使用的缓冲区 */
    private ArrayList<UniversalCommandCompiler> cache;

    /**
     * 初始化
     */
    public CommandRepository(UniversalScriptAnalysis analysis) {
        if (analysis == null) {
            throw new NullPointerException();
        }

        this.analysis = analysis;
        this.all = new ArrayList<CommandCompilerContext>();
        this.cache = new ArrayList<UniversalCommandCompiler>();
        this.rule1 = new ArrayList<CommandCompilerContext>();
        this.rule2 = new HashMap<String, CommandCompilerContext>();
        this.rule3 = new HashMap<String, List<CommandCompilerContext>>();
        this.rule4 = new ArrayList<CommandCompilerContext>();
    }

    public void load(UniversalScriptContext context) {
        new CommandScanner(context, this);
    }

    public void setDefault(UniversalCommandCompiler compiler) {
        this.defaultCompiler = compiler;
    }

    public UniversalCommandCompiler getDefault() {
        return this.defaultCompiler;
    }

    public boolean contains(Class<? extends UniversalCommandCompiler> cls) {
        if (cls != null) {
            for (CommandCompilerContext context : this.all) {
                if (context.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clear() {
        this.all.clear();
        this.rule1.clear();
        this.rule2.clear();
        this.rule3.clear();
        this.rule4.clear();
//		this.rule5 = null; 不能删除默认命令
    }

    public void add(String[] names, UniversalCommandCompiler compiler) {
        Class<? extends UniversalCommandCompiler> cls = compiler.getClass();
        if (this.contains(cls)) { // 校验是否重复添加
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(54, cls.getName(), cls.getName(), "kind()"));
        }

        CommandCompilerContext context = new CommandCompilerContext(compiler);
        for (String name : names) {
            this.add(name, context);
        }
        this.all.add(context);
    }

    /**
     * 添加脚本命令的注解配置信息
     *
     * @param name    脚本命令前缀
     * @param context 脚本命令配置信息
     */
    private void add(String name, CommandCompilerContext context) {
        String key = name.toUpperCase();

        // 添加 1 级匹配规则
        if ("^".equals(key)) {
            if (!this.rule1.contains(context)) {
                this.rule1.add(context);
            }
            return;
        }

        // 添加 4 级匹配规则
        else if ("*".equals(key)) {
            if (!this.rule4.contains(context)) {
                this.rule4.add(context);
            }
            return;
        }

        if (this.rule2.containsKey(key)) {
            List<CommandCompilerContext> list = this.rule3.get(key);
            if (list == null) {
                list = new ArrayList<CommandCompilerContext>();
            }

            CommandCompilerContext old = this.rule2.remove(key);
            if (old != null) {
                list.add(old);
            }
            list.add(context);
            this.rule3.put(key, list);
            return;
        }

        if (this.rule3.containsKey(key)) {
            List<CommandCompilerContext> list = this.rule3.get(key);
            if (list == null) {
                list = new ArrayList<CommandCompilerContext>();
            }

            list.add(context);
            this.rule3.put(key, list);
            return;
        }

        this.rule2.put(key, context);
    }

    @SuppressWarnings("unchecked")
    public <E extends UniversalCommandCompiler> E get(Class<E> cls) {
        for (CommandCompilerContext context : this.all) {
            if (context.isAssignableFrom(cls)) {
                return (E) context.getCompiler();
            }
        }
        return null;
    }

    public synchronized UniversalCommandCompiler get(String script) {
        this.cache.clear();

        String name = this.analysis.getPrefix(script); // 截取命令名
        String key = name.toUpperCase();

        // 尝试匹配 * 命令
        for (CommandCompilerContext cxt : this.rule1) {
            int code = cxt.getCompiler().match(name, script);
            switch (code) {
                case 0:
                    this.cache.add(cxt.getCompiler());
                    break;
                case 1:
                    return cxt.getCompiler();
                case 2:
                    continue;
                case 3:
                    return this.defaultCompiler;
                default:
                    throw new UnsupportedOperationException(String.valueOf(code));
            }
        }

        if (this.cache.size() == 1) {
            return this.cache.get(0);
        } else if (this.cache.size() > 1) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(51, script, this.cache.size(), StringUtils.join(this.cache, ", ")));
        }

        // 优先根据命令的名称查找对应的编译器
        CommandCompilerContext cxt = this.rule2.get(key);
        if (cxt != null) {
            return cxt.getCompiler();
        }

        // 查找相同名字的命令
        List<CommandCompilerContext> list = this.rule3.get(key);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                CommandCompilerContext ccxt = list.get(i);
                int code = ccxt.getCompiler().match(name, script);
                switch (code) {
                    case 0:
                        this.cache.add(ccxt.getCompiler());
                        break;
                    case 1:
                        return ccxt.getCompiler();
                    case 2:
                        continue;
                    case 3:
                        return this.defaultCompiler;
                    default:
                        throw new UnsupportedOperationException(String.valueOf(code));
                }
            }
        }

        if (this.cache.size() == 1) {
            return this.cache.get(0);
        } else if (this.cache.size() > 1) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(51, script, this.cache.size(), StringUtils.join(this.cache, ", ")));
        }

        // 尝试匹配 * 命令
        for (CommandCompilerContext obj : this.rule4) {
            int code = obj.getCompiler().match(name, script);
            switch (code) {
                case 0:
                    this.cache.add(obj.getCompiler());
                    break;
                case 1:
                    return obj.getCompiler();
                case 2:
                    continue;
                case 3:
                    return this.defaultCompiler;
                default:
                    throw new UnsupportedOperationException(String.valueOf(code));
            }
        }

        if (this.cache.size() == 1) {
            return this.cache.get(0);
        } else if (this.cache.size() > 1) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(51, script, this.cache.size(), StringUtils.join(this.cache, ", ")));
        } else { // count == 0
            return this.defaultCompiler; // 默认命令
        }
    }

    public Iterator<CommandCompilerContext> iterator() {
        List<CommandCompilerContext> list = new ArrayList<CommandCompilerContext>(this.all);
        Collections.sort(list, new Comparator<CommandCompilerContext>() {

            public int compare(CommandCompilerContext o1, CommandCompilerContext o2) {
                return o1.getInstructionOrder() - o2.getInstructionOrder();
            }
        });
        return list.iterator();
    }

    public boolean isEmpty() {
        return this.all.isEmpty();
    }

    public String toString() {
        return this.toString(StringUtils.CHARSET, true);
    }

    public String toString(String charsetName, boolean ltrim) {
        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg007"), ',');
        CharTable ct = new CharTable(charsetName);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[0]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[1]);
        ct.addTitle(CharTable.ALIGN_MIDDLE, titles[3]);

        for (Iterator<CommandCompilerContext> it = this.iterator(); it.hasNext(); ) {
            CommandCompilerContext obj = it.next();
            Class<? extends UniversalCommandCompiler> cls = obj.getCompiler().getClass();
            ScriptCommand anno = cls.getAnnotation(ScriptCommand.class);

            String[] prefixs = StringUtils.removeBlank(anno.name());
            for (String name : prefixs) {
                ct.addValue(name);
                ct.addValue(cls.getName());
                ct.addValue(ResourcesUtils.existsScriptMessage(obj.getUsage()) ? "" : "------");
            }
        }

        if (ltrim) {
            ct.removeLeftBlank();
        }
        return ct.toDB2Shape();
    }

}
