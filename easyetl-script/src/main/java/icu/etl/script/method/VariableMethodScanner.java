package icu.etl.script.method;

import java.util.List;

import icu.etl.annotation.ScriptVariableFunction;
import icu.etl.ioc.BeanInfo;
import icu.etl.script.Script;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 扫描并加载脚本引擎变量方法
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public class VariableMethodScanner {

    /** 脚本引擎变量方法的工厂类 */
    private VariableMethodRepository repository;

    /** 脚本引擎工厂 */
    private UniversalScriptEngineFactory factory;

    private UniversalScriptContext context;

    /**
     * 初始化
     *
     * @param context    脚本引擎上下文信息
     * @param repository 变量方法仓库
     */
    public VariableMethodScanner(UniversalScriptContext context, VariableMethodRepository repository) {
        this.context = context;
        this.factory = context.getFactory();
        this.repository = repository;

        // 显示所有已加载的变量方法
        List<BeanInfo> beanInfoList = this.factory.getContext().getBeanInfoList(UniversalScriptVariableMethod.class);
        for (BeanInfo beanInfo : beanInfoList) {
            this.loadVariableMethod(beanInfo.getType());
        }

        // 如果类扫描器未扫描到脚本引擎默认变量方法，则自动加载所有变量方法
        if (beanInfoList.isEmpty()) {
            this.loadVariableMethodBuilder();
        }

        if (Script.out.isDebugEnabled() && !this.repository.isEmpty()) {
            Script.out.debug(this.repository.toString(null, true));
        }
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

        if (cls.isAnnotationPresent(ScriptVariableFunction.class)) {
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
                method = this.context.getFactory().getContext().createBean(cls);
            } catch (Throwable e) {
                if (Script.out.isWarnEnabled()) {
                    Script.out.warn(ResourcesUtils.getScriptStdoutMessage(42, cls.getName()), e);
                }
                return;
            }

            // 保存变量方法
            this.repository.add(name, method);

            if (Script.out.isDebugEnabled()) {
                Script.out.debug(ResourcesUtils.getScriptStdoutMessage(51, cls.getName()));
            }

            // 添加关键字
            for (String key : words) {
                this.factory.getKeywords().add(key);
            }
        } else {
            if (Script.out.isDebugEnabled()) { // 只有调试模式才会打印警告
                Script.out.warn(ResourcesUtils.getScriptStderrMessage(52, cls.getName(), UniversalScriptVariableMethod.class.getName(), ScriptVariableFunction.class.getName()));
            }
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

}
