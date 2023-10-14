package icu.etl.script.internal;

import java.lang.reflect.Modifier;
import java.util.Comparator;

import icu.etl.annotation.ScriptCommand;
import icu.etl.annotation.ScriptVariableFunction;
import icu.etl.ioc.BeanConfig;
import icu.etl.ioc.BeanRegister;
import icu.etl.ioc.ClassScanRule;
import icu.etl.log.STD;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.util.ResourcesUtils;

/**
 * 注解加载器 <br>
 * 扫描所有被注解标记过的类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class ScriptClassScanRule implements ClassScanRule {

    /** 用于判断组件实现是否相等 */
    private Comparator<BeanConfig> comparator = new Comparator<BeanConfig>() {
        public int compare(BeanConfig o1, BeanConfig o2) {
            return o1.getImplementClass().equals(o2.getImplementClass()) ? 0 : 1;
        }
    };

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public ScriptClassScanRule() {
    }

    public boolean process(Class<?> cls, BeanRegister register) {
        if (cls == null) {
            return false;
        }

        boolean load = false;

        // 脚本引擎命令的实现类
        if (cls.isAnnotationPresent(ScriptCommand.class) && UniversalCommandCompiler.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
            ScriptCommand anno = cls.getAnnotation(ScriptCommand.class);
            BeanConfig bean = new BeanConfig(UniversalCommandCompiler.class, cls, anno);

            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getClassMessage(9, cls.getName()));
            }
            register.add(bean, this.comparator);
            load = true;
        }

        // 脚本引擎变量方法的实现类
        if (cls.isAnnotationPresent(ScriptVariableFunction.class) && UniversalScriptVariableMethod.class.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers())) {
            ScriptVariableFunction anno = cls.getAnnotation(ScriptVariableFunction.class);
            BeanConfig bean = new BeanConfig(UniversalScriptVariableMethod.class, cls, anno);

            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getClassMessage(20, cls.getName()));
            }
            register.add(bean, this.comparator);
            load = true;
        }

        return load;
    }

    public boolean equals(Object obj) {
        return obj != null && ScriptClassScanRule.class.equals(obj.getClass());
    }

}
