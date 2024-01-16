package icu.etl.script.internal;

import java.lang.reflect.Method;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptContextAware;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptException;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎命令类扫描器 <br>
 * 用于扫描当前JVM 中所有可用的脚本引擎命令类信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public class CommandScanner {
    private final static Log log = LogFactory.getLog(CommandScanner.class);

    /** 脚本引擎命令仓库 */
    private UniversalCommandRepository repository;

    /** 脚本引擎工厂 */
    private UniversalScriptEngineFactory factory;

    /** 脚本引擎上下文信息 */
    private UniversalScriptContext context;

    /**
     * 初始化
     *
     * @param context    脚本引擎上下文信息
     * @param repository 命令仓库
     */
    public CommandScanner(UniversalScriptContext context, UniversalCommandRepository repository) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (repository == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.factory = context.getFactory();
        this.repository = repository;

        // 显示所有已加载的脚本引擎命令
        EasyContext cxt = context.getContainer();
        List<EasyBeanInfo> beanList = cxt.getBeanInfoList(UniversalCommandCompiler.class);
        for (EasyBeanInfo beanInfo : beanList) {
            Class<? extends UniversalCommandCompiler> cls = beanInfo.getType();
            try {
                this.loadScriptCommand(cls);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(18, cls.getName()), e);
                }
            }
        }
    }

    /**
     * 加载脚本命令
     *
     * @param cls 脚本引擎命令的Class信息
     */
    public void loadScriptCommand(Class<? extends UniversalCommandCompiler> cls) {
        if (this.repository.contains(cls)) {
            return;
        }

        if (cls.isAnnotationPresent(ScriptCommand.class)) {
            ScriptCommand anno = cls.getAnnotation(ScriptCommand.class);
            String[] names = StringUtils.trimBlank(anno.name());
            String[] words = StringUtils.trimBlank(anno.keywords());

            if (StringUtils.isBlank(names)) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getScriptStderrMessage(48, cls.getName(), ScriptCommand.class.getName(), "name"));
                }
                return;
            }

            UniversalCommandCompiler compiler;
            try {
                compiler = this.context.getContainer().createBean(cls);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(12, cls.getName()), e);
                }
                return;
            }

            // 向编译器中注入脚本引擎上下文信息
            if (compiler instanceof UniversalScriptContextAware) {
                ((UniversalScriptContextAware) compiler).setContext(this.context);
            } else {
                this.invoke(compiler, this.context);
            }

            this.repository.add(names, compiler);

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getScriptStdoutMessage(50, cls.getName()));
            }

            // 关键字
            for (String key : words) {
                this.factory.getKeywords().add(key);
            }
        } else {
            if (log.isDebugEnabled()) { // 只有调试模式才会打印警告
                log.warn(ResourcesUtils.getScriptStderrMessage(52, cls.getName(), UniversalScriptCommand.class.getName(), ScriptCommand.class.getName()));
            }
        }
    }

    /**
     * 向编译器中注入脚本引擎上下文信息，如果编译器类中存在 void set(UniversalScriptContext) 方法时，自动调用方法并注入上下文信息
     *
     * @param compiler 编译器
     * @param context  上下文信息
     */
    private void invoke(UniversalCommandCompiler compiler, UniversalScriptContext context) {
        Method[] methods = compiler.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (method.getName().startsWith("set") // 方法名
                    && "void".equalsIgnoreCase(method.getReturnType().getName()) // 无返回值
                    && types != null && types.length == 1 && types[0].equals(UniversalScriptContext.class) // 只有一个参数
            ) {
                try {
                    method.invoke(compiler, new Object[]{context});
                } catch (Throwable e) {
                    throw new UniversalScriptException(compiler.getClass().getName() + "." + StringUtils.toString(method), e);
                }
            }
        }
    }

}
