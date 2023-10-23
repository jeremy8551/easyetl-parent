package icu.etl.script.method;

import java.util.List;

import icu.etl.annotation.ScriptVariableFunction;
import icu.etl.ioc.BeanConfig;
import icu.etl.ioc.EasyetlContext;
import icu.etl.log.Log;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 扫描并加载脚本引擎变量方法
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public class VariableMethodScanner {

    /** 日志输出接口 */
    private Log log;

    /** 脚本引擎变量方法的工厂类 */
    private VariableMethodRepository repository;

    /** 脚本引擎工厂 */
    private UniversalScriptEngineFactory factory;

    /**
     * 初始化
     *
     * @param context    脚本引擎上下文信息
     * @param repository 变量方法仓库
     */
    public VariableMethodScanner(UniversalScriptContext context, VariableMethodRepository repository) {
        this.factory = context.getFactory();
        this.log = this.factory.getStdoutLog();
        this.repository = repository;

        // 组件工厂上下文信息
        EasyetlContext cxt = this.factory.getContext();

        // 显示所有已加载的变量方法
        List<BeanConfig> beans = cxt.getImplements(UniversalScriptVariableMethod.class);
        for (BeanConfig obj : beans) {
            Class<? extends UniversalScriptVariableMethod> cls = obj.getImplementClass();
            this.loadVariableMethod(cls);
        }

        // 如果类扫描器未扫描到脚本引擎默认变量方法，则自动加载所有变量方法
        if (beans.isEmpty()) {
            this.loadVariableMethodBuilder();
        }

        if (log.isDebugEnabled() && !this.repository.isEmpty()) {
            log.debug(this.repository.toString(null, true));
        }
    }

    /**
     * 加载基础变量方法
     */
    public void loadVariableMethodBuilder() {
        this.loadVariableMethod(DeleteFileMethod.class);
        this.loadVariableMethod(ElementMethod.class);
        this.loadVariableMethod(ExistsFileMethod.class);
        this.loadVariableMethod(FormatMethod.class);
        this.loadVariableMethod(GetDayMethod.class);
        this.loadVariableMethod(GetDaysMethod.class);
        this.loadVariableMethod(GetFileExtMethod.class);
        this.loadVariableMethod(GetFileLineSeparatorMethod.class);
        this.loadVariableMethod(GetFileSuffixMethod.class);
        this.loadVariableMethod(GetFilenameMethod.class);
        this.loadVariableMethod(GetFilenameNoExtMethod.class);
        this.loadVariableMethod(GetFilenameNoSuffixMethod.class);
        this.loadVariableMethod(GetHourMethod.class);
        this.loadVariableMethod(GetMillisMethod.class);
        this.loadVariableMethod(GetMinuteMethod.class);
        this.loadVariableMethod(GetMonthMethod.class);
        this.loadVariableMethod(GetParentMethod.class);
        this.loadVariableMethod(GetSecondMethod.class);
        this.loadVariableMethod(GetYearMethod.class);
        this.loadVariableMethod(IndexOfMethod.class);
        this.loadVariableMethod(IntMethod.class);
        this.loadVariableMethod(IsDirectoryMethod.class);
        this.loadVariableMethod(IsFileMethod.class);
        this.loadVariableMethod(LengthMethod.class);
        this.loadVariableMethod(LowerMethod.class);
        this.loadVariableMethod(LsMethod.class);
        this.loadVariableMethod(LtrimMethod.class);
        this.loadVariableMethod(MkdirMethod.class);
        this.loadVariableMethod(PrintMethod.class);
        this.loadVariableMethod(RtrimMethod.class);
        this.loadVariableMethod(SplitMethod.class);
        this.loadVariableMethod(SubstrMethod.class);
        this.loadVariableMethod(TouchMethod.class);
        this.loadVariableMethod(TrimMethod.class);
        this.loadVariableMethod(UpperMethod.class);
    }

    /**
     * 加载变量方法
     *
     * @param cls 变量方法的Class信息
     */
    public void loadVariableMethod(Class<? extends UniversalScriptVariableMethod> cls) {
        if (this.repository.contains(cls)) {
            return;
        }

        boolean exists = cls.isAnnotationPresent(ScriptVariableFunction.class);
        if (exists) {
            ScriptVariableFunction anno = cls.getAnnotation(ScriptVariableFunction.class);
            String name = StringUtils.trimBlank(anno.name());
            if (StringUtils.isBlank(name)) {
                return;
            }

            String[] words = StringUtils.trimBlank(anno.keywords());
            UniversalScriptVariableMethod method = this.repository.get(name);
            if (method != null) {
                throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(128, name, method.getClass().getName(), cls.getName()));
            }

            try {
                method = ClassUtils.newInstance(cls);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(12, cls.getName()), e);
                }
                return;
            }

            // 保存变量方法
            this.repository.add(name, method);
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getScriptStdoutMessage(43, cls.getName()));
            }

            // 添加关键字
            for (String key : words) {
                this.factory.getKeywords().add(key);
            }
        } else {
            if (log.isDebugEnabled()) { // 只有调试模式才会打印警告
                log.warn(ResourcesUtils.getScriptStderrMessage(52, cls.getName(), UniversalScriptVariableMethod.class.getName(), ScriptVariableFunction.class.getName()));
            }
        }
    }

}
