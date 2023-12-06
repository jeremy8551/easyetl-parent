package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanRegister;
import icu.etl.ioc.scan.ClassScanRule;

/**
 * 类扫描器的扫描规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class ClassScanRuleImpl implements ClassScanRule {

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public ClassScanRuleImpl() {
    }

    public boolean process(Class<?> cls, EasyBeanRegister register) {
        return cls != null && cls.isAnnotationPresent(EasyBean.class) && register.addBean(cls);
    }

    public boolean equals(Object obj) {
        return obj != null && ClassScanRuleImpl.class.equals(obj.getClass());
    }

}
