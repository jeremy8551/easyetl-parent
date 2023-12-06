package icu.etl.script.internal;

import java.lang.reflect.Modifier;

import icu.etl.annotation.ScriptCommand;
import icu.etl.annotation.ScriptFunction;
import icu.etl.ioc.EasyBeanRegister;
import icu.etl.ioc.scan.ClassScanRule;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptVariableMethod;

/**
 * 注解加载器 <br>
 * 扫描所有被注解标记过的类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class ScriptClassScanRule implements ClassScanRule {

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public ScriptClassScanRule() {
    }

    public boolean process(Class<?> cls, EasyBeanRegister register) {
        if (cls == null) {
            return false;
        }

        boolean load = false;

        // 脚本引擎命令的实现类
        if (cls.isAnnotationPresent(ScriptCommand.class)  //
                && UniversalCommandCompiler.class.isAssignableFrom(cls) //
                && !Modifier.isAbstract(cls.getModifiers()) //
                && register.addBean(cls) //
        ) {
            load = true;
        }

        // 脚本引擎变量方法的实现类
        if (cls.isAnnotationPresent(ScriptFunction.class) //
                && UniversalScriptVariableMethod.class.isAssignableFrom(cls) //
                && !Modifier.isAbstract(cls.getModifiers()) //
                && register.addBean(cls) //
        ) {
            load = true;
        }

        return load;
    }

    public boolean equals(Object obj) {
        return obj != null && ScriptClassScanRule.class.equals(obj.getClass());
    }

}
