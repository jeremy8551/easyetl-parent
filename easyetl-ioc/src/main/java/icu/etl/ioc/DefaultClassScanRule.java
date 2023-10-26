package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBean;

/**
 * 类扫描器的扫描规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class DefaultClassScanRule implements ClassScanRule, Comparator<BeanInfo> {

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public DefaultClassScanRule() {
    }

    public boolean process(Class<?> cls, BeanRegister register) {
        if (cls == null) {
            return false;
        }

        // 如果类上配置了注解
        if (cls.isAnnotationPresent(EasyBean.class)) {
            return register.addBean(new AnnotationBeanInfo(cls), this);
        }
        return false;
    }

    public int compare(BeanInfo o1, BeanInfo o2) {
        if (o1.equals(o2.getName()) && o1.equals(o2.getType())) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean equals(Object obj) {
        return obj != null && DefaultClassScanRule.class.equals(obj.getClass());
    }

}
